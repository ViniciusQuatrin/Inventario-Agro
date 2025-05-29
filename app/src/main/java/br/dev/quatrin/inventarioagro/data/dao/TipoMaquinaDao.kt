package br.dev.quatrin.inventarioagro.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import br.dev.quatrin.inventarioagro.data.model.TipoMaquina

@Dao
interface TipoMaquinaDao {
    @Insert
    suspend fun inserir(tipoMaquina: TipoMaquina): Long
    
    @Update
    suspend fun atualizar(tipoMaquina: TipoMaquina)
    
    @Delete
    suspend fun excluir(tipoMaquina: TipoMaquina)
    
    @Query("SELECT * FROM tipos_maquina ORDER BY descricao")
    fun buscarTodos(): LiveData<List<TipoMaquina>>
    
    @Query("SELECT * FROM tipos_maquina WHERE id = :id")
    suspend fun buscarPorId(id: Long): TipoMaquina?

    @Query("SELECT COUNT(id) FROM maquinas WHERE idTipo = :tipoId")
    suspend fun contarMaquinasComTipo(tipoId: Long): Int
}
