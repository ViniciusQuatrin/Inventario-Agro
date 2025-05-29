package br.dev.quatrin.inventarioagro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tipos_maquina")
data class TipoMaquina(
    @PrimaryKey
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("descricao")
    val descricao: String
)

