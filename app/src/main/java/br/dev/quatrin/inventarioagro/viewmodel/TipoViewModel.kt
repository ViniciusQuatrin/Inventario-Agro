package br.dev.quatrin.inventarioagro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import br.dev.quatrin.inventarioagro.InventarioAgroApplication
import br.dev.quatrin.inventarioagro.data.api.RetrofitClient
import br.dev.quatrin.inventarioagro.data.model.TipoMaquina
import br.dev.quatrin.inventarioagro.repository.TipoRepository
import kotlinx.coroutines.launch

class TipoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TipoRepository

    val todosTipos: LiveData<List<TipoMaquina>>

    init {
        val database = (application as InventarioAgroApplication).database
        repository = TipoRepository(database.tipoMaquinaDao(), RetrofitClient.apiService)
        todosTipos = repository.buscarTodos()
    }

    fun sincronizarTipos() {
        viewModelScope.launch {
            repository.sincronizarTipos()
        }
    }

    fun criarTipo(descricao: String, callback: (Boolean, String) -> Unit) {
        if (descricao.isBlank()) {
            callback(false, "A descrição não pode estar vazia")
            return
        }

        viewModelScope.launch {
            val tipo = TipoMaquina(descricao = descricao)
            try {
                val result = repository.criarTipo(tipo)
                if (result.isSuccess) {
                    callback(true, "Tipo criado com sucesso")
                } else {
                    callback(false, result.exceptionOrNull()?.message ?: "Erro ao criar tipo")
                }
            } catch (e: Exception) {
                callback(false, "Erro: ${e.message}")
            }
        }
    }

    fun atualizarTipo(id: Long, descricao: String, callback: (Boolean, String) -> Unit) {
        if (descricao.isBlank()) {
            callback(false, "A descrição não pode estar vazia")
            return
        }

        viewModelScope.launch {
            try {
                val tipoExistente = repository.buscarPorId(id)
                if (tipoExistente != null) {
                    val tipo = TipoMaquina(id = id, descricao = descricao)
                    val result = repository.atualizarTipo(tipo)
                    if (result.isSuccess) {
                        callback(true, "Tipo atualizado com sucesso")
                    } else {
                        callback(
                            false,
                            result.exceptionOrNull()?.message ?: "Erro ao atualizar tipo"
                        )
                    }
                } else {
                    callback(false, "Tipo não encontrado")
                }
            } catch (e: Exception) {
                callback(false, "Erro: ${e.message}")
            }
        }
    }

    fun excluirTipo(id: Long, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val tipo = repository.buscarPorId(id)
                if (tipo != null) {
                    val temMaquinas = repository.temMaquinasAssociadas(id)
                    if (temMaquinas) {
                        callback(
                            false,
                            "Este tipo não pode ser excluído pois existem máquinas associadas a ele"
                        )
                        return@launch
                    }

                    val result = repository.excluirTipo(tipo)
                    if (result.isSuccess) {
                        callback(true, "Tipo excluído com sucesso")
                    } else {
                        callback(false, result.exceptionOrNull()?.message ?: "Erro ao excluir tipo")
                    }
                } else {
                    callback(false, "Tipo não encontrado")
                }
            } catch (e: Exception) {
                callback(false, "Erro: ${e.message}")
            }
        }
    }
}