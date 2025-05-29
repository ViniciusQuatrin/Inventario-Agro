package br.dev.quatrin.inventarioagro.repository

import androidx.lifecycle.LiveData
import br.dev.quatrin.inventarioagro.data.api.ApiService
import br.dev.quatrin.inventarioagro.data.dao.TipoMaquinaDao
import br.dev.quatrin.inventarioagro.data.model.TipoMaquina
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TipoRepository(
    private val tipoMaquinaDao: TipoMaquinaDao,
    private val apiService: ApiService
) {
    // Operações locais
    suspend fun inserir(tipo: TipoMaquina) = tipoMaquinaDao.inserir(tipo)

    suspend fun atualizar(tipo: TipoMaquina) = tipoMaquinaDao.atualizar(tipo)

    suspend fun excluir(tipo: TipoMaquina) = tipoMaquinaDao.excluir(tipo)

    fun buscarTodos(): LiveData<List<TipoMaquina>> = tipoMaquinaDao.buscarTodos()

    suspend fun buscarPorId(id: Long): TipoMaquina? = tipoMaquinaDao.buscarPorId(id)

    suspend fun temMaquinasAssociadas(id: Long): Boolean =
        tipoMaquinaDao.contarMaquinasComTipo(id) > 0

    // Operações com a API
    suspend fun sincronizarTipos() = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTipos()
            if (response.isSuccessful) {
                response.body()?.let { tipos ->
                    tipos.forEach { tipo ->
                        withContext(Dispatchers.IO) {
                            val tipoExistente = buscarPorId(tipo.id)
                            if (tipoExistente == null) {
                                inserir(tipo)
                            } else {
                                atualizar(tipo)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Tratar erro de conexão
            e.printStackTrace()
        }
    }

    suspend fun criarTipo(tipo: TipoMaquina): Result<TipoMaquina> = withContext(Dispatchers.IO) {
        return@withContext try {
            // SEMPRE salvar localmente PRIMEIRO (offline-first)
            val tipoLocal = tipo.copy(id = 0) // ID será gerado pelo SQLite
            val novaId = inserir(tipoLocal)
            val tipoSalvo = tipoLocal.copy(id = novaId)

            // Retornar sucesso imediatamente com dados locais
            val result = Result.success(tipoSalvo)

            // Tentar sincronizar com servidor em background (não bloqueia UI)
            try {
                val response =
                    apiService.createTipo(tipoSalvo.copy(id = 0)) // Remove ID para server
                if (response.isSuccessful) {
                    val tipoServidor = response.body()
                    if (tipoServidor != null) {
                        // Atualizar local com ID do servidor
                        atualizar(tipoSalvo.copy(id = tipoServidor.id))
                        println("Tipo sincronizado com servidor: ID ${tipoServidor.id}")
                    }
                } else {
                    println("Erro ao sincronizar com servidor (${response.code()}), mas tipo salvo localmente")
                }
            } catch (e: Exception) {
                println("Erro de rede ao sincronizar, mas tipo salvo localmente: ${e.message}")
            }

            result
        } catch (e: Exception) {
            println("Erro ao salvar tipo localmente: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun atualizarTipo(tipo: TipoMaquina): Result<TipoMaquina> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateTipo(tipo.id, tipo)
            if (response.isSuccessful) {
                response.body()?.let { tipoAtualizado ->
                    atualizar(tipoAtualizado)
                    Result.success(tipoAtualizado)
                } ?: Result.failure(RuntimeException("Resposta nula do servidor"))
            } else {
                Result.failure(RuntimeException("Erro ao atualizar tipo: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buscarTiposDoServidor(): Result<List<TipoMaquina>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTipos()
            if (response.isSuccessful) {
                val tipos = response.body() ?: emptyList()
                // Atualizar ou inserir cada tipo individualmente
                tipos.forEach { tipo ->
                    val tipoExistente = buscarPorId(tipo.id)
                    if (tipoExistente == null) {
                        inserir(tipo)
                    } else {
                        atualizar(tipo)
                    }
                }
                Result.success(tipos)
            } else {
                Result.failure(Exception("Erro na resposta: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun excluirTipo(tipo: TipoMaquina): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteTipo(tipo.id)
            if (response.isSuccessful) {
                excluir(tipo)
                Result.success(Unit)
            } else {
                Result.failure(RuntimeException("Erro ao excluir tipo: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
