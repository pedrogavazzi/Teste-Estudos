package com.pedrogavazzi.controleestudos.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Quantidade máxima de caracteres permitida para o nome de uma matéria. */
const val TAMANHO_MAXIMO_NOME_MATERIA = 60

private val formatadorDataHora = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
private val formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))
private val formatadorDiaSemanaData = DateTimeFormatter.ofPattern("EEEE, dd/MM", Locale("pt", "BR"))
private val formatadorHora = DateTimeFormatter.ofPattern("HH:mm", Locale("pt", "BR"))

fun formatarDataHora(millis: Long?): String {
    if (millis == null) return "Sem data definida"
    val dataHora = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
    return dataHora.format(formatadorDataHora)
}

fun formatarData(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(formatadorData)

fun formatarDiaSemanaData(millis: Long): String {
    val texto = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(formatadorDiaSemanaData)
    return texto.replaceFirstChar { it.uppercase() }
}

fun formatarHora(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(formatadorHora)

/**
 * Exibe o nome de uma matéria de forma consistente em todas as telas: como o nome pode ter
 * até [TAMANHO_MAXIMO_NOME_MATERIA] caracteres, corta com reticências além de [maxLines] linhas
 * em vez de quebrar o layout da tela.
 */
@Composable
fun TextoNomeMateria(
    nome: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    maxLines: Int = 1
) {
    Text(
        text = nome,
        style = style,
        fontWeight = fontWeight,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}
