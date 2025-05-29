package br.dev.quatrin.inventarioagro.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import br.dev.quatrin.inventarioagro.data.model.Marca

@Dao
interface MarcaDao {
    @Insert
    suspend fun inserir(marca: Marca): Long
    
    @Update
    suspend fun atualizar(marca: Marca)
    
    @Delete
    suspend fun excluir(marca: Marca)
    
    @Query("SELECT * FROM marcas ORDER BY nome")
    fun buscarTodas(): LiveData<List<Marca>>
    
    @Query("SELECT * FROM marcas WHERE id = :id")
    suspend fun buscarPorId(id: Long): Marca?
    
    @Query("SELECT COUNT(id) FROM maquinas WHERE idMarca = :marcaId")
    suspend fun contarMaquinasComMarca(marcaId: Long): Int

    @Query("DELETE FROM marcas")
    suspend fun excluirTodas()
}
