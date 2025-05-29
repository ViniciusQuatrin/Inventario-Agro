package br.dev.quatrin.inventarioagro.ui.tipo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.dev.quatrin.inventarioagro.data.model.TipoMaquina
import br.dev.quatrin.inventarioagro.databinding.ItemTipoBinding

class TipoAdapter(private val onTipoClick: (TipoMaquina, Int) -> Unit) :
    ListAdapter<TipoMaquina, TipoAdapter.TipoViewHolder>(TipoDiffCallback()) {

    companion object {
        const val ACTION_EDIT = 1
        const val ACTION_DELETE = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipoViewHolder {
        val binding = ItemTipoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TipoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TipoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TipoViewHolder(private val binding: ItemTipoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tipo: TipoMaquina) {
            binding.textViewTipoDescricao.text = tipo.descricao

            binding.buttonEdit.setOnClickListener {
                onTipoClick(tipo, ACTION_EDIT)
            }

            binding.buttonDelete.setOnClickListener {
                onTipoClick(tipo, ACTION_DELETE)
            }
        }
    }
}

class TipoDiffCallback : DiffUtil.ItemCallback<TipoMaquina>() {
    override fun areItemsTheSame(oldItem: TipoMaquina, newItem: TipoMaquina): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TipoMaquina, newItem: TipoMaquina): Boolean {
        return oldItem == newItem
    }
}