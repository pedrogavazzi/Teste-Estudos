package com.pedrogavazzi.controleestudos.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Calendar

/** Status derivado de uma aula, calculado em tempo de exibição (não persistido). */
enum class StatusAula {
    NAO_AGENDADA,
    AGENDADA,
    ATRASADA,
    CONCLUIDA
}

/** Forma como o alerta de uma aula deve notificar o usuário: com som ou apenas silenciosa. */
enum class TipoAlerta {
    COM_SOM,
    SEM_SOM
}

/**
 * Uma aula individual de uma matéria (ex: "Aula 3 de Contabilidade Avançada"),
 * com data/horário próprios, observação curta, anotações longas do caderno de aula,
 * alerta e status de conclusão.
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
    val tipoAlerta: TipoAlerta = TipoAlerta.COM_SOM,
    /** Observação curta sobre a aula (ex: lembrete rápido), editada na tela da matéria. */
    val observacao: String = "",
    /** Anotações longas feitas durante/depois da aula, editadas na aba Caderno. */
    val anotacoesCaderno: String = "",
    val vezesReagendada: Int = 0
)

/** Retorna o instante do fim (23:59:59.999) do dia em que `millis` cai. */
fun fimDoDiaMillis(millis: Long): Long {
    val calendario = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return calendario.timeInMillis
}

/**
 * Uma aula só é considerada atrasada depois que o dia inteiro agendado para ela já passou
 * sem que tenha sido concluída ou reagendada (reagendar move a data, então recomeça a contagem).
 */
fun Aula.statusAtual(agoraMillis: Long = System.currentTimeMillis()): StatusAula {
    return when {
        concluida -> StatusAula.CONCLUIDA
        dataHoraMillis == null -> StatusAula.NAO_AGENDADA
        fimDoDiaMillis(dataHoraMillis) < agoraMillis -> StatusAula.ATRASADA
        else -> StatusAula.AGENDADA
    }
}
