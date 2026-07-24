package com.pedrogavazzi.controleestudos.ui.caderno

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CadernoSerializerTest {

    @Test
    fun `serializar e desserializar preserva texto e estilos`() {
        val nota = NotaCaderno(
            texto = "Hoje vimos derivadas e integrais.",
            estilos = listOf(
                EstiloAplicado(5, 9, TipoEstilo.NEGRITO),
                EstiloAplicado(15, 25, TipoEstilo.REALCE)
            )
        )
        val salvo = CadernoSerializer.serializar(nota)
        val restaurado = CadernoSerializer.desserializar(salvo)

        assertEquals(nota.texto, restaurado.texto)
        assertEquals(nota.estilos, restaurado.estilos)
    }

    @Test
    fun `nota vazia desserializa como texto vazio sem estilos`() {
        val restaurado = CadernoSerializer.desserializar("")
        assertEquals("", restaurado.texto)
        assertTrue(restaurado.estilos.isEmpty())
    }

    @Test
    fun `temConteudo e falso para nota so com cabecalho sem texto`() {
        val salvo = CadernoSerializer.serializar(NotaCaderno(texto = ""))
        assertTrue(!CadernoSerializer.temConteudo(salvo))
    }

    @Test
    fun `temConteudo e verdadeiro quando ha texto`() {
        val salvo = CadernoSerializer.serializar(NotaCaderno(texto = "anotação"))
        assertTrue(CadernoSerializer.temConteudo(salvo))
    }

    @Test
    fun `texto puro de versoes muito antigas e lido como um unico bloco sem formatacao`() {
        val restaurado = CadernoSerializer.desserializar("uma anotação bem antiga, sem formatação")
        assertEquals("uma anotação bem antiga, sem formatação", restaurado.texto)
        assertTrue(restaurado.estilos.isEmpty())
    }
}
