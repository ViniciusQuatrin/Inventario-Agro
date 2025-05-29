package br.dev.quatrin.inventarioagro.ui.maquina

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.dev.quatrin.inventarioagro.R
import br.dev.quatrin.inventarioagro.data.api.RetrofitClient
import br.dev.quatrin.inventarioagro.data.database.AppDatabase
import br.dev.quatrin.inventarioagro.data.model.Maquina
import br.dev.quatrin.inventarioagro.data.model.Marca
import br.dev.quatrin.inventarioagro.data.model.TipoMaquina
import br.dev.quatrin.inventarioagro.repository.MarcaRepository
import br.dev.quatrin.inventarioagro.repository.TipoRepository
import br.dev.quatrin.inventarioagro.util.NetworkUtils
import br.dev.quatrin.inventarioagro.viewmodel.MaquinaViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Date

class MaquinaListActivity : AppCompatActivity() {

    private val viewModel: MaquinaViewModel by viewModels()
    private lateinit var adapter: MaquinaAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton

    private lateinit var tipoRepository: TipoRepository
    private lateinit var marcaRepository: MarcaRepository
    private var tiposList = listOf<TipoMaquina>()
    private var marcasList = listOf<Marca>()
    private var filtroTipo: String? = null
    private var filtroMarca: String? = null
    private var filtroValorMin: Double? = null
    private var filtroValorMax: Double? = null
    private var filtroStatus: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maquina_list)

        supportActionBar?.apply {
            title = "Máquinas"
            setDisplayHomeAsUpEnabled(true)
        }

        initRepositories()
        initViews()
        setupRecyclerView()
        observeViewModel()
        loadTiposAndMarcas()

        // Sincronização automática ao abrir a tela
        autoSyncData()
    }

    private fun autoSyncData() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Sincronizando máquinas...", Toast.LENGTH_SHORT).show()
            viewModel.buscarMaquinasDoServidor()
        } else {
            Toast.makeText(this, "Sem conexão de rede - dados locais", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initRepositories() {
        val database = AppDatabase.getDatabase(this)
        val apiService = RetrofitClient.apiService
        tipoRepository = TipoRepository(database.tipoMaquinaDao(), apiService)
        marcaRepository = MarcaRepository(database.marcaDao(), apiService)
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_maquinas)
        fabAdd = findViewById(R.id.fab_add_maquina)

        fabAdd.setOnClickListener {
            showAddEditDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = MaquinaAdapter { maquina, acao ->
            when (acao) {
                MaquinaAdapter.ACAO_EDITAR -> {
                    showAddEditDialog(maquina)
                }
                MaquinaAdapter.ACAO_EXCLUIR -> {
                    showDeleteDialog(maquina)
                }
            }
        }

        recyclerView.apply {
            this.adapter = this@MaquinaListActivity.adapter
            layoutManager = LinearLayoutManager(this@MaquinaListActivity)
        }
    }

    private fun observeViewModel() {
        viewModel.todasMaquinas.observe(this) { liveDataMaquinas ->
            liveDataMaquinas?.observe(this) { maquinas ->
                adapter.submitList(maquinas)
            }
        }

        viewModel.operacaoStatus.observe(this) { status ->
            if (status.isNotEmpty()) {
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
                viewModel.limparStatus()
            }
        }
    }

    private fun loadTiposAndMarcas() {
        lifecycleScope.launch {
            // Carregar tipos
            tipoRepository.buscarTodos().observe(this@MaquinaListActivity) { tipos ->
                tiposList = tipos
            }

            // Carregar marcas
            marcaRepository.buscarTodas().observe(this@MaquinaListActivity) { marcas ->
                marcasList = marcas
            }
        }
    }

    private fun showAddEditDialog(maquina: Maquina? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_maquina, null)

        // Encontrar views
        val spinnerTipo = dialogView.findViewById<AutoCompleteTextView>(R.id.spinner_tipo)
        val spinnerMarca = dialogView.findViewById<AutoCompleteTextView>(R.id.spinner_marca)
        val spinnerStatus = dialogView.findViewById<AutoCompleteTextView>(R.id.spinner_status)
        val editDescricao = dialogView.findViewById<TextInputEditText>(R.id.edit_descricao)
        val editAno = dialogView.findViewById<TextInputEditText>(R.id.edit_ano)
        val editValor = dialogView.findViewById<TextInputEditText>(R.id.edit_valor)
        val editProprietario = dialogView.findViewById<TextInputEditText>(R.id.edit_proprietario)
        val editContato = dialogView.findViewById<TextInputEditText>(R.id.edit_contato)
        val editComissao = dialogView.findViewById<TextInputEditText>(R.id.edit_comissao)

        // Configurar spinners
        val tiposAdapter = ArrayAdapter(
            this, android.R.layout.simple_dropdown_item_1line,
            tiposList.map { it.descricao })
        spinnerTipo.setAdapter(tiposAdapter)

        val marcasAdapter = ArrayAdapter(
            this, android.R.layout.simple_dropdown_item_1line,
            marcasList.map { it.nome })
        spinnerMarca.setAdapter(marcasAdapter)

        val statusArray = arrayOf("Disponível", "Em Negociação", "Reservada", "Vendida")
        val statusAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusArray)
        spinnerStatus.setAdapter(statusAdapter)

        // Preencher campos se for edição
        maquina?.let {
            val tipoSelecionado = tiposList.find { tipo -> tipo.id == it.idTipo }
            val marcaSelecionada = marcasList.find { marca -> marca.id == it.idMarca }

            spinnerTipo.setText(tipoSelecionado?.descricao, false)
            spinnerMarca.setText(marcaSelecionada?.nome, false)
            editDescricao.setText(it.descricao)
            editAno.setText(it.anoFabricacao.toString())
            editValor.setText(it.valor.toString())
            editProprietario.setText(it.nomeProprietario)
            editContato.setText(it.contatoProprietario)
            editComissao.setText(it.percentualComissao.toString())

            val statusTexto = when (it.status) {
                "D" -> "Disponível"
                "N" -> "Em Negociação"
                "R" -> "Reservada"
                "V" -> "Vendida"
                else -> "Disponível"
            }
            spinnerStatus.setText(statusTexto, false)
        }

        AlertDialog.Builder(this)
            .setTitle(if (maquina == null) "Nova Máquina" else "Editar Máquina")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                salvarMaquina(
                    maquina,
                    spinnerTipo.text.toString(),
                    spinnerMarca.text.toString(),
                    editDescricao.text.toString(),
                    editAno.text.toString(),
                    editValor.text.toString(),
                    editProprietario.text.toString(),
                    editContato.text.toString(),
                    editComissao.text.toString(),
                    spinnerStatus.text.toString()
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun salvarMaquina(
        maquinaExistente: Maquina?,
        tipoTexto: String,
        marcaTexto: String,
        descricao: String,
        ano: String,
        valor: String,
        proprietario: String,
        contato: String,
        comissao: String,
        statusTexto: String
    ) {
        // Validações
        if (tipoTexto.isBlank() || marcaTexto.isBlank() || descricao.isBlank() ||
            ano.isBlank() || valor.isBlank() || proprietario.isBlank()
        ) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        // Encontrar IDs
        val tipoSelecionado = tiposList.find { it.descricao == tipoTexto }
        val marcaSelecionada = marcasList.find { it.nome == marcaTexto }

        if (tipoSelecionado == null || marcaSelecionada == null) {
            Toast.makeText(this, "Tipo ou Marca inválidos", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val anoInt = ano.toInt()
            val valorDouble = valor.replace(",", ".").toDouble()
            val comissaoDouble =
                if (comissao.isBlank()) 0.0 else comissao.replace(",", ".").toDouble()

            val statusCodigo = when (statusTexto) {
                "Disponível" -> "D"
                "Em Negociação" -> "N"
                "Reservada" -> "R"
                "Vendida" -> "V"
                else -> "D"
            }

            if (maquinaExistente == null) {
                // Nova máquina
                viewModel.criarMaquina(
                    tipoSelecionado.id,
                    marcaSelecionada.id,
                    descricao,
                    anoInt,
                    valorDouble,
                    proprietario,
                    contato,
                    comissaoDouble,
                    statusCodigo
                )
            } else {
                // Editar máquina
                viewModel.atualizarMaquina(
                    maquinaExistente.id,
                    tipoSelecionado.id,
                    marcaSelecionada.id,
                    descricao,
                    anoInt,
                    valorDouble,
                    proprietario,
                    contato,
                    comissaoDouble,
                    statusCodigo,
                    maquinaExistente.dataInclusao
                )
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Valores numéricos inválidos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteDialog(maquina: Maquina) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Deseja realmente excluir a máquina '${maquina.descricao}'?")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.excluirMaquina(maquina.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filtro_maquinas, null)

        // Encontrar views
        val spinnerTipo = dialogView.findViewById<AutoCompleteTextView>(R.id.filter_spinner_tipo)
        val spinnerMarca = dialogView.findViewById<AutoCompleteTextView>(R.id.filter_spinner_marca)
        val editValorMin = dialogView.findViewById<TextInputEditText>(R.id.filter_edit_valor_min)
        val editValorMax = dialogView.findViewById<TextInputEditText>(R.id.filter_edit_valor_max)
        val spinnerStatus =
            dialogView.findViewById<AutoCompleteTextView>(R.id.filter_spinner_status)

        // Configurar spinners
        val tiposAdapter = ArrayAdapter(
            this, android.R.layout.simple_dropdown_item_1line,
            listOf("Todos") + tiposList.map { it.descricao })
        spinnerTipo.setAdapter(tiposAdapter)

        val marcasAdapter = ArrayAdapter(
            this, android.R.layout.simple_dropdown_item_1line,
            listOf("Todos") + marcasList.map { it.nome })
        spinnerMarca.setAdapter(marcasAdapter)

        val statusArray = arrayOf("Todos", "Disponível", "Em Negociação", "Reservada", "Vendida")
        val statusAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusArray)
        spinnerStatus.setAdapter(statusAdapter)

        // Preencher campos com filtros atuais
        spinnerTipo.setText(filtroTipo ?: "Todos", false)
        spinnerMarca.setText(filtroMarca ?: "Todos", false)
        editValorMin.setText(filtroValorMin?.toString() ?: "")
        editValorMax.setText(filtroValorMax?.toString() ?: "")
        spinnerStatus.setText(filtroStatus ?: "Todos", false)

        AlertDialog.Builder(this)
            .setTitle("Filtrar Máquinas")
            .setView(dialogView)
            .setPositiveButton("Filtrar") { _, _ ->
                try {
                    // Processar filtros
                    val tipoTexto =
                        if (spinnerTipo.text.toString() == "Todos") null else spinnerTipo.text.toString()
                    val marcaTexto =
                        if (spinnerMarca.text.toString() == "Todos") null else spinnerMarca.text.toString()
                    val statusTexto =
                        if (spinnerStatus.text.toString() == "Todos") null else spinnerStatus.text.toString()

                    val valorMin = editValorMin.text?.toString()?.let {
                        if (it.isNotBlank()) it.replace(",", ".").toDoubleOrNull() else null
                    }
                    val valorMax = editValorMax.text?.toString()?.let {
                        if (it.isNotBlank()) it.replace(",", ".").toDoubleOrNull() else null
                    }

                    // Encontrar IDs
                    val tipoId = tipoTexto?.let { texto ->
                        tiposList.find { it.descricao == texto }?.id
                    }
                    val marcaId = marcaTexto?.let { texto ->
                        marcasList.find { it.nome == texto }?.id
                    }

                    // Converter status
                    val statusEnum = statusTexto?.let { texto ->
                        when (texto) {
                            "Disponível" -> br.dev.quatrin.inventarioagro.data.model.StatusMaquina.DISPONIVEL
                            "Em Negociação" -> br.dev.quatrin.inventarioagro.data.model.StatusMaquina.NEGOCIACAO
                            "Reservada" -> br.dev.quatrin.inventarioagro.data.model.StatusMaquina.RESERVADA
                            "Vendida" -> br.dev.quatrin.inventarioagro.data.model.StatusMaquina.VENDIDA
                            else -> null
                        }
                    }

                    // Aplicar filtros
                    viewModel.aplicarFiltros(tipoId, marcaId, valorMin, valorMax, statusEnum)

                } catch (e: Exception) {
                    Toast.makeText(this, "Erro nos filtros: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Limpar") { _, _ ->
                viewModel.limparFiltros()
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.maquina_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_sync -> {
                viewModel.buscarMaquinasDoServidor()
                true
            }
            R.id.action_filter -> {
                showFilterDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
