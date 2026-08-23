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

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log

/**
 * Wrapper rond de Android BluetoothHidDevice API.
 *
 * Meldt deze telefoon aan als een Bluetooth HID "Game Pad" en stuurt
 * input-reports naar het gekoppelde apparaat (PC, laptop, console, ...).
 *
 * Let op: dit werkt alleen op een écht toestel (geen emulator) en alleen als
 * de Bluetooth-chipset van het toestel de HID-Device rol ondersteunt.
 */
class BluetoothHidController(
    private val context: Context,
    private val listener: Listener
) {

    interface Listener {
        fun onAppRegistered(success: Boolean)
        fun onConnectionStateChanged(device: BluetoothDevice?, state: Int)
    }

    companion object {
        private const val TAG = "BtHidController"
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

    private var hidDevice: BluetoothHidDevice? = null
    var connectedDevice: BluetoothDevice? = null
        private set
    var isRegistered: Boolean = false
        private set

    private val sdpSettings = BluetoothHidDeviceAppSdpSettings(
        "BT Control",                      // naam zoals hij verschijnt op de host
        "Virtuele Bluetooth gamepad",       // beschrijving
        "BtControlContributors",            // provider
        BluetoothHidDevice.SUBCLASS1_COMBO,
        HidGamepadDescriptor.DESCRIPTOR
    )

    private val qosSettings = BluetoothHidDeviceAppQosSettings(
        BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
        800,   // token rate
        9,     // token bucket size
        0,     // peak bandwidth
        11250, // latency (us)
        11250  // delay variation (us)
    )

    private val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile != BluetoothProfile.HID_DEVICE) return
            hidDevice = proxy as BluetoothHidDevice
            registerApp()
        }

        override fun onServiceDisconnected(profile: Int) {
            hidDevice = null
            isRegistered = false
        }
    }

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d(TAG, "App geregistreerd: $registered")
            isRegistered = registered
            listener.onAppRegistered(registered)
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            Log.d(TAG, "Connectie state: $state voor ${device?.name}")
            connectedDevice = if (state == BluetoothProfile.STATE_CONNECTED) device else null
            listener.onConnectionStateChanged(device, state)
        }

        override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
            // Niet nodig voor deze gamepad-implementatie.
        }

        override fun onSetReport(device: BluetoothDevice?, type: Byte, id: Byte, data: ByteArray?) {
            // Niet nodig voor deze gamepad-implementatie.
        }
    }

    /** Start de registratie van de HID-proxy. Roep dit aan nadat permissies zijn gegeven. */
    fun start() {
        val adapter = bluetoothAdapter ?: run {
            Log.e(TAG, "Geen Bluetooth adapter beschikbaar")
            listener.onAppRegistered(false)
            return
        }
        adapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
    }

    private fun registerApp() {
        try {
            hidDevice?.registerApp(sdpSettings, null, qosSettings, { it.run() }, hidCallback)
        } catch (se: SecurityException) {
            Log.e(TAG, "Geen permissie om HID app te registreren", se)
            listener.onAppRegistered(false)
        }
    }

    /** Geeft de lijst met reeds gekoppelde (bonded) apparaten terug om uit te kiezen. */
    fun getBondedDevices(): List<BluetoothDevice> {
        return try {
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } catch (se: SecurityException) {
            emptyList()
        }
    }

    /** Vraag verbinding met een specifiek gekoppeld apparaat als host. */
    fun connect(device: BluetoothDevice) {
        try {
            hidDevice?.connect(device)
        } catch (se: SecurityException) {
            Log.e(TAG, "Geen permissie om te verbinden", se)
        }
    }

    fun disconnect(device: BluetoothDevice) {
        try {
            hidDevice?.disconnect(device)
        } catch (se: SecurityException) {
            Log.e(TAG, "Geen permissie om te verbreken", se)
        }
    }

    /**
     * Stuur een gamepad-input-report naar de host.
     * @param standardButtons bitmasker van de 8 standaardknoppen (A/B/X/Y/L/R/Select/Start)
     * @param customButtons bitmasker van de 8 door de gebruiker toegevoegde knoppen
     * @param x joystick X-as, -127..127
     * @param y joystick Y-as, -127..127
     * @param hat D-pad richting, 0-7, of 8 voor neutraal
     */
    fun sendReport(standardButtons: Int, customButtons: Int, x: Int, y: Int, hat: Int) {
        val device = connectedDevice ?: return
        val report = byteArrayOf(
            (standardButtons and 0xFF).toByte(),
            (customButtons and 0xFF).toByte(),
            x.coerceIn(-127, 127).toByte(),
            y.coerceIn(-127, 127).toByte(),
            (hat and 0x0F).toByte()
        )
        try {
            hidDevice?.sendReport(device, HidGamepadDescriptor.REPORT_ID.toInt(), report)
        } catch (se: SecurityException) {
            Log.e(TAG, "Geen permissie om report te sturen", se)
        }
    }

    /** Ruim de proxy netjes op, bijvoorbeeld in onDestroy(). */
    fun stop() {
        try {
            connectedDevice?.let { hidDevice?.disconnect(it) }
            hidDevice?.unregisterApp()
            bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
        } catch (se: SecurityException) {
            Log.e(TAG, "Geen permissie tijdens opruimen", se)
        }
    }
}
