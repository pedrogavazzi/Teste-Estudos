package com.pedrogavazzi.controleestudos.ui.caderno

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CadernoEstilosTest {

    @Test
    fun `inserir texto antes de um trecho formatado desloca o estilo junto`() {
        val antigo = "0123456789"
        val estilos = listOf(EstiloAplicado(3, 6, TipoEstilo.NEGRITO)) // "345"
        val novo = "Hello 0123456789" // inseriu "Hello " (6 chars) no início

        val ajustados = ajustarEstilosParaEdicao(estilos, antigo, novo)

        assertEquals(1, ajustados.size)
        assertEquals(EstiloAplicado(9, 12, TipoEstilo.NEGRITO), ajustados[0])
        assertEquals("345", novo.substring(ajustados[0].inicio, ajustados[0].fim))
    }

    @Test
    fun `apagar texto antes de um trecho formatado desloca o estilo pra tras`() {
        val antigo = "Hello 0123456789"
        val estilos = listOf(EstiloAplicado(9, 12, TipoEstilo.NEGRITO)) // "345"
        val novo = "0123456789" // removeu "Hello " do início

        val ajustados = ajustarEstilosParaEdicao(estilos, antigo, novo)

        assertEquals(EstiloAplicado(3, 6, TipoEstilo.NEGRITO), ajustados[0])
        assertEquals("345", novo.substring(ajustados[0].inicio, ajustados[0].fim))
    }

    @Test
    fun `digitar apos o texto nao afeta estilos existentes`() {
        val antigo = "abc DEF ghi"
        val estilos = listOf(EstiloAplicado(4, 7, TipoEstilo.NEGRITO)) // "DEF"
        val novo = "abc DEF ghi jkl"

        val ajustados = ajustarEstilosParaEdicao(estilos, antigo, novo)

        assertEquals(estilos, ajustados)
    }

    @Test
    fun `apagar o proprio trecho formatado remove o estilo`() {
        val antigo = "abc DEF ghi"
        val estilos = listOf(EstiloAplicado(4, 7, TipoEstilo.NEGRITO)) // "DEF"
        val novo = "abc  ghi" // removeu "DEF"

        val ajustados = ajustarEstilosParaEdicao(estilos, antigo, novo)

        assertTrue(ajustados.isEmpty())
    }

    @Test
    fun `varios estilos sao ajustados de forma independente`() {
        val antigo = "AAA BBB CCC"
        val estilos = listOf(
            EstiloAplicado(0, 3, TipoEstilo.NEGRITO), // AAA
            EstiloAplicado(8, 11, TipoEstilo.ITALICO) // CCC
        )
        val novo = "XY AAA BBB CCC" // inseriu "XY " no início

        val ajustados = ajustarEstilosParaEdicao(estilos, antigo, novo)

        assertEquals(EstiloAplicado(3, 6, TipoEstilo.NEGRITO), ajustados.first { it.tipo == TipoEstilo.NEGRITO })
        assertEquals(EstiloAplicado(11, 14, TipoEstilo.ITALICO), ajustados.first { it.tipo == TipoEstilo.ITALICO })
        assertEquals("AAA", novo.substring(3, 6))
        assertEquals("CCC", novo.substring(11, 14))
    }

    @Test
    fun `alternar estilo aplica quando trecho nao esta totalmente coberto`() {
        val resultado = alternarEstilo(emptyList(), TipoEstilo.NEGRITO, 2, 8)
        assertEquals(listOf(EstiloAplicado(2, 8, TipoEstilo.NEGRITO)), resultado)
    }

    @Test
    fun `alternar estilo remove quando trecho ja esta totalmente coberto`() {
        val estilos = listOf(EstiloAplicado(2, 8, TipoEstilo.NEGRITO))
        val resultado = alternarEstilo(estilos, TipoEstilo.NEGRITO, 2, 8)
        assertTrue(resultado.none { it.tipo == TipoEstilo.NEGRITO })
    }

    @Test
    fun `trechoTemEstilo reflete corretamente cobertura parcial e total`() {
        val estilos = listOf(EstiloAplicado(2, 8, TipoEstilo.NEGRITO))
        assertTrue(trechoTemEstilo(estilos, TipoEstilo.NEGRITO, 2, 8))
        assertTrue(trechoTemEstilo(estilos, TipoEstilo.NEGRITO, 3, 5))
        assertTrue(!trechoTemEstilo(estilos, TipoEstilo.NEGRITO, 1, 8))
        assertTrue(!trechoTemEstilo(estilos, TipoEstilo.ITALICO, 2, 8))
    }

    @Test
    fun `aplicarTamanho substitui tamanho anterior no mesmo trecho`() {
        val estilos = listOf(EstiloAplicado(0, 5, TipoEstilo.GRANDE))
        val resultado = aplicarTamanho(estilos, TipoEstilo.TITULO, 0, 5)
        assertEquals(listOf(EstiloAplicado(0, 5, TipoEstilo.TITULO)), resultado)
    }

    @Test
    fun `aplicarTamanho nulo remove qualquer tamanho do trecho`() {
        val estilos = listOf(EstiloAplicado(0, 5, TipoEstilo.GRANDE))
        val resultado = aplicarTamanho(estilos, null, 0, 5)
        assertTrue(resultado.none { it.tipo in setOf(TipoEstilo.PEQUENO, TipoEstilo.GRANDE, TipoEstilo.TITULO) })
    }

    // ---- Modo "apertar a formatação e depois digitar" ----

    @Test
    fun `estilo pendente aplica ao texto recem digitado`() {
        var estilos = ajustarEAplicarPendentes(emptyList(), "", "Ola", emptySet(), null)
        estilos = ajustarEAplicarPendentes(estilos, "Ola", "Ola mundo", setOf(TipoEstilo.NEGRITO), null)

        val negrito = estilos.first { it.tipo == TipoEstilo.NEGRITO }
        assertEquals(" mundo", "Ola mundo".substring(negrito.inicio, negrito.fim))
    }

    @Test
    fun `estilo pendente continua mesclando enquanto o usuario segue digitando`() {
        var estilos = ajustarEAplicarPendentes(emptyList(), "", "Ola", emptySet(), null)
        estilos = ajustarEAplicarPendentes(estilos, "Ola", "Ola mundo", setOf(TipoEstilo.NEGRITO), null)
        estilos = ajustarEAplicarPendentes(estilos, "Ola mundo", "Ola mundo!!!", setOf(TipoEstilo.NEGRITO), null)

        assertEquals(1, estilos.size)
        val negrito = estilos.first()
        assertEquals(TipoEstilo.NEGRITO, negrito.tipo)
        assertEquals(" mundo!!!", "Ola mundo!!!".substring(negrito.inicio, negrito.fim))
    }

    @Test
    fun `desligar o estilo pendente impede heranca indevida no texto digitado depois`() {
        // Esse teste pegou um bug real: o texto digitado logo após desligar o negrito estava
        // "herdando" a formatação por coincidência de posição, mesmo sem estilo pendente.
        var estilos = ajustarEAplicarPendentes(emptyList(), "", "abc", emptySet(), null)
        estilos = ajustarEAplicarPendentes(estilos, "abc", "abcNEG", setOf(TipoEstilo.NEGRITO), null)
        estilos = ajustarEAplicarPendentes(estilos, "abcNEG", "abcNEGxyz", emptySet(), null)

        assertEquals(1, estilos.size)
        val negrito = estilos.first()
        assertEquals("NEG", "abcNEGxyz".substring(negrito.inicio, negrito.fim))
    }

    @Test
    fun `tamanho pendente tambem se aplica ao texto recem digitado`() {
        val estilos = ajustarEAplicarPendentes(emptyList(), "", "Título grande", emptySet(), TipoEstilo.TITULO)
        val titulo = estilos.first { it.tipo == TipoEstilo.TITULO }
        assertEquals("Título grande", "Título grande".substring(titulo.inicio, titulo.fim))
    }

    @Test
    fun `apagar texto nao gera estilo mesmo com pendente ativo`() {
        val estilos = ajustarEAplicarPendentes(
            listOf(EstiloAplicado(0, 5, TipoEstilo.NEGRITO)), "abcde", "abc", setOf(TipoEstilo.ITALICO), null
        )
        // Apagar não insere nada, então nenhum ITALICO deveria aparecer — só o reajuste do NEGRITO.
        assertTrue(estilos.none { it.tipo == TipoEstilo.ITALICO })
    }
}
