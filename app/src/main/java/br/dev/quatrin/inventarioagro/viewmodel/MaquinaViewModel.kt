package br.dev.quatrin.inventarioagro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import br.dev.quatrin.inventarioagro.data.api.RetrofitClient
import br.dev.quatrin.inventarioagro.data.database.AppDatabase
import br.dev.quatrin.inventarioagro.data.model.Maquina
import br.dev.quatrin.inventarioagro.data.model.StatusMaquina
import br.dev.quatrin.inventarioagro.repository.MaquinaRepository
import kotlinx.coroutines.launch
import java.util.Date

class MaquinaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MaquinaRepository

    private val _todasMaquinas = MutableLiveData<LiveData<List<Maquina>>>()
    val todasMaquinas: LiveData<LiveData<List<Maquina>>> = _todasMaquinas

    private val _operacaoStatus = MutableLiveData<String>()
    val operacaoStatus: LiveData<String> = _operacaoStatus

    // Filtros
    private val _filtroTipo = MutableLiveData<Long?>()
    private val _filtroMarca = MutableLiveData<Long?>()
    private val _filtroValorMinimo = MutableLiveData<Double?>()
    private val _filtroValorMaximo = MutableLiveData<Double?>()
    private val _filtroStatus = MutableLiveData<StatusMaquina?>()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MaquinaRepository(database.maquinaDao(), RetrofitClient.apiService)

        // Inicializar com todas as máquinas
        atualizarListaFiltrada()
    }

    fun buscarMaquinasDoServidor() {
        viewModelScope.launch {
            try {
                val resultado = repository.buscarMaquinasDoServidor()
                if (resultado.isSuccess) {
                    _operacaoStatus.value = "Máquinas atualizadas com sucesso"
                    atualizarListaFiltrada()
                } else {
                    _operacaoStatus.value =
                        "Erro ao buscar máquinas: ${resultado.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _operacaoStatus.value = "Erro: ${e.message}"
            }
        }
    }

    fun criarMaquina(
        idTipo: Long,
        idMarca: Long,
        descricao: String,
        anoFabricacao: Int,
        valor: Double,
        nomeProprietario: String,
        contatoProprietario: String,
        percentualComissao: Double,
        status: String
    ) {
        if (descricao.isBlank() || nomeProprietario.isBlank()) {
            _operacaoStatus.value = "Preencha todos os campos obrigatórios"
            return
        }

        viewModelScope.launch {
            try {
                val maquina = Maquina(
                    idTipo = idTipo,
                    idMarca = idMarca,
                    descricao = descricao,
                    anoFabricacao = anoFabricacao,
                    valor = valor,
                    nomeProprietario = nomeProprietario,
                    contatoProprietario = contatoProprietario,
                    percentualComissao = percentualComissao,
                    status = status,
                    dataInclusao = Date()
                )

                val resultado = repository.criarMaquina(maquina)
                if (resultado.isSuccess) {
                    _operacaoStatus.value = "Máquina criada com sucesso"
                    atualizarListaFiltrada()
                } else {
                    _operacaoStatus.value =
                        "Erro ao criar máquina: ${resultado.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _operacaoStatus.value = "Erro: ${e.message}"
            }
        }
    }

    fun atualizarMaquina(
        id: Long,
        idTipo: Long,
        idMarca: Long,
        descricao: String,
        anoFabricacao: Int,
        valor: Double,
        nomeProprietario: String,
        contatoProprietario: String,
        percentualComissao: Double,
        status: String,
        dataInclusao: Date
    ) {
        if (descricao.isBlank() || nomeProprietario.isBlank()) {
            _operacaoStatus.value = "Preencha todos os campos obrigatórios"
            return
        }

        viewModelScope.launch {
            try {
                val maquina = Maquina(
                    id = id,
                    idTipo = idTipo,
                    idMarca = idMarca,
                    descricao = descricao,
                    anoFabricacao = anoFabricacao,
                    valor = valor,
                    nomeProprietario = nomeProprietario,
                    contatoProprietario = contatoProprietario,
                    percentualComissao = percentualComissao,
                    status = status,
                    dataInclusao = dataInclusao
                )

                val resultado = repository.atualizarMaquina(maquina)
                if (resultado.isSuccess) {
                    _operacaoStatus.value = "Máquina atualizada com sucesso"
                    atualizarListaFiltrada()
                } else {
                    _operacaoStatus.value =
                        "Erro ao atualizar máquina: ${resultado.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _operacaoStatus.value = "Erro: ${e.message}"
            }
        }
    }

    fun excluirMaquina(id: Long) {
        viewModelScope.launch {
            try {
                val resultado = repository.excluirMaquina(id)
                if (resultado.isSuccess) {
                    _operacaoStatus.value = "Máquina excluída com sucesso"
                    atualizarListaFiltrada()
                } else {
                    _operacaoStatus.value =
                        "Erro ao excluir máquina: ${resultado.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _operacaoStatus.value = "Erro: ${e.message}"
            }
        }
    }

    // Métodos de filtro
    fun aplicarFiltros(
        tipoId: Long? = null,
        marcaId: Long? = null,
        valorMinimo: Double? = null,
        valorMaximo: Double? = null,
        status: StatusMaquina? = null
    ) {
        _filtroTipo.value = tipoId
        _filtroMarca.value = marcaId
        _filtroValorMinimo.value = valorMinimo
        _filtroValorMaximo.value = valorMaximo
        _filtroStatus.value = status

        atualizarListaFiltrada()
    }

    fun limparFiltros() {
        _filtroTipo.value = null
        _filtroMarca.value = null
        _filtroValorMinimo.value = null
        _filtroValorMaximo.value = null
        _filtroStatus.value = null

        atualizarListaFiltrada()
    }

    private fun atualizarListaFiltrada() {
        val lista = repository.buscarComFiltros(
            _filtroTipo.value,
            _filtroMarca.value,
            _filtroValorMinimo.value,
            _filtroValorMaximo.value,
            _filtroStatus.value
        )
        _todasMaquinas.value = lista
    }

    fun limparStatus() {
        _operacaoStatus.value = ""
    }
}