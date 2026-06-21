package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "Alarm",
    val isEnabled: Boolean = true,
    val repeatDays: String = "", // e.g. "Mon,Tue" or empty for Once
    val isVibrate: Boolean = true
) {
    val formattedTime: String
        get() = String.format("%02d:%02d", hour, minute)
}
