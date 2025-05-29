package br.dev.quatrin.inventarioagro.ui.tipo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import br.dev.quatrin.inventarioagro.R
import br.dev.quatrin.inventarioagro.databinding.ActivityTipoListBinding
import br.dev.quatrin.inventarioagro.databinding.DialogAddEditTipoBinding
import br.dev.quatrin.inventarioagro.util.NetworkUtils
import br.dev.quatrin.inventarioagro.viewmodel.TipoViewModel
import com.google.android.material.snackbar.Snackbar

class TipoListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTipoListBinding
    private lateinit var viewModel: TipoViewModel
    private lateinit var adapter: TipoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewModel = ViewModelProvider(this).get(TipoViewModel::class.java)

        setupRecyclerView()
        setupObservers()
        setupListeners()

        // Sincronizar com o servidor quando a tela é criada
        autoSyncData()
    }

    private fun autoSyncData() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            binding.progressBar.visibility = View.VISIBLE
            viewModel.sincronizarTipos()
        } else {
            Toast.makeText(this, "Sem conexão de rede - dados locais", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = TipoAdapter { tipo, action ->
            when (action) {
                TipoAdapter.ACTION_EDIT -> showEditDialog(tipo.id, tipo.descricao)
                TipoAdapter.ACTION_DELETE -> confirmarExclusao(tipo.id, tipo.descricao)
            }
        }

        binding.recyclerViewTipos.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTipos.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.todosTipos.observe(this) { tipos ->
            binding.progressBar.visibility = View.GONE

            if (tipos.isEmpty()) {
                binding.recyclerViewTipos.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            } else {
                binding.recyclerViewTipos.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE
                adapter.submitList(tipos)
            }
        }
    }

    private fun setupListeners() {
        binding.fabAddTipo.setOnClickListener {
            showAddDialog()
        }
    }

    private fun showAddDialog() {
        val dialogBinding = DialogAddEditTipoBinding.inflate(layoutInflater)

        AlertDialog.Builder(this)
            .setTitle(R.string.adicionar)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.salvar) { _, _ ->
                val descricao = dialogBinding.editTextDescricao.text.toString()
                viewModel.criarTipo(descricao) { success, message ->
                    if (success) {
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .create()
            .show()
    }

    private fun showEditDialog(id: Long, descricaoAtual: String) {
        val dialogBinding = DialogAddEditTipoBinding.inflate(layoutInflater)
        dialogBinding.editTextDescricao.setText(descricaoAtual)

        AlertDialog.Builder(this)
            .setTitle(R.string.editar)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.salvar) { _, _ ->
                val descricao = dialogBinding.editTextDescricao.text.toString()
                viewModel.atualizarTipo(id, descricao) { success, message ->
                    if (success) {
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .create()
            .show()
    }

    private fun confirmarExclusao(id: Long, descricao: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.excluir)
            .setMessage("Deseja realmente excluir o tipo '$descricao'?")
            .setPositiveButton(R.string.excluir) { _, _ ->
                viewModel.excluirTipo(id) { success, message ->
                    if (success) {
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .create()
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
