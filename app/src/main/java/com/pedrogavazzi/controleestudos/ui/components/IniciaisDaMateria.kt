package com.pedrogavazzi.controleestudos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Bolinha colorida com a inicial da matéria dentro — usada em Agenda, Caderno e Matérias pra
 * identificar a matéria de relance. Antes disso, a cor sozinha era o único jeito de diferenciar
 * matérias nessas listas: difícil de reconhecer rápido com várias matérias cadastradas, e
 * impossível para quem não distingue bem as cores.
 */
@Composable
fun IniciaisDaMateria(
    nomeMateria: String,
    cor: Color,
    modifier: Modifier = Modifier,
    tamanho: Dp = 22.dp
) {
    val inicial = nomeMateria.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val corTexto = corDeContrasteParaFundo(cor)
    Box(
        modifier = modifier.size(tamanho).background(cor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            inicial,
            color = corTexto,
            fontSize = (tamanho.value * 0.5).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Preto ou branco, o que der mais contraste sobre a cor de fundo — mesma fórmula de
 *  luminância relativa usada no seletor de cor das matérias. */
fun corDeContrasteParaFundo(cor: Color): Color {
    val luminancia = 0.299 * cor.red + 0.587 * cor.green + 0.114 * cor.blue
    return if (luminancia > 0.6) Color.Black else Color.White
}
