package br.dev.quatrin.inventarioagro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import br.dev.quatrin.inventarioagro.data.api.RetrofitClient
import br.dev.quatrin.inventarioagro.data.database.AppDatabase
import br.dev.quatrin.inventarioagro.data.model.Marca
import br.dev.quatrin.inventarioagro.repository.MarcaRepository
import kotlinx.coroutines.launch

class MarcaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MarcaRepository
    val todasMarcas: LiveData<List<Marca>>

    private val _operacaoStatus = MutableLiveData<String>()
    val operacaoStatus: LiveData<String> = _operacaoStatus

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MarcaRepository(database.marcaDao(), RetrofitClient.apiService)
        todasMarcas = repository.buscarTodas()
    }

    fun buscarMarcasDoServidor() {
        viewModelScope.launch {
            try {
                println("MarcaViewModel: Iniciando busca no servidor...")
                val resultado = repository.buscarMarcasDoServidor()
                if (resultado.isSuccess) {
                    val marcas = resultado.getOrNull() ?: emptyList()
                    println("MarcaViewModel: Sucesso! ${marcas.size} marcas carregadas")
                    _operacaoStatus.value = "Marcas atualizadas com sucesso (${marcas.size} marcas)"
                } else {
                    val erro = resultado.exceptionOrNull()?.message ?: "Erro desconhecido"
                    println("MarcaViewModel: Erro na busca: $erro")
                    _operacaoStatus.value = "Erro ao buscar marcas: $erro"
                }
            } catch (e: Exception) {
                println("MarcaViewModel: Exception: ${e.message}")
                e.printStackTrace()
                _operacaoStatus.value = "Erro: ${e.message}"
            }
        }
    }

    fun criarMarca(nome: String) {
        if (nome.isBlank()) {
            _operacaoStatus.value = "Nome da marca não pode estar vazio"
            return
        }

        viewModelScope.launch {
            try {
                val marca = Marca(nome = nome)
                val resultado = repository.criarMarca(marca)
                if (resultado.isSuccess) {
                    _operacaoStatus.value = "Marca criada com sucesso"
                } else {
                    _operacaoStatus.value =
                        "Erro ao criar marca: ${resultado.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _operacaoStatus.value = "Erro: ${e.message}"
            }
        }
    }

    fun atualizarMarca(id: Long, nome: String) {
        if (nome.isBlank()) {
            _operacaoStatus.value = "Nome da marca não pode estar vazio"
            return
        }

        viewModelScope.launch {
            try {
                val marca = Marca(id = id, nome = nome)
                val resultado = repository.atualizarMarca(marca)
                if (resultado.isSuccess) {
                    _operacaoStatus.value = "Marca atualizada com sucesso"
                } else {
                    _operacaoStatus.value =
                        "Erro ao atualizar marca: ${resultado.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _operacaoStatus.value = "Erro: ${e.message}"
            }
        }
    }

    fun excluirMarca(id: Long) {
        viewModelScope.launch {
            try {
                // Verificar se tem máquinas associadas
                val temMaquinas = repository.temMaquinasAssociadas(id)
                if (temMaquinas) {
                    _operacaoStatus.value = "Não é possível excluir marca com máquinas associadas"
                    return@launch
                }

                val resultado = repository.excluirMarca(id)
                if (resultado.isSuccess) {
                    _operacaoStatus.value = "Marca excluída com sucesso"
                } else {
                    _operacaoStatus.value =
                        "Erro ao excluir marca: ${resultado.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _operacaoStatus.value = "Erro: ${e.message}"
            }
        }
    }

    fun limparStatus() {
        _operacaoStatus.value = ""
    }
}
