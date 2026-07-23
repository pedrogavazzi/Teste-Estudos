package com.pedrogavazzi.controleestudos.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Diálogo de confirmação usado antes de desmarcar uma aula já concluída (mas nunca ao marcar
 * como concluída, que não precisa de confirmação) — mesmo texto em qualquer tela que tenha
 * essa opção, para manter consistência.
 */
@Composable
private fun DialogoConfirmarDesmarcar(onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Desmarcar como concluída?") },
        text = { Text("Essa aula volta a aparecer como pendente na agenda e no desempenho.") },
        confirmButton = { TextButton(onClick = onConfirmar) { Text("Desmarcar") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

/** Ícone de conclusão (usado no cabeçalho compacto de uma aula) que pede confirmação só
 *  quando o toque for para DESMARCAR uma aula já concluída. */
@Composable
fun IconeConclusao(concluida: Boolean, onAlterar: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    var mostrarConfirmacao by remember { mutableStateOf(false) }

    IconButton(
        onClick = { if (concluida) mostrarConfirmacao = true else onAlterar(true) },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (concluida) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = if (concluida) "Marcar como não concluída" else "Marcar como concluída",
            tint = if (concluida) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    }

    if (mostrarConfirmacao) {
        DialogoConfirmarDesmarcar(
            onConfirmar = { onAlterar(false); mostrarConfirmacao = false },
            onCancelar = { mostrarConfirmacao = false }
        )
    }
}

/** Checkbox de conclusão (usado em listas — Agenda, Caderno) com a mesma confirmação
 *  ao desmarcar uma aula já concluída. */
@Composable
fun CaixaConclusao(concluida: Boolean, onAlterar: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    var mostrarConfirmacao by remember { mutableStateOf(false) }

    Checkbox(
        checked = concluida,
        onCheckedChange = { novoValor ->
            if (concluida && !novoValor) mostrarConfirmacao = true else onAlterar(novoValor)
        },
        modifier = modifier
    )

    if (mostrarConfirmacao) {
        DialogoConfirmarDesmarcar(
            onConfirmar = { onAlterar(false); mostrarConfirmacao = false },
            onCancelar = { mostrarConfirmacao = false }
        )
    }
}
