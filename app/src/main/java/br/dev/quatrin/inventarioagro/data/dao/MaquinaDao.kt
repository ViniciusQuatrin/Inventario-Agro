package br.dev.quatrin.inventarioagro.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.dev.quatrin.inventarioagro.data.model.Maquina
import br.dev.quatrin.inventarioagro.data.model.StatusMaquina

@Dao
interface MaquinaDao {
    @Insert
    suspend fun inserir(maquina: Maquina): Long
    
    @Update
    suspend fun atualizar(maquina: Maquina)
    
    @Delete
    suspend fun excluir(maquina: Maquina)
    
    @Query("SELECT * FROM maquinas ORDER BY dataInclusao DESC")
    fun buscarTodas(): LiveData<List<Maquina>>
    
    @Query("SELECT * FROM maquinas WHERE id = :id")
    suspend fun buscarPorId(id: Long): Maquina?
    
    // Filtros
    @Query("SELECT * FROM maquinas WHERE (:tipoId IS NULL OR idTipo = :tipoId) " +
           "AND (:marcaId IS NULL OR idMarca = :marcaId) " +
           "AND (:valorMinimo IS NULL OR valor >= :valorMinimo) " +
           "AND (:valorMaximo IS NULL OR valor <= :valorMaximo) " +
           "AND (:status IS NULL OR status = :status) " +
           "ORDER BY dataInclusao DESC")
    fun buscarComFiltros(
        tipoId: Long? = null,
        marcaId: Long? = null,
        valorMinimo: Double? = null,
        valorMaximo: Double? = null,
        status: StatusMaquina? = null
    ): LiveData<List<Maquina>>

    @Query("DELETE FROM maquinas")
    suspend fun excluirTodas()
}
