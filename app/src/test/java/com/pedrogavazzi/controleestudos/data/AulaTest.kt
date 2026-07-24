package com.pedrogavazzi.controleestudos.data

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Test

class AulaTest {

    private fun millisEm(ano: Int, mes: Int, dia: Int, hora: Int, minuto: Int): Long =
        Calendar.getInstance().apply {
            set(ano, mes, dia, hora, minuto, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun aulaBase(dataHoraMillis: Long? = null, concluida: Boolean = false) = Aula(
        materiaId = 1L,
        numero = 1,
        dataHoraMillis = dataHoraMillis,
        concluida = concluida
    )

    @Test
    fun `aula sem data e nao agendada`() {
        assertEquals(StatusAula.NAO_AGENDADA, aulaBase().statusAtual())
    }

    @Test
    fun `aula concluida e sempre concluida mesmo com data no passado`() {
        val aula = aulaBase(dataHoraMillis = millisEm(2020, Calendar.JANUARY, 1, 10, 0), concluida = true)
        assertEquals(StatusAula.CONCLUIDA, aula.statusAtual())
    }

    @Test
    fun `aula com horario ja passado no MESMO dia ainda esta agendada, nao atrasada`() {
        // Regra do app: só fica atrasada depois do FIM do dia agendado, não assim que o
        // horário exato passa — esse foi um bug relatado em uma rodada anterior.
        val dataAula = millisEm(2026, Calendar.JULY, 24, 8, 0)
        val agoraNoMesmoDia = millisEm(2026, Calendar.JULY, 24, 20, 0)
        val aula = aulaBase(dataHoraMillis = dataAula)
        assertEquals(StatusAula.AGENDADA, aula.statusAtual(agoraNoMesmoDia))
    }

    @Test
    fun `aula fica atrasada so depois do fim do dia agendado`() {
        val dataAula = millisEm(2026, Calendar.JULY, 24, 8, 0)
        val diaSeguinte = millisEm(2026, Calendar.JULY, 25, 0, 1)
        val aula = aulaBase(dataHoraMillis = dataAula)
        assertEquals(StatusAula.ATRASADA, aula.statusAtual(diaSeguinte))
    }

    @Test
    fun `aula com data no futuro esta agendada`() {
        val dataFutura = millisEm(2030, Calendar.JANUARY, 1, 10, 0)
        val aula = aulaBase(dataHoraMillis = dataFutura)
        assertEquals(StatusAula.AGENDADA, aula.statusAtual(millisEm(2026, Calendar.JULY, 24, 10, 0)))
    }

    @Test
    fun `nomeExibido usa nome personalizado quando definido`() {
        val aula = aulaBase().copy(numero = 3, nomePersonalizado = "Prova final")
        assertEquals("Prova final", aula.nomeExibido())
    }

    @Test
    fun `nomeExibido volta ao padrao quando personalizado esta em branco`() {
        val aula = aulaBase().copy(numero = 3, nomePersonalizado = "   ")
        assertEquals("Aula 3", aula.nomeExibido())
    }

    @Test
    fun `nomeExibido usa padrao numerado quando nao ha nome personalizado`() {
        val aula = aulaBase().copy(numero = 7, nomePersonalizado = null)
        assertEquals("Aula 7", aula.nomeExibido())
    }
}
