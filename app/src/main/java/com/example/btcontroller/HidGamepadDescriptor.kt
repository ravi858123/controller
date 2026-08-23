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

/**
 * Standaard USB-HID "Game Pad" report descriptor.
 *
 * Report-indeling (5 bytes, exclusief report-ID die apart wordt meegegeven
 * aan BluetoothHidDevice.sendReport()):
 *   byte 0: 8 standaardknoppen, 1 bit per knop (A, B, X, Y, L, R, Select, Start)
 *   byte 1: 8 door de gebruiker toe te voegen "eigen knoppen", 1 bit per knop
 *   byte 2: X-as joystick, signed -127..127
 *   byte 3: Y-as joystick, signed -127..127
 *   byte 4: onderste 4 bits = D-pad "hat switch" (0=boven ... 7=links-boven,
 *           8 = neutraal). Bovenste 4 bits = padding (altijd 0).
 */
object HidGamepadDescriptor {

    const val REPORT_ID: Byte = 0x01
    const val CUSTOM_BUTTON_COUNT = 8

    val DESCRIPTOR: ByteArray = byteArrayOf(
        0x05, 0x01,             // USAGE_PAGE (Generic Desktop)
        0x09, 0x05,             // USAGE (Game Pad)
        0xa1.toByte(), 0x01,    // COLLECTION (Application)
        0x85.toByte(), REPORT_ID, //   REPORT_ID (1)

        // --- 16 knoppen (8 standaard + 8 eigen) ---
        0x05, 0x09,             //   USAGE_PAGE (Button)
        0x19, 0x01,             //   USAGE_MINIMUM (Button 1)
        0x29, 0x10,             //   USAGE_MAXIMUM (Button 16)
        0x15, 0x00,             //   LOGICAL_MINIMUM (0)
        0x25, 0x01,             //   LOGICAL_MAXIMUM (1)
        0x75, 0x01,             //   REPORT_SIZE (1)
        0x95.toByte(), 0x10,    //   REPORT_COUNT (16)
        0x81.toByte(), 0x02,    //   INPUT (Data,Var,Abs)

        // --- Joystick X/Y ---
        0x05, 0x01,             //   USAGE_PAGE (Generic Desktop)
        0x09, 0x01,             //   USAGE (Pointer)
        0xa1.toByte(), 0x00,    //   COLLECTION (Physical)
        0x09, 0x30,             //     USAGE (X)
        0x09, 0x31,             //     USAGE (Y)
        0x15, 0x81.toByte(),    //     LOGICAL_MINIMUM (-127)
        0x25, 0x7f,             //     LOGICAL_MAXIMUM (127)
        0x75, 0x08,             //     REPORT_SIZE (8)
        0x95.toByte(), 0x02,    //     REPORT_COUNT (2)
        0x81.toByte(), 0x02,    //     INPUT (Data,Var,Abs)
        0xc0.toByte(),          //   END_COLLECTION

        // --- D-pad (hat switch) ---
        0x09, 0x39,             //   USAGE (Hat switch)
        0x15, 0x00,             //   LOGICAL_MINIMUM (0)
        0x25, 0x07,             //   LOGICAL_MAXIMUM (7)
        0x35, 0x00,             //   PHYSICAL_MINIMUM (0)
        0x46, 0x3b, 0x01,       //   PHYSICAL_MAXIMUM (315)
        0x65, 0x14,             //   UNIT (Eng Rot:Angular Pos)
        0x75, 0x04,             //   REPORT_SIZE (4)
        0x95.toByte(), 0x01,    //   REPORT_COUNT (1)
        0x81.toByte(), 0x42,    //   INPUT (Data,Var,Abs,Null) -> waarde 8 = neutraal
        0x75, 0x04,             //   REPORT_SIZE (4)  (padding)
        0x95.toByte(), 0x01,    //   REPORT_COUNT (1)
        0x81.toByte(), 0x01,    //   INPUT (Cnst,Ary,Abs) -> padding

        0xc0.toByte()           // END_COLLECTION
    )
}
