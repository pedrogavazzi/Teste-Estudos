package com.pedrogavazzi.controleestudos.data

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgendamentoUtilTest {

    private fun calendarioPara(ano: Int, mes: Int, dia: Int): Calendar =
        Calendar.getInstance().apply {
            set(ano, mes, dia, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

    private fun diaDaSemana(millis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.DAY_OF_WEEK)

    @Test
    fun `todo dia sem pular fim de semana inclui sabado e domingo`() {
        // 2026-07-24 é sexta-feira.
        val sexta = calendarioPara(2026, Calendar.JULY, 24).timeInMillis
        val datas = AgendamentoUtil.calcularDatas(sexta, intervaloDias = 1, quantidade = 4, apenasDiasUteis = false)

        assertEquals(4, datas.size)
        assertEquals(Calendar.FRIDAY, diaDaSemana(datas[0]))
        assertEquals(Calendar.SATURDAY, diaDaSemana(datas[1]))
        assertEquals(Calendar.SUNDAY, diaDaSemana(datas[2]))
        assertEquals(Calendar.MONDAY, diaDaSemana(datas[3]))
    }

    @Test
    fun `apenas dias uteis pula sabado e domingo sem contar como intervalo`() {
        // 2026-07-24 é sexta-feira.
        val sexta = calendarioPara(2026, Calendar.JULY, 24).timeInMillis
        val datas = AgendamentoUtil.calcularDatas(sexta, intervaloDias = 1, quantidade = 5, apenasDiasUteis = true)

        assertEquals(5, datas.size)
        // Nenhuma data deve cair em sábado ou domingo.
        datas.forEach { data ->
            val dia = diaDaSemana(data)
            assertTrue("data caiu em fim de semana", dia != Calendar.SATURDAY && dia != Calendar.SUNDAY)
        }
        // Sexta, Segunda, Terça, Quarta, Quinta — sem repetir nem colidir.
        assertEquals(Calendar.FRIDAY, diaDaSemana(datas[0]))
        assertEquals(Calendar.MONDAY, diaDaSemana(datas[1]))
        assertEquals(Calendar.TUESDAY, diaDaSemana(datas[2]))
        assertEquals(Calendar.WEDNESDAY, diaDaSemana(datas[3]))
        assertEquals(Calendar.THURSDAY, diaDaSemana(datas[4]))
    }

    @Test
    fun `apenas dias uteis nunca gera datas repetidas mesmo com intervalo maior que 1`() {
        // 2026-07-23 é quinta-feira.
        val quinta = calendarioPara(2026, Calendar.JULY, 23).timeInMillis
        val datas = AgendamentoUtil.calcularDatas(quinta, intervaloDias = 2, quantidade = 6, apenasDiasUteis = true)

        assertEquals(6, datas.size)
        assertEquals("não pode haver datas repetidas", datas.size, datas.toSet().size)
        datas.forEach { data ->
            val dia = diaDaSemana(data)
            assertTrue(dia != Calendar.SATURDAY && dia != Calendar.SUNDAY)
        }
    }

    @Test
    fun `primeira aula em fim de semana e empurrada para segunda quando apenas dias uteis`() {
        // 2026-07-25 é sábado.
        val sabado = calendarioPara(2026, Calendar.JULY, 25).timeInMillis
        val datas = AgendamentoUtil.calcularDatas(sabado, intervaloDias = 1, quantidade = 1, apenasDiasUteis = true)

        assertEquals(1, datas.size)
        assertEquals(Calendar.MONDAY, diaDaSemana(datas[0]))
    }

    @Test
    fun `sem apenas dias uteis a primeira data nao e alterada mesmo em fim de semana`() {
        val sabado = calendarioPara(2026, Calendar.JULY, 25).timeInMillis
        val datas = AgendamentoUtil.calcularDatas(sabado, intervaloDias = 1, quantidade = 1, apenasDiasUteis = false)

        assertEquals(Calendar.SATURDAY, diaDaSemana(datas[0]))
    }

    @Test
    fun `quantidade zero retorna lista vazia`() {
        val hoje = System.currentTimeMillis()
        val datas = AgendamentoUtil.calcularDatas(hoje, intervaloDias = 1, quantidade = 0, apenasDiasUteis = true)
        assertTrue(datas.isEmpty())
    }

    @Test
    fun `intervalo grande com apenas dias uteis nunca colide numa varredura ampla`() {
        // Varre várias combinações de dia de início / intervalo / quantidade, garantindo que
        // nenhuma data se repita e nenhuma caia em fim de semana — o mesmo tipo de checagem
        // que revelou o bug original de aulas colidindo na mesma segunda-feira.
        for (diaInicial in 1..28) {
            for (intervalo in 1..10) {
                val inicio = calendarioPara(2026, Calendar.JULY, diaInicial).timeInMillis
                val datas = AgendamentoUtil.calcularDatas(inicio, intervalo, quantidade = 8, apenasDiasUteis = true)
                assertEquals(8, datas.toSet().size)
                datas.forEach { data ->
                    val dia = diaDaSemana(data)
                    assertTrue(dia != Calendar.SATURDAY && dia != Calendar.SUNDAY)
                }
            }
        }
    }

    @Test
    fun `misturarRoundRobin intercala grupos preservando ordem interna`() {
        val grupos = listOf(listOf("a1", "a2", "a3"), listOf("b1", "b2"), listOf("c1"))
        val resultado = AgendamentoUtil.misturarRoundRobin(grupos)
        assertEquals(listOf("a1", "b1", "c1", "a2", "b2", "a3"), resultado)
    }

    @Test
    fun `misturarRoundRobin com grupo vazio no meio nao quebra`() {
        val grupos = listOf(listOf("a1", "a2"), emptyList(), listOf("c1"))
        val resultado = AgendamentoUtil.misturarRoundRobin(grupos)
        assertEquals(listOf("a1", "c1", "a2"), resultado)
    }

    @Test
    fun `misturarRoundRobin com todos os grupos vazios devolve lista vazia`() {
        val resultado = AgendamentoUtil.misturarRoundRobin(listOf(emptyList(), emptyList<String>()))
        assertTrue(resultado.isEmpty())
    }

    @Test
    fun `calcularSlotsAutomaticos preenche todos os horarios do dia antes de avancar`() {
        // 2026-07-24 é sexta-feira.
        val sexta = calendarioPara(2026, Calendar.JULY, 24).timeInMillis
        val slots = AgendamentoUtil.calcularSlotsAutomaticos(
            dataInicialMillis = sexta,
            quantidadeTotal = 6,
            aulasPorDia = 2,
            horariosMinutosDoDia = listOf(8 * 60, 19 * 60),
            incluirSabado = true,
            incluirDomingo = true
        )
        assertEquals(6, slots.size)
        assertEquals(Calendar.FRIDAY, diaDaSemana(slots[0]))
        assertEquals(Calendar.FRIDAY, diaDaSemana(slots[1]))
        assertEquals(Calendar.SATURDAY, diaDaSemana(slots[2]))
    }

    @Test
    fun `calcularSlotsAutomaticos pula sabado e domingo quando excluidos`() {
        val sexta = calendarioPara(2026, Calendar.JULY, 24).timeInMillis
        val slots = AgendamentoUtil.calcularSlotsAutomaticos(
            dataInicialMillis = sexta,
            quantidadeTotal = 4,
            aulasPorDia = 1,
            horariosMinutosDoDia = listOf(10 * 60),
            incluirSabado = false,
            incluirDomingo = false
        )
        assertEquals(4, slots.size)
        slots.forEach { slot ->
            val dia = diaDaSemana(slot)
            assertTrue(dia != Calendar.SATURDAY && dia != Calendar.SUNDAY)
        }
    }

    @Test
    fun `calcularSlotsAutomaticos comecando num sabado excluido pula pro proximo dia valido`() {
        val sabado = calendarioPara(2026, Calendar.JULY, 25).timeInMillis
        val slots = AgendamentoUtil.calcularSlotsAutomaticos(
            dataInicialMillis = sabado,
            quantidadeTotal = 1,
            aulasPorDia = 1,
            horariosMinutosDoDia = listOf(9 * 60),
            incluirSabado = false,
            incluirDomingo = false
        )
        assertEquals(Calendar.MONDAY, diaDaSemana(slots[0]))
    }

    @Test
    fun `calcularSlotsAutomaticos varredura ampla nunca gera horario duplicado no mesmo dia`() {
        // Regressão do bug encontrado durante o desenvolvimento: quando a quantidade de
        // horários é menor que aulasPorDia, o ciclo (índice % tamanho) repetia o mesmo
        // horário duas vezes no mesmo dia — aqui a lista de horários sempre tem exatamente
        // aulasPorDia itens (como a tela vai garantir), então isso nunca deve acontecer.
        for (porDia in 1..5) {
            val horarios = (0 until porDia).map { it * 180 + 480 } // horários bem espaçados
            for (diaInicial in 1..28) {
                val inicio = calendarioPara(2026, Calendar.JULY, diaInicial).timeInMillis
                val slots = AgendamentoUtil.calcularSlotsAutomaticos(
                    dataInicialMillis = inicio,
                    quantidadeTotal = 20,
                    aulasPorDia = porDia,
                    horariosMinutosDoDia = horarios,
                    incluirSabado = diaInicial % 2 == 0,
                    incluirDomingo = diaInicial % 3 == 0
                )
                assertEquals(20, slots.toSet().size)
                assertEquals(slots.sorted(), slots)
            }
        }
    }

    @Test
    fun `calcularInicioDaContinuacao usa o dia seguinte a ultima data agendada`() {
        val agora = calendarioPara(2026, Calendar.JULY, 26).timeInMillis
        val ultimaAgendada = calendarioPara(2026, Calendar.AUGUST, 5).timeInMillis
        val inicio = AgendamentoUtil.calcularInicioDaContinuacao(ultimaAgendada, agora)
        assertEquals(Calendar.AUGUST, diaDoMes(inicio).second)
        assertEquals(6, diaDoMes(inicio).first)
    }

    @Test
    fun `calcularInicioDaContinuacao usa hoje quando nada esta agendado`() {
        val agora = calendarioPara(2026, Calendar.JULY, 26).timeInMillis
        val inicio = AgendamentoUtil.calcularInicioDaContinuacao(null, agora)
        assertEquals(26, diaDoMes(inicio).first)
        assertEquals(Calendar.JULY, diaDoMes(inicio).second)
    }

    @Test
    fun `calcularInicioDaContinuacao usa hoje quando a ultima agendada ja passou`() {
        val agora = calendarioPara(2026, Calendar.JULY, 26).timeInMillis
        val ultimaNoPassado = calendarioPara(2026, Calendar.JANUARY, 1).timeInMillis
        val inicio = AgendamentoUtil.calcularInicioDaContinuacao(ultimaNoPassado, agora)
        assertEquals(26, diaDoMes(inicio).first)
        assertEquals(Calendar.JULY, diaDoMes(inicio).second)
    }

    private fun diaDoMes(millis: Long): Pair<Int, Int> {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return cal.get(Calendar.DAY_OF_MONTH) to cal.get(Calendar.MONTH)
    }
}
