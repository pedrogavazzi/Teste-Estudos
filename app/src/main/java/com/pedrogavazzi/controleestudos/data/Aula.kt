package com.pedrogavazzi.controleestudos.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Status derivado de uma aula, calculado em tempo de exibição (não persistido). */
enum class StatusAula {
    NAO_AGENDADA,
    AGENDADA,
    ATRASADA,
    CONCLUIDA
}

/**
 * Uma aula individual de uma matéria (ex: "Aula 3 de Contabilidade Avançada"),
 * com data/horário próprios, observação livre, alerta e status de conclusão.
 */
@Entity(
    tableName = "aulas",
    foreignKeys = [
        ForeignKey(
            entity = Materia::class,
            parentColumns = ["id"],
            childColumns = ["materiaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("materiaId")]
)
data class Aula(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val materiaId: Long,
    val numero: Int,
    val dataHoraMillis: Long? = null,
    val concluida: Boolean = false,
    val concluidaEmMillis: Long? = null,
    val alertaAtivado: Boolean = true,
    val observacao: String = "",
    val vezesReagendada: Int = 0
)

fun Aula.statusAtual(agoraMillis: Long = System.currentTimeMillis()): StatusAula {
    return when {
        concluida -> StatusAula.CONCLUIDA
        dataHoraMillis == null -> StatusAula.NAO_AGENDADA
        dataHoraMillis < agoraMillis -> StatusAula.ATRASADA
        else -> StatusAula.AGENDADA
    }
}
