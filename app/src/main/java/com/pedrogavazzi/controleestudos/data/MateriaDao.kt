package com.pedrogavazzi.controleestudos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MateriaDao {

    @Query("SELECT * FROM materias ORDER BY nome COLLATE NOCASE ASC")
    fun observarTodas(): Flow<List<Materia>>

    @Query("SELECT * FROM materias WHERE id = :id")
    fun observarPorId(id: Long): Flow<Materia?>

    @Query("SELECT * FROM materias WHERE id = :id")
    suspend fun buscarPorId(id: Long): Materia?

    @Query("SELECT * FROM materias")
    suspend fun buscarTodasSuspend(): List<Materia>

    @Insert
    suspend fun inserir(materia: Materia): Long

    @Update
    suspend fun atualizar(materia: Materia)

    @Delete
    suspend fun excluir(materia: Materia)
}
