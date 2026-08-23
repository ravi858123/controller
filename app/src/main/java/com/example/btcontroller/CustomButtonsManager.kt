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

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Eén door de gebruiker toegevoegde knop. [bitIndex] loopt van 0..7. */
data class CustomButton(
    val id: String,
    val label: String,
    val bitIndex: Int
)

/**
 * Beheert de door de gebruiker toegevoegde knoppen. Opslag gebeurt lokaal via
 * SharedPreferences als JSON — geen extra afhankelijkheden nodig (org.json zit
 * standaard in Android), wat prettig is voor een minimale, F-Droid-vriendelijke
 * dependency-graph.
 */
class CustomButtonsManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadButtons(): List<CustomButton> {
        val raw = prefs.getString(KEY_BUTTONS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                CustomButton(
                    id = obj.getString("id"),
                    label = obj.getString("label"),
                    bitIndex = obj.getInt("bitIndex")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveButtons(buttons: List<CustomButton>) {
        val array = JSONArray()
        buttons.forEach { button ->
            val obj = JSONObject()
            obj.put("id", button.id)
            obj.put("label", button.label)
            obj.put("bitIndex", button.bitIndex)
            array.put(obj)
        }
        prefs.edit().putString(KEY_BUTTONS, array.toString()).apply()
    }

    /** Voegt een knop toe met de eerstvolgende vrije bit. Retourneert null als vol (max 8). */
    fun addButton(label: String): CustomButton? {
        val current = loadButtons()
        if (current.size >= HidGamepadDescriptor.CUSTOM_BUTTON_COUNT) return null
        val usedBits = current.map { it.bitIndex }.toSet()
        val freeBit = (0 until HidGamepadDescriptor.CUSTOM_BUTTON_COUNT).firstOrNull { it !in usedBits }
            ?: return null
        val newButton = CustomButton(
            id = "custom_${System.currentTimeMillis()}",
            label = label.trim().ifBlank { "Knop" },
            bitIndex = freeBit
        )
        saveButtons(current + newButton)
        return newButton
    }

    fun removeButton(id: String) {
        val current = loadButtons()
        saveButtons(current.filterNot { it.id == id })
    }

    companion object {
        private const val PREFS_NAME = "bt_control_custom_buttons"
        private const val KEY_BUTTONS = "buttons_json"
    }
}
