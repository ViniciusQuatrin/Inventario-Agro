package br.dev.quatrin.inventarioagro.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import br.dev.quatrin.inventarioagro.data.database.Converters
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(
    tableName = "maquinas",
    indices = [
        Index("idTipo"),
        Index("idMarca")
    ],
    foreignKeys = [
        ForeignKey(
            entity = TipoMaquina::class,
            parentColumns = ["id"],
            childColumns = ["idTipo"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Marca::class,
            parentColumns = ["id"],
            childColumns = ["idMarca"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
@TypeConverters(Converters::class)
data class Maquina(
    @PrimaryKey
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("idTipo")
    val idTipo: Long,

    @SerializedName("idMarca")
    val idMarca: Long,

    @SerializedName("dataInclusao")
    val dataInclusao: Date = Date(),

    @SerializedName("descricao")
    val descricao: String,

    @SerializedName("anoFabricacao")
    val anoFabricacao: Int,

    @SerializedName("valor")
    val valor: Double,

    @SerializedName("nomeProprietario")
    val nomeProprietario: String,

    @SerializedName("contatoProprietario")
    val contatoProprietario: String,

    @SerializedName("percentualComissao")
    val percentualComissao: Double,

    @SerializedName("status")
    val status: String // "D", "N", "R", "V"
) {
    // Converter entre status String e enum
    fun getStatusEnum(): StatusMaquina {
        return when(status) {
            "D" -> StatusMaquina.DISPONIVEL
            "N" -> StatusMaquina.NEGOCIACAO
            "R" -> StatusMaquina.RESERVADA
            "V" -> StatusMaquina.VENDIDA
            else -> StatusMaquina.DISPONIVEL
        }
    }
}

