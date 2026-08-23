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

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BluetoothDeviceAdapter(
    private val devices: List<BluetoothDevice>,
    private val connectedAddress: String?,
    private val onDeviceClick: (BluetoothDevice, isConnected: Boolean) -> Unit
) : RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.deviceName)
        val address: TextView = view.findViewById(R.id.deviceAddress)
        val action: TextView = view.findViewById(R.id.deviceAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bluetooth_device, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("MissingPermission") // permissies zijn al gecontroleerd vóór het tonen van deze lijst
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]
        val isConnected = device.address == connectedAddress

        holder.name.text = device.name ?: device.address
        holder.address.text = device.address
        holder.action.text = holder.itemView.context.getString(
            if (isConnected) R.string.bt_disconnect else R.string.bt_connect
        )
        holder.itemView.setOnClickListener { onDeviceClick(device, isConnected) }
        holder.action.setOnClickListener { onDeviceClick(device, isConnected) }
    }

    override fun getItemCount(): Int = devices.size
}
