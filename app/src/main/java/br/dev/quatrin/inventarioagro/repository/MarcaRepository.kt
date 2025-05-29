package br.dev.quatrin.inventarioagro.repository

import androidx.lifecycle.LiveData
import br.dev.quatrin.inventarioagro.data.api.ApiService
import br.dev.quatrin.inventarioagro.data.dao.MarcaDao
import br.dev.quatrin.inventarioagro.data.model.Marca
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MarcaRepository(
    private val marcaDao: MarcaDao,
    private val apiService: ApiService
) {

    suspend fun inserir(marca: Marca) = marcaDao.inserir(marca)

    suspend fun atualizar(marca: Marca) = marcaDao.atualizar(marca)

    suspend fun excluir(marca: Marca) = marcaDao.excluir(marca)

    fun buscarTodas(): LiveData<List<Marca>> = marcaDao.buscarTodas()

    suspend fun buscarPorId(id: Long): Marca? = marcaDao.buscarPorId(id)

    suspend fun temMaquinasAssociadas(id: Long): Boolean =
        marcaDao.contarMaquinasComMarca(id) > 0

    suspend fun buscarMarcasDoServidor(): Result<List<Marca>> = withContext(Dispatchers.IO) {
        try {
            println("MarcaRepository: Fazendo chamada para getMarcas()...")
            val response = apiService.getMarcas()
            println("MarcaRepository: Response code: ${response.code()}")
            println("MarcaRepository: Response success: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val marcas = response.body() ?: emptyList()
                println("MarcaRepository: ${marcas.size} marcas recebidas do servidor")
                marcas.forEach { marca ->
                    println("MarcaRepository: Processando marca: ${marca.nome} (ID: ${marca.id})")
                }

                // Atualizar ou inserir cada marca individualmente
                marcas.forEach { marca ->
                    val marcaExistente = marcaDao.buscarPorId(marca.id)
                    if (marcaExistente == null) {
                        println("MarcaRepository: Inserindo nova marca: ${marca.nome}")
                        marcaDao.inserir(marca)
                    } else {
                        println("MarcaRepository: Atualizando marca existente: ${marca.nome}")
                        marcaDao.atualizar(marca)
                    }
                }
                println("MarcaRepository: Sincronização concluída com sucesso")
                Result.success(marcas)
            } else {
                val error = "Erro na resposta: ${response.code()}"
                println("MarcaRepository: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            println("MarcaRepository: Exception durante sincronização: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun criarMarca(marca: Marca): Result<Marca> = withContext(Dispatchers.IO) {
        return@withContext try {
            // SEMPRE salvar localmente PRIMEIRO (offline-first)
            val marcaLocal = marca.copy(id = 0) // ID será gerado pelo SQLite
            val novaId = marcaDao.inserir(marcaLocal)
            val marcaSalva = marcaLocal.copy(id = novaId)

            // Retornar sucesso imediatamente com dados locais
            val result = Result.success(marcaSalva)

            // Tentar sincronizar com servidor em background (não bloqueia UI)
            try {
                val response =
                    apiService.createMarca(marcaSalva.copy(id = 0)) // Remove ID para server
                if (response.isSuccessful) {
                    val marcaServidor = response.body()
                    if (marcaServidor != null) {
                        // Atualizar local com ID do servidor
                        marcaDao.atualizar(marcaSalva.copy(id = marcaServidor.id))
                        println("Marca sincronizada com servidor: ID ${marcaServidor.id}")
                    }
                } else {
                    println("Erro ao sincronizar com servidor (${response.code()}), mas marca salva localmente")
                }
            } catch (e: Exception) {
                println("Erro de rede ao sincronizar, mas marca salva localmente: ${e.message}")
            }

            result
        } catch (e: Exception) {
            println("Erro ao salvar marca localmente: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun atualizarMarca(marca: Marca): Result<Marca> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateMarca(marca.id, marca)
            if (response.isSuccessful) {
                val marcaAtualizada = response.body()!!
                marcaDao.atualizar(marcaAtualizada)
                Result.success(marcaAtualizada)
            } else {
                Result.failure(Exception("Erro ao atualizar marca: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Salvar localmente para sincronização posterior
            marcaDao.atualizar(marca)
            Result.success(marca)
        }
    }

    suspend fun excluirMarca(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteMarca(id)
            if (response.isSuccessful) {
                val marca = marcaDao.buscarPorId(id)
                marca?.let { marcaDao.excluir(it) }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro ao excluir marca: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
