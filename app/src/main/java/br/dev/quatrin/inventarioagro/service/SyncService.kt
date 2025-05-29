package br.dev.quatrin.inventarioagro.service

import android.app.IntentService
import android.content.Intent
import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.dev.quatrin.inventarioagro.data.api.RetrofitClient
import br.dev.quatrin.inventarioagro.data.database.AppDatabase
import br.dev.quatrin.inventarioagro.repository.TipoRepository
import br.dev.quatrin.inventarioagro.repository.MarcaRepository
import br.dev.quatrin.inventarioagro.repository.MaquinaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SyncService : IntentService("SyncService") {

    companion object {
        const val ACTION_FETCH_ALL = "action_fetch_all"
        const val ACTION_SYNC_OFFLINE = "action_sync_offline"

        const val BROADCAST_SYNC_COMPLETE = "sync_complete"
        const val BROADCAST_SYNC_ERROR = "sync_error"
        const val EXTRA_MESSAGE = "message"

        fun startFetchAll(context: Context) {
            val intent = Intent(context, SyncService::class.java).apply {
                action = ACTION_FETCH_ALL
            }
            context.startService(intent)
        }

        fun startSyncOffline(context: Context) {
            val intent = Intent(context, SyncService::class.java).apply {
                action = ACTION_SYNC_OFFLINE
            }
            context.startService(intent)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        intent?.let {
            when (it.action) {
                ACTION_FETCH_ALL -> handleFetchAll()
                ACTION_SYNC_OFFLINE -> handleSyncOffline()
            }
        }
    }

    private fun handleFetchAll() {
        val database = AppDatabase.getDatabase(applicationContext)
        val apiService = RetrofitClient.apiService

        val tipoRepository = TipoRepository(database.tipoMaquinaDao(), apiService)
        val marcaRepository = MarcaRepository(database.marcaDao(), apiService)
        val maquinaRepository = MaquinaRepository(database.maquinaDao(), apiService)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Buscar todos os dados do servidor
                println("=== INICIANDO SINCRONIZAÇÃO ===")

                val tiposResult = tipoRepository.buscarTiposDoServidor()
                println("Tipos Result: ${tiposResult.isSuccess} - ${tiposResult.getOrNull()?.size ?: 0} items")

                val marcasResult = marcaRepository.buscarMarcasDoServidor()
                println("Marcas Result: ${marcasResult.isSuccess} - ${marcasResult.getOrNull()?.size ?: 0} items")
                if (marcasResult.isFailure) {
                    println("Marcas Error: ${marcasResult.exceptionOrNull()?.message}")
                }

                val maquinasResult = maquinaRepository.buscarMaquinasDoServidor()
                println("Maquinas Result: ${maquinasResult.isSuccess} - ${maquinasResult.getOrNull()?.size ?: 0} items")
                if (maquinasResult.isFailure) {
                    println("Maquinas Error: ${maquinasResult.exceptionOrNull()?.message}")
                }

                val message = when {
                    tiposResult.isSuccess && marcasResult.isSuccess && maquinasResult.isSuccess ->
                        "Todos os dados foram sincronizados com sucesso (${tiposResult.getOrNull()?.size} tipos, ${marcasResult.getOrNull()?.size} marcas, ${maquinasResult.getOrNull()?.size} máquinas)"

                    else -> {
                        val erros = mutableListOf<String>()
                        if (tiposResult.isFailure) erros.add("tipos: ${tiposResult.exceptionOrNull()?.message}")
                        if (marcasResult.isFailure) erros.add("marcas: ${marcasResult.exceptionOrNull()?.message}")
                        if (maquinasResult.isFailure) erros.add("máquinas: ${maquinasResult.exceptionOrNull()?.message}")
                        "Erro na sincronização: ${erros.joinToString(", ")}"
                    }
                }

                sendBroadcast(BROADCAST_SYNC_COMPLETE, message)

            } catch (e: Exception) {
                println("Erro geral na sincronização: ${e.message}")
                e.printStackTrace()
                sendBroadcast(BROADCAST_SYNC_ERROR, "Erro na sincronização: ${e.message}")
            }
        }
    }

    private fun handleSyncOffline() {
        val message = "Sincronização offline em desenvolvimento"
        sendBroadcast(BROADCAST_SYNC_COMPLETE, message)
    }

    private fun sendBroadcast(action: String, message: String) {
        val intent = Intent(action).apply {
            putExtra(EXTRA_MESSAGE, message)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
