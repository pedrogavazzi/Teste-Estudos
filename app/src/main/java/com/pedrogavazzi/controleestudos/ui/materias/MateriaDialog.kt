package com.pedrogavazzi.controleestudos.ui.materias

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.pedrogavazzi.controleestudos.data.Materia
import com.pedrogavazzi.controleestudos.ui.components.TAMANHO_MAXIMO_NOME_MATERIA
import com.pedrogavazzi.controleestudos.ui.theme.PaletaMaterias

/** Diálogo usado tanto para cadastrar uma nova matéria quanto para editar uma existente. */
@Composable
fun MateriaDialog(
    materiaParaEditar: Materia?,
    onDismiss: () -> Unit,
    onConfirmar: (nome: String, totalAulas: Int, corHex: String) -> Unit
) {
    var nome by remember { mutableStateOf(materiaParaEditar?.nome ?: "") }
    var totalAulasTexto by remember {
        mutableStateOf(materiaParaEditar?.totalAulas?.toString() ?: "")
    }
    var corSelecionada by remember {
        mutableStateOf(materiaParaEditar?.corHex ?: PaletaMaterias.first().toHex())
    }
    val totalAulas = totalAulasTexto.toIntOrNull()
    val nomeValido = nome.isNotBlank()
    val totalValido = totalAulas != null && totalAulas in 1..999

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (materiaParaEditar == null) "Nova matéria" else "Editar matéria") },
        text = {
            Column {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { novo -> if (novo.length <= TAMANHO_MAXIMO_NOME_MATERIA) nome = novo },
                    label = { Text("Nome da matéria *") },
                    supportingText = { Text("${nome.length}/$TAMANHO_MAXIMO_NOME_MATERIA") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.padding(top = 8.dp))
                OutlinedTextField(
                    value = totalAulasTexto,
                    onValueChange = { novo -> if (novo.all { it.isDigit() }) totalAulasTexto = novo },
                    label = { Text("Número de aulas *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.padding(top = 12.dp))
                Text("Cor de identificação", style = MaterialTheme.typography.labelLarge)
                PaletaMaterias.chunked(6).forEach { linha ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        linha.forEach { cor ->
                            val hex = cor.toHex()
                            BolinhaDeCor(
                                cor = cor,
                                selecionada = hex == corSelecionada,
                                onClick = { corSelecionada = hex }
                            )
                        }
                    }
                }
                Text(
                    "* obrigatório",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                enabled = nomeValido && totalValido,
                onClick = { onConfirmar(nome, totalAulas ?: 0, corSelecionada) }
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/**
 * Bolinha de cor selecionável: quando marcada, ganha um anel de contorno bem visível e um
 * ícone de check com cor de contraste automática (branco ou preto conforme a cor de fundo) —
 * a diferença de antes (2dp de padding) era sutil demais para perceber qual estava escolhida.
 */
@Composable
private fun BolinhaDeCor(cor: Color, selecionada: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(cor)
            .border(
                width = if (selecionada) 3.dp else 1.dp,
                color = if (selecionada) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selecionada) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "Cor selecionada",
                tint = corDeContraste(cor),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun corDeContraste(cor: Color): Color {
    val luminancia = 0.299 * cor.red + 0.587 * cor.green + 0.114 * cor.blue
    return if (luminancia > 0.6) Color.Black else Color.White
}

fun Color.toHex(): String {
    val argb = this.toArgb()
    return String.format("#%06X", 0xFFFFFF and argb)
}
