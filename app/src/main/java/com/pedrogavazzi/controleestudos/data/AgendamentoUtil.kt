package com.pedrogavazzi.controleestudos.data

import java.util.Calendar

/**
 * Lógica pura de cálculo de datas para o agendamento em lote — extraída do repositório para
 * poder ser testada isoladamente (sem precisar de Android/Room), já que essa é a lógica que
 * já teve mais de um bug relatado (contar fim de semana como dia útil, datas colidindo).
 */
object AgendamentoUtil {

    /** Empurra para a próxima segunda-feira se a data cair em sábado ou domingo. */
    fun empurrarParaDiaUtil(calendario: Calendar) {
        while (calendario.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendario.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            calendario.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    /** Avança exatamente [quantidade] dias ÚTEIS (sábado/domingo não contam nem são destino). */
    fun adicionarDiasUteis(calendario: Calendar, quantidade: Int) {
        var restante = quantidade
        while (restante > 0) {
            calendario.add(Calendar.DAY_OF_YEAR, 1)
            val diaDaSemana = calendario.get(Calendar.DAY_OF_WEEK)
            if (diaDaSemana != Calendar.SATURDAY && diaDaSemana != Calendar.SUNDAY) {
                restante--
            }
        }
    }

    /**
     * Calcula as datas (em millis) de [quantidade] aulas em sequência, a partir de
     * [dataHoraInicialMillis], espaçadas por [intervaloDias] dias. Se [apenasDiasUteis]
     * estiver ativo, pula sábados e domingos sem contá-los como parte do intervalo.
     */
    fun calcularDatas(
        dataHoraInicialMillis: Long,
        intervaloDias: Int,
        quantidade: Int,
        apenasDiasUteis: Boolean
    ): List<Long> {
        if (quantidade <= 0) return emptyList()
        val calendario = Calendar.getInstance().apply { timeInMillis = dataHoraInicialMillis }
        if (apenasDiasUteis) empurrarParaDiaUtil(calendario)

        val resultado = mutableListOf<Long>()
        val passo = intervaloDias.coerceAtLeast(1)
        for (indice in 0 until quantidade) {
            if (indice > 0) {
                if (apenasDiasUteis) {
                    adicionarDiasUteis(calendario, passo)
                } else {
                    calendario.add(Calendar.DAY_OF_YEAR, passo)
                }
            }
            resultado.add(calendario.timeInMillis)
        }
        return resultado
    }
}
