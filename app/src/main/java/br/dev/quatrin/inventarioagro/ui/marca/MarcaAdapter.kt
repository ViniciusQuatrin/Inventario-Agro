package br.dev.quatrin.inventarioagro.ui.marca

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.dev.quatrin.inventarioagro.R
import br.dev.quatrin.inventarioagro.data.model.Marca

class MarcaAdapter(private val onMarcaClick: (Marca, Int) -> Unit) :
    ListAdapter<Marca, MarcaAdapter.MarcaViewHolder>(MarcaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarcaViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_marca, parent, false)
        return MarcaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MarcaViewHolder, position: Int) {
        val marca = getItem(position)
        holder.bind(marca)

        holder.btnEditar.setOnClickListener {
            onMarcaClick(marca, ACAO_EDITAR)
        }

        holder.btnExcluir.setOnClickListener {
            onMarcaClick(marca, ACAO_EXCLUIR)
        }
    }

    class MarcaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtNome: TextView = itemView.findViewById(R.id.txt_nome)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btn_editar)
        val btnExcluir: ImageButton = itemView.findViewById(R.id.btn_excluir)

        fun bind(marca: Marca) {
            txtNome.text = marca.nome
        }
    }

    companion object {
        const val ACAO_EDITAR = 1
        const val ACAO_EXCLUIR = 2
    }
}

class MarcaDiffCallback : DiffUtil.ItemCallback<Marca>() {
    override fun areItemsTheSame(oldItem: Marca, newItem: Marca): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Marca, newItem: Marca): Boolean {
        return oldItem == newItem
    }
}