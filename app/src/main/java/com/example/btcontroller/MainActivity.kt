/*
 * Copyright 2026 BT Control contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.btcontroller

import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.btcontroller.views.JoystickView

class MainActivity : AppCompatActivity(), BluetoothHidController.Listener {

    private lateinit var hidController: BluetoothHidController
    private lateinit var customButtonsManager: CustomButtonsManager

    private lateinit var statusText: TextView
    private lateinit var statusDot: android.view.View
    private lateinit var customButtonsContainer: android.widget.LinearLayout

    // Bitmasker-posities voor de 8 standaardknoppen. Moet overeenkomen met de
    // volgorde die HidGamepadDescriptor verwacht (byte 0 van het report).
    private object ButtonBit {
        const val A = 1 shl 0
        const val B = 1 shl 1
        const val X = 1 shl 2
        const val Y = 1 shl 3
        const val L = 1 shl 4
        const val R = 1 shl 5
        const val SELECT = 1 shl 6
        const val START = 1 shl 7
    }

    private var standardButtonState = 0
    private var customButtonState = 0
    private var joyX = 0
    private var joyY = 0
    private var hatState = 8 // 8 = neutraal

    private var dpadUp = false
    private var dpadDown = false
    private var dpadLeft = false
    private var dpadRight = false

    private val requiredPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_ADVERTISE,
                android.Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

    private val permissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            hidController.start()
        } else {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        statusDot = findViewById(R.id.statusDot)
        customButtonsContainer = findViewById(R.id.customButtonsContainer)

        hidController = BluetoothHidController(this, this)
        customButtonsManager = CustomButtonsManager(this)

        findViewById<ImageButton>(R.id.bluetoothButton).setOnClickListener { showDevicePicker() }
        findViewById<ImageButton>(R.id.addCustomButton).setOnClickListener { showAddCustomButtonDialog() }

        setupFaceButtons()
        setupDpad()
        setupShoulderButtons()
        setupJoystick()
        renderCustomButtons()

        setStatusDotColor(R.color.status_disconnected)

        if (hasAllPermissions()) {
            hidController.start()
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    private fun hasAllPermissions(): Boolean = requiredPermissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    // ---------- UI setup: standaardknoppen ----------

    private fun setupFaceButtons() {
        bindButton(R.id.btnA, ButtonBit.A)
        bindButton(R.id.btnB, ButtonBit.B)
        bindButton(R.id.btnX, ButtonBit.X)
        bindButton(R.id.btnY, ButtonBit.Y)
    }

    private fun setupShoulderButtons() {
        bindButton(R.id.btnL, ButtonBit.L)
        bindButton(R.id.btnR, ButtonBit.R)
        bindButton(R.id.btnSelect, ButtonBit.SELECT)
        bindButton(R.id.btnStart, ButtonBit.START)
    }

    private fun bindButton(viewId: Int, bit: Int) {
        val button = findViewById<TextView>(viewId)
        button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    standardButtonState = standardButtonState or bit
                    sendReport()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    standardButtonState = standardButtonState and bit.inv()
                    sendReport()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupDpad() {
        bindDpadButton(R.id.dpadUp) { pressed -> dpadUp = pressed }
        bindDpadButton(R.id.dpadDown) { pressed -> dpadDown = pressed }
        bindDpadButton(R.id.dpadLeft) { pressed -> dpadLeft = pressed }
        bindDpadButton(R.id.dpadRight) { pressed -> dpadRight = pressed }
    }

    private fun bindDpadButton(viewId: Int, update: (Boolean) -> Unit) {
        val button = findViewById<TextView>(viewId)
        button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    update(true)
                    recomputeHat()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    update(false)
                    recomputeHat()
                    true
                }
                else -> false
            }
        }
    }

    private fun recomputeHat() {
        hatState = when {
            dpadUp && dpadRight -> 1
            dpadRight && dpadDown -> 3
            dpadDown && dpadLeft -> 5
            dpadLeft && dpadUp -> 7
            dpadUp -> 0
            dpadRight -> 2
            dpadDown -> 4
            dpadLeft -> 6
            else -> 8
        }
        sendReport()
    }

    private fun setupJoystick() {
        val joystick = findViewById<JoystickView>(R.id.joystick)
        joystick.onMoveListener = { x, y ->
            joyX = x
            joyY = y
            sendReport()
        }
    }

    // ---------- Eigen (custom) knoppen ----------

    private fun renderCustomButtons() {
        customButtonsContainer.removeAllViews()
        val buttons = customButtonsManager.loadButtons()
        buttons.forEach { addCustomButtonView(it) }
    }

    private fun addCustomButtonView(button: CustomButton) {
        val view = TextView(this, null, 0, R.style.CustomButtonStyle)
        view.layoutParams = android.widget.LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            36.dpToPx()
        )
        view.text = button.label
        val bit = 1 shl button.bitIndex

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    customButtonState = customButtonState or bit
                    sendReport()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    customButtonState = customButtonState and bit.inv()
                    sendReport()
                    true
                }
                else -> false
            }
        }
        view.setOnLongClickListener {
            confirmRemoveCustomButton(button)
            true
        }
        customButtonsContainer.addView(view)
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun showAddCustomButtonDialog() {
        val current = customButtonsManager.loadButtons()
        if (current.size >= HidGamepadDescriptor.CUSTOM_BUTTON_COUNT) {
            Toast.makeText(this, getString(R.string.custom_limit_reached), Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = getString(R.string.custom_add_label_hint)
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.custom_add_title)
            .setView(input)
            .setPositiveButton(R.string.custom_add_confirm) { _, _ ->
                val label = input.text.toString().trim()
                if (label.isNotEmpty()) {
                    val added = customButtonsManager.addButton(label)
                    if (added != null) {
                        addCustomButtonView(added)
                    } else {
                        Toast.makeText(this, getString(R.string.custom_limit_reached), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.custom_add_cancel, null)
            .show()
    }

    private fun confirmRemoveCustomButton(button: CustomButton) {
        AlertDialog.Builder(this)
            .setTitle(R.string.custom_remove_title)
            .setMessage(getString(R.string.custom_remove_message, button.label))
            .setPositiveButton(R.string.custom_remove_confirm) { _, _ ->
                customButtonsManager.removeButton(button.id)
                renderCustomButtons()
            }
            .setNegativeButton(R.string.custom_add_cancel, null)
            .show()
    }

    // ---------- Reports versturen ----------

    private fun sendReport() {
        hidController.sendReport(standardButtonState, customButtonState, joyX, joyY, hatState)
    }

    // ---------- Bluetooth apparaatkiezer ----------

    private fun showDevicePicker() {
        if (!hasAllPermissions()) {
            permissionLauncher.launch(requiredPermissions)
            return
        }
        val devices = hidController.getBondedDevices()
        val connectedAddress = try {
            hidController.connectedDevice?.address
        } catch (se: SecurityException) {
            null
        }

        BluetoothDevicesBottomSheet(
            devices = devices,
            connectedAddress = connectedAddress,
            onConnect = { device -> hidController.connect(device) },
            onDisconnect = { device -> hidController.disconnect(device) }
        ).show(supportFragmentManager, BluetoothDevicesBottomSheet.TAG)
    }

    // ---------- BluetoothHidController.Listener ----------

    override fun onAppRegistered(success: Boolean) {
        runOnUiThread {
            if (success) {
                statusText.text = getString(R.string.status_ready)
                setStatusDotColor(R.color.status_disconnected)
            } else {
                statusText.text = getString(R.string.status_register_failed)
                setStatusDotColor(R.color.status_disconnected)
            }
        }
    }

    override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
        runOnUiThread {
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    statusText.text = getString(R.string.status_connected, safeDeviceName(device))
                    setStatusDotColor(R.color.status_connected)
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    statusText.text = getString(R.string.status_connecting)
                    setStatusDotColor(R.color.status_connecting)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    statusText.text = getString(R.string.status_disconnected)
                    setStatusDotColor(R.color.status_disconnected)
                }
            }
        }
    }

    private fun setStatusDotColor(colorRes: Int) {
        val drawable = statusDot.background.mutate() as? GradientDrawable
        drawable?.setColor(ContextCompat.getColor(this, colorRes))
    }

    private fun safeDeviceName(device: BluetoothDevice?): String {
        return try {
            device?.name ?: device?.address ?: "onbekend apparaat"
        } catch (se: SecurityException) {
            "onbekend apparaat"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hidController.stop()
    }
}
