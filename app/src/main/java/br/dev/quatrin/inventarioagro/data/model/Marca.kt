package br.dev.quatrin.inventarioagro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "marcas")
data class Marca(
    @PrimaryKey
    @SerializedName("id")
    val id: Long = 0,
    
    @SerializedName("nome")
    val nome: String
)