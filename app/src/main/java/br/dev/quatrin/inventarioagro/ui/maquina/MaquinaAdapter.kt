package br.dev.quatrin.inventarioagro.ui.maquina

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.dev.quatrin.inventarioagro.R
import br.dev.quatrin.inventarioagro.data.model.Maquina
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class MaquinaAdapter(private val onMaquinaClick: (Maquina, Int) -> Unit) :
    ListAdapter<Maquina, MaquinaAdapter.MaquinaViewHolder>(MaquinaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaquinaViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_maquina, parent, false)
        return MaquinaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MaquinaViewHolder, position: Int) {
        val maquina = getItem(position)
        holder.bind(maquina)

        holder.btnEditar.setOnClickListener {
            onMaquinaClick(maquina, ACAO_EDITAR)
        }

        holder.btnExcluir.setOnClickListener {
            onMaquinaClick(maquina, ACAO_EXCLUIR)
        }
    }

    class MaquinaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtDescricao: TextView = itemView.findViewById(R.id.txt_descricao)
        private val txtAnoValor: TextView = itemView.findViewById(R.id.txt_ano_valor)
        private val txtProprietario: TextView = itemView.findViewById(R.id.txt_proprietario)
        private val txtStatus: TextView = itemView.findViewById(R.id.txt_status)
        private val txtData: TextView = itemView.findViewById(R.id.txt_data)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btn_editar)
        val btnExcluir: ImageButton = itemView.findViewById(R.id.btn_excluir)

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        fun bind(maquina: Maquina) {
            txtDescricao.text = maquina.descricao
            txtAnoValor.text = "${maquina.anoFabricacao} - ${currencyFormat.format(maquina.valor)}"
            txtProprietario.text = maquina.nomeProprietario
            txtStatus.text = getStatusText(maquina.status)
            txtData.text = dateFormat.format(maquina.dataInclusao)

            // Colorir status
            val statusColor = when (maquina.status) {
                "D" -> android.R.color.holo_green_dark
                "N" -> android.R.color.holo_orange_dark
                "R" -> android.R.color.holo_blue_dark
                "V" -> android.R.color.darker_gray
                else -> android.R.color.black
            }
            txtStatus.setTextColor(itemView.context.getColor(statusColor))
        }

        private fun getStatusText(status: String): String {
            return when (status) {
                "D" -> "Disponível"
                "N" -> "Em Negociação"
                "R" -> "Reservada"
                "V" -> "Vendida"
                else -> "Desconhecido"
            }
        }
    }

    companion object {
        const val ACAO_EDITAR = 1
        const val ACAO_EXCLUIR = 2
    }
}

class MaquinaDiffCallback : DiffUtil.ItemCallback<Maquina>() {
    override fun areItemsTheSame(oldItem: Maquina, newItem: Maquina): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Maquina, newItem: Maquina): Boolean {
        return oldItem == newItem
    }
}