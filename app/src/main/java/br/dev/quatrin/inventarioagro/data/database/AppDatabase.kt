package br.dev.quatrin.inventarioagro.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.dev.quatrin.inventarioagro.data.dao.MaquinaDao
import br.dev.quatrin.inventarioagro.data.dao.MarcaDao
import br.dev.quatrin.inventarioagro.data.dao.TipoMaquinaDao
import br.dev.quatrin.inventarioagro.data.model.Maquina
import br.dev.quatrin.inventarioagro.data.model.Marca
import br.dev.quatrin.inventarioagro.data.model.TipoMaquina

@Database(
    entities = [TipoMaquina::class, Marca::class, Maquina::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tipoMaquinaDao(): TipoMaquinaDao
    abstract fun marcaDao(): MarcaDao
    abstract fun maquinaDao(): MaquinaDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventario_agro_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}