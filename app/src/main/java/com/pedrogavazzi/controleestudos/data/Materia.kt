package com.pedrogavazzi.controleestudos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Uma matéria/disciplina cadastrada pelo usuário (ex: "Contabilidade Avançada"),
 * com o número total de aulas planejadas para ela.
 */
@Entity(tableName = "materias")
data class Materia(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nome: String,
    val totalAulas: Int,
    val corHex: String = "#6750A4",
    val criadoEmMillis: Long = System.currentTimeMillis()
)
