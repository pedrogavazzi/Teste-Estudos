package com.pedrogavazzi.controleestudos.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Forma e cores de card usadas em todo o app: cantos mais arredondados e cor de superfície
 * tonal (em vez do card branco com sombra do Material 2) — o padrão visual atual do
 * Material 3 usado pelos apps do Google (Gmail, Agenda, etc.), mais "chapado" e sem sombra
 * pesada, com hierarquia dada pela cor em vez da elevação.
 */
val FormaCard = RoundedCornerShape(16.dp)

@Composable
fun corDeCardTonal(): CardColors =
    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)

@Composable
fun corDeCardTonalDestacado(): CardColors =
    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
