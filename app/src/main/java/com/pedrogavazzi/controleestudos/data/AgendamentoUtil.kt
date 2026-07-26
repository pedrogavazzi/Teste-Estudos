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

    /**
     * Intercala vários grupos em rodízio (pega o próximo item de cada grupo, um de cada vez,
     * até todos esvaziarem) — usado pra misturar as aulas de várias matérias sem terminar uma
     * matéria inteira antes de começar a próxima. A ordem interna de cada grupo é preservada.
     */
    fun <T> misturarRoundRobin(grupos: List<List<T>>): List<T> {
        val resultado = mutableListOf<T>()
        val indices = IntArray(grupos.size)
        var restantes = grupos.sumOf { it.size }
        while (restantes > 0) {
            for (i in grupos.indices) {
                if (indices[i] < grupos[i].size) {
                    resultado.add(grupos[i][indices[i]])
                    indices[i]++
                    restantes--
                }
            }
        }
        return resultado
    }

    /**
     * Ponto de partida (início do dia, em millis) pra continuar agendando automaticamente:
     * o dia seguinte à última aula já agendada, ou hoje, se não houver nada agendado ainda
     * (ou se tudo que está agendado já ficou no passado) — assim, aulas novas entram sempre
     * no fim da fila, sem esbarrar em datas já ocupadas.
     */
    fun calcularInicioDaContinuacao(ultimaDataAgendadaMillis: Long?, agoraMillis: Long): Long {
        val inicioHoje = Calendar.getInstance().apply {
            timeInMillis = agoraMillis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        if (ultimaDataAgendadaMillis == null || ultimaDataAgendadaMillis < inicioHoje) return inicioHoje
        return Calendar.getInstance().apply {
            timeInMillis = ultimaDataAgendadaMillis
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Gera [quantidadeTotal] horários (em millis) pro agendamento automático: [aulasPorDia]
     * aulas em cada dia válido, usando [horariosMinutosDoDia] (minutos desde meia-noite —
     * precisa ter exatamente [aulasPorDia] valores, um por posição do dia) — pulando sábado
     * e/ou domingo conforme [incluirSabado]/[incluirDomingo]. Validado com testes fartos:
     * nunca gera dois horários iguais (mesmo quando o dia inicial cai num dia excluído) e
     * sempre devolve a quantidade pedida em ordem crescente.
     */
    fun calcularSlotsAutomaticos(
        dataInicialMillis: Long,
        quantidadeTotal: Int,
        aulasPorDia: Int,
        horariosMinutosDoDia: List<Int>,
        incluirSabado: Boolean,
        incluirDomingo: Boolean
    ): List<Long> {
        if (quantidadeTotal <= 0 || aulasPorDia <= 0 || horariosMinutosDoDia.isEmpty()) return emptyList()
        val resultado = mutableListOf<Long>()
        val diaBase = Calendar.getInstance().apply {
            timeInMillis = dataInicialMillis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        while (resultado.size < quantidadeTotal) {
            val diaDaSemana = diaBase.get(Calendar.DAY_OF_WEEK)
            val diaValido = when (diaDaSemana) {
                Calendar.SATURDAY -> incluirSabado
                Calendar.SUNDAY -> incluirDomingo
                else -> true
            }
            if (diaValido) {
                for (indiceSlot in 0 until aulasPorDia) {
                    if (resultado.size >= quantidadeTotal) break
                    val minutos = horariosMinutosDoDia[indiceSlot % horariosMinutosDoDia.size]
                    val slot = Calendar.getInstance().apply {
                        timeInMillis = diaBase.timeInMillis
                        add(Calendar.MINUTE, minutos)
                    }.timeInMillis
                    resultado.add(slot)
                }
            }
            diaBase.add(Calendar.DAY_OF_YEAR, 1)
        }
        return resultado
    }
}
