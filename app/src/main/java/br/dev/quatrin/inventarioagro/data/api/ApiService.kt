package br.dev.quatrin.inventarioagro.data.api

import br.dev.quatrin.inventarioagro.data.model.Maquina
import br.dev.quatrin.inventarioagro.data.model.Marca
import br.dev.quatrin.inventarioagro.data.model.TipoMaquina
import br.dev.quatrin.inventarioagro.data.model.StatusMaquina
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("ws/tipo")
    suspend fun getTipos(): Response<List<TipoMaquina>>
    
    @GET("ws/tipo/{id}")
    suspend fun getTipo(@Path("id") id: Long): Response<TipoMaquina>
    
    @POST("ws/tipo")
    suspend fun createTipo(@Body tipo: TipoMaquina): Response<TipoMaquina>
    
    @PUT("ws/tipo/{id}")
    suspend fun updateTipo(@Path("id") id: Long, @Body tipo: TipoMaquina): Response<TipoMaquina>
    
    @DELETE("ws/tipo/{id}")
    suspend fun deleteTipo(@Path("id") id: Long): Response<Void>
    
    // Endpoints para Marca
    @GET("ws/marca")
    suspend fun getMarcas(): Response<List<Marca>>
    
    @GET("ws/marca/{id}")
    suspend fun getMarca(@Path("id") id: Long): Response<Marca>
    
    @POST("ws/marca")
    suspend fun createMarca(@Body marca: Marca): Response<Marca>
    
    @PUT("ws/marca/{id}")
    suspend fun updateMarca(@Path("id") id: Long, @Body marca: Marca): Response<Marca>
    
    @DELETE("ws/marca/{id}")
    suspend fun deleteMarca(@Path("id") id: Long): Response<Void>

    @GET("ws/maquina")
    suspend fun getMaquinas(
        @Query("valorDe") valorDe: Double? = null,
        @Query("valorAte") valorAte: Double? = null,
        @Query("status") status: String? = null,
        @Query("idTipo") idTipo: Long? = null,
        @Query("idMarca") idMarca: Long? = null
    ): Response<List<Maquina>>
    
    @GET("ws/maquina/{id}")
    suspend fun getMaquina(@Path("id") id: Long): Response<Maquina>
    
    @POST("ws/maquina")
    suspend fun createMaquina(@Body maquina: Maquina): Response<Maquina>
    
    @PUT("ws/maquina/{id}")
    suspend fun updateMaquina(@Path("id") id: Long, @Body maquina: Maquina): Response<Maquina>
    
    @DELETE("ws/maquina/{id}")
    suspend fun deleteMaquina(@Path("id") id: Long): Response<Void>
}

