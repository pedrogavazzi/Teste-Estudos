package com.pedrogavazzi.controleestudos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AulaDao {

    @Query("SELECT * FROM aulas WHERE materiaId = :materiaId ORDER BY numero ASC")
    fun observarPorMateria(materiaId: Long): Flow<List<Aula>>

    @Query("SELECT * FROM aulas ORDER BY dataHoraMillis IS NULL, dataHoraMillis ASC")
    fun observarTodas(): Flow<List<Aula>>

    @Query("SELECT * FROM aulas WHERE id = :id")
    fun observarPorId(id: Long): Flow<Aula?>

    @Query("SELECT * FROM aulas WHERE materiaId = :materiaId ORDER BY numero DESC LIMIT 1")
    suspend fun ultimaAulaDaMateria(materiaId: Long): Aula?

    @Query("SELECT COUNT(*) FROM aulas WHERE materiaId = :materiaId")
    suspend fun contarAulasDaMateria(materiaId: Long): Int

    @Query("SELECT * FROM aulas WHERE materiaId = :materiaId ORDER BY numero ASC")
    suspend fun buscarTodasDaMateriaSuspend(materiaId: Long): List<Aula>

    @Query("SELECT * FROM aulas")
    suspend fun buscarTodasSuspend(): List<Aula>

    @Insert
    suspend fun inserir(aula: Aula): Long

    @Insert
    suspend fun inserirTodas(aulas: List<Aula>)

    @Update
    suspend fun atualizar(aula: Aula)

    @Delete
    suspend fun excluir(aula: Aula)

    @Query("DELETE FROM aulas WHERE materiaId = :materiaId AND numero > :maxNumero")
    suspend fun removerAulasAcimaDoNumero(materiaId: Long, maxNumero: Int)
}
