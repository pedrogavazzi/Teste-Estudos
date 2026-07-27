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

    @Query("SELECT * FROM aulas WHERE id = :id")
    suspend fun buscarPorIdSuspend(id: Long): Aula?

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

    // As consultas abaixo atualizam só a coluna indicada — ao contrário de @Update (que
    // reescreve a linha inteira a partir do objeto Aula em memória), essas não têm como
    // afetar nenhum outro campo por engano, nem por um objeto desatualizado na tela nem por
    // qualquer outro motivo. Usadas pelas telas que editam só um campo de cada vez.
    @Query("UPDATE aulas SET anotacoesCaderno = :anotacoes WHERE id = :aulaId")
    suspend fun atualizarAnotacaoCaderno(aulaId: Long, anotacoes: String)

    @Query("UPDATE aulas SET observacao = :observacao WHERE id = :aulaId")
    suspend fun atualizarObservacao(aulaId: Long, observacao: String)

    @Query("UPDATE aulas SET link = :link WHERE id = :aulaId")
    suspend fun atualizarLink(aulaId: Long, link: String)

    @Query("UPDATE aulas SET favorita = :favorita WHERE id = :aulaId")
    suspend fun atualizarFavorita(aulaId: Long, favorita: Boolean)

    @Query("UPDATE aulas SET nomePersonalizado = :nome WHERE id = :aulaId")
    suspend fun atualizarNomePersonalizado(aulaId: Long, nome: String?)

    @Delete
    suspend fun excluir(aula: Aula)

    @Query("DELETE FROM aulas WHERE materiaId = :materiaId AND numero > :maxNumero")
    suspend fun removerAulasAcimaDoNumero(materiaId: Long, maxNumero: Int)
}
