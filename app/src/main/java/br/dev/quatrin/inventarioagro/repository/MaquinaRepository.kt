package br.dev.quatrin.inventarioagro.repository

import androidx.lifecycle.LiveData
import br.dev.quatrin.inventarioagro.data.api.ApiService
import br.dev.quatrin.inventarioagro.data.dao.MaquinaDao
import br.dev.quatrin.inventarioagro.data.model.Maquina
import br.dev.quatrin.inventarioagro.data.model.StatusMaquina
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MaquinaRepository(
    private val maquinaDao: MaquinaDao,
    private val apiService: ApiService
) {

    suspend fun inserir(maquina: Maquina) = maquinaDao.inserir(maquina)

    suspend fun atualizar(maquina: Maquina) = maquinaDao.atualizar(maquina)

    suspend fun excluir(maquina: Maquina) = maquinaDao.excluir(maquina)

    fun buscarTodas(): LiveData<List<Maquina>> = maquinaDao.buscarTodas()

    suspend fun buscarPorId(id: Long): Maquina? = maquinaDao.buscarPorId(id)

    fun buscarComFiltros(
        tipoId: Long? = null,
        marcaId: Long? = null,
        valorMinimo: Double? = null,
        valorMaximo: Double? = null,
        status: StatusMaquina? = null
    ): LiveData<List<Maquina>> =
        maquinaDao.buscarComFiltros(tipoId, marcaId, valorMinimo, valorMaximo, status)

    suspend fun buscarMaquinasDoServidor(
        valorDe: Double? = null,
        valorAte: Double? = null,
        status: String? = null,
        idTipo: Long? = null,
        idMarca: Long? = null
    ): Result<List<Maquina>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMaquinas(valorDe, valorAte, status, idTipo, idMarca)
            if (response.isSuccessful) {
                val maquinas = response.body() ?: emptyList()
                // Se nenhum filtro foi usado, limpar dados locais e inserir novos
                if (valorDe == null && valorAte == null && status == null && idTipo == null && idMarca == null) {
                    maquinaDao.excluirTodas()
                    maquinas.forEach { maquina ->
                        maquinaDao.inserir(maquina)
                    }
                }
                Result.success(maquinas)
            } else {
                Result.failure(Exception("Erro na resposta: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun criarMaquina(maquina: Maquina): Result<Maquina> = withContext(Dispatchers.IO) {
        return@withContext try {
            // SEMPRE salvar localmente PRIMEIRO (offline-first)
            val maquinaLocal = maquina.copy(id = 0) // ID será gerado pelo SQLite
            val novaId = maquinaDao.inserir(maquinaLocal)
            val maquinaSalva = maquinaLocal.copy(id = novaId)

            // Retornar sucesso imediatamente com dados locais
            val result = Result.success(maquinaSalva)

            // Tentar sincronizar com servidor em background (não bloqueia UI)
            try {
                val response =
                    apiService.createMaquina(maquinaSalva.copy(id = 0)) // Remove ID para server
                if (response.isSuccessful) {
                    val maquinaServidor = response.body()
                    if (maquinaServidor != null) {
                        // Atualizar local com ID do servidor
                        maquinaDao.atualizar(maquinaSalva.copy(id = maquinaServidor.id))
                        println("Máquina sincronizada com servidor: ID ${maquinaServidor.id}")
                    }
                } else {
                    println("Erro ao sincronizar com servidor (${response.code()}), mas máquina salva localmente")
                }
            } catch (e: Exception) {
                println("Erro de rede ao sincronizar, mas máquina salva localmente: ${e.message}")
            }

            result
        } catch (e: Exception) {
            println("Erro ao salvar máquina localmente: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun atualizarMaquina(maquina: Maquina): Result<Maquina> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateMaquina(maquina.id, maquina)
            if (response.isSuccessful) {
                val maquinaAtualizada = response.body()!!
                maquinaDao.atualizar(maquinaAtualizada)
                Result.success(maquinaAtualizada)
            } else {
                Result.failure(Exception("Erro ao atualizar máquina: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Salvar localmente para sincronização posterior
            maquinaDao.atualizar(maquina)
            Result.success(maquina)
        }
    }

    suspend fun excluirMaquina(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteMaquina(id)
            if (response.isSuccessful) {
                val maquina = maquinaDao.buscarPorId(id)
                maquina?.let { maquinaDao.excluir(it) }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro ao excluir máquina: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
