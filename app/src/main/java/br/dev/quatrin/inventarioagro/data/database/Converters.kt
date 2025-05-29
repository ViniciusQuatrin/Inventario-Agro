package br.dev.quatrin.inventarioagro.data.database

import androidx.room.TypeConverter
import br.dev.quatrin.inventarioagro.data.model.StatusMaquina
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun toStatusMaquina(value: Char): StatusMaquina {
        return StatusMaquina.values().first { it.codigo == value }
    }
    
    @TypeConverter
    fun fromStatusMaquina(status: StatusMaquina): Char {
        return status.codigo
    }
}