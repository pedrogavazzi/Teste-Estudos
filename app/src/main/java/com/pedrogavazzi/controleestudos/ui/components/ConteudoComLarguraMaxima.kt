package com.pedrogavazzi.controleestudos.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Largura confortável de leitura — acima disso, texto em linha única fica cansativo de
 *  acompanhar (o motivo de livros e jornais nunca usarem a página inteira como coluna). */
private val LARGURA_MAXIMA_CONTEUDO = 600.dp

/**
 * Envolve o conteúdo de uma tela limitando a largura em telas grandes (tablet, celular em
 * paisagem), centralizando o resultado — em vez de deixar linhas de texto e cards esticarem
 * até a borda da tela inteira. Em celulares na vertical (a grande maioria dos casos) isso não
 * muda nada visualmente, já que a tela é mais estreita que o limite.
 */
@Composable
fun ConteudoComLarguraMaxima(
    modifier: Modifier = Modifier,
    conteudo: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxWidth().fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
        Box(modifier = Modifier.widthIn(max = LARGURA_MAXIMA_CONTEUDO).fillMaxHeight()) {
            conteudo()
        }
    }
}
