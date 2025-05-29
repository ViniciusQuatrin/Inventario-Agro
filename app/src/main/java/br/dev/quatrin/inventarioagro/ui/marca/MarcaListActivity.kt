package br.dev.quatrin.inventarioagro.ui.marca

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.dev.quatrin.inventarioagro.R
import br.dev.quatrin.inventarioagro.data.model.Marca
import br.dev.quatrin.inventarioagro.util.NetworkUtils
import br.dev.quatrin.inventarioagro.viewmodel.MarcaViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MarcaListActivity : AppCompatActivity() {

    private val viewModel: MarcaViewModel by viewModels()
    private lateinit var adapter: MarcaAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marca_list)

        supportActionBar?.apply {
            title = "Marcas"
            setDisplayHomeAsUpEnabled(true)
        }

        initViews()
        setupRecyclerView()
        observeViewModel()

        // Sincronização automática ao abrir a tela
        autoSyncData()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_marcas)
        fabAdd = findViewById(R.id.fab_add_marca)

        fabAdd.setOnClickListener {
            showAddEditDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = MarcaAdapter { marca, acao ->
            when (acao) {
                MarcaAdapter.ACAO_EDITAR -> {
                    showAddEditDialog(marca)
                }

                MarcaAdapter.ACAO_EXCLUIR -> {
                    showDeleteDialog(marca)
                }
            }
        }

        recyclerView.apply {
            this.adapter = this@MarcaListActivity.adapter
            layoutManager = LinearLayoutManager(this@MarcaListActivity)
        }
    }

    private fun observeViewModel() {
        viewModel.todasMarcas.observe(this) { marcas ->
            adapter.submitList(marcas)
        }

        viewModel.operacaoStatus.observe(this) { status ->
            if (status.isNotEmpty()) {
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
                viewModel.limparStatus()
            }
        }
    }

    private fun autoSyncData() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Sincronizando marcas...", Toast.LENGTH_SHORT).show()
            viewModel.buscarMarcasDoServidor()
        } else {
            Toast.makeText(this, "Sem conexão de rede - dados locais", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddEditDialog(marca: Marca? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_marca, null)
        val editNome = dialogView.findViewById<EditText>(R.id.edit_nome)

        if (marca != null) {
            editNome.setText(marca.nome)
        }

        AlertDialog.Builder(this)
            .setTitle(if (marca == null) "Nova Marca" else "Editar Marca")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val nome = editNome.text.toString().trim()
                if (marca == null) {
                    viewModel.criarMarca(nome)
                } else {
                    viewModel.atualizarMarca(marca.id, nome)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteDialog(marca: Marca) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Deseja realmente excluir a marca '${marca.nome}'?")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.excluirMarca(marca.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.marca_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.action_sync -> {
                Toast.makeText(this, "Iniciando sincronização de marcas...", Toast.LENGTH_SHORT)
                    .show()
                Log.d("MarcaListActivity", "Sincronizando marcas")
                viewModel.buscarMarcasDoServidor()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
