package com.pedrogavazzi.controleestudos.ui.components

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
