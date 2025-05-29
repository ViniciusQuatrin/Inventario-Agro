package br.dev.quatrin.inventarioagro

import android.app.Application
import br.dev.quatrin.inventarioagro.data.database.AppDatabase

class InventarioAgroApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    companion object {
        lateinit var instance: InventarioAgroApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}