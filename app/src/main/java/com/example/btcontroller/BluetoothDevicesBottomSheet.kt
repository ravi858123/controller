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

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Toont de reeds gekoppelde (bonded) Bluetooth-apparaten en laat de gebruiker
 * er één kiezen om mee te verbinden of van te verbreken. Nieuwe apparaten
 * koppel je via de systeem Bluetooth-instellingen (link onderaan) — deze app
 * heeft geen eigen scan-functie, dat blijft bewust aan het systeem over.
 */
class BluetoothDevicesBottomSheet(
    private val devices: List<BluetoothDevice>,
    private val connectedAddress: String?,
    private val onConnect: (BluetoothDevice) -> Unit,
    private val onDisconnect: (BluetoothDevice) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_bluetooth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.deviceList)
        val emptyState = view.findViewById<View>(R.id.emptyState)
        val openSettings = view.findViewById<View>(R.id.openSettingsButton)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = BluetoothDeviceAdapter(devices, connectedAddress) { device, isConnected ->
            if (isConnected) onDisconnect(device) else onConnect(device)
            dismiss()
        }

        val isEmpty = devices.isEmpty()
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE

        openSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            dismiss()
        }
    }

    companion object {
        const val TAG = "BluetoothDevicesBottomSheet"
    }
}
