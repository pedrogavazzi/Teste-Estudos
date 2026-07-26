package com.pedrogavazzi.controleestudos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FotoAulaDao {

    @Query("SELECT * FROM fotos_aula WHERE aulaId = :aulaId ORDER BY criadaEmMillis ASC")
    fun observarFotosDaAula(aulaId: Long): Flow<List<FotoAula>>

    @Query("SELECT * FROM fotos_aula WHERE aulaId = :aulaId ORDER BY criadaEmMillis ASC")
    suspend fun buscarFotosDaAulaSuspend(aulaId: Long): List<FotoAula>

    @Query("SELECT * FROM fotos_aula")
    fun observarTodas(): Flow<List<FotoAula>>

    @Insert
    suspend fun inserir(foto: FotoAula): Long

    @Delete
    suspend fun excluir(foto: FotoAula)

    @Query("DELETE FROM fotos_aula WHERE aulaId = :aulaId")
    suspend fun excluirTodasDaAula(aulaId: Long)
}
