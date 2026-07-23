package com.pedrogavazzi.controleestudos.ui.materias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
            androidx.compose.foundation.layout.Column {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { novo -> if (novo.length <= TAMANHO_MAXIMO_NOME_MATERIA) nome = novo },
                    label = { Text("Nome da matéria") },
                    supportingText = { Text("${nome.length}/$TAMANHO_MAXIMO_NOME_MATERIA") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 8.dp))
                OutlinedTextField(
                    value = totalAulasTexto,
                    onValueChange = { novo -> if (novo.all { it.isDigit() }) totalAulasTexto = novo },
                    label = { Text("Número de aulas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 12.dp))
                Text("Cor de identificação")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaletaMaterias.take(6).forEach { cor ->
                        val hex = cor.toHex()
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .then(
                                    if (hex == corSelecionada) Modifier.padding(2.dp) else Modifier
                                ),
                            shape = CircleShape,
                            color = cor,
                            onClick = { corSelecionada = hex }
                        ) {}
                    }
                }
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

fun Color.toHex(): String {
    val argb = this.toArgb()
    return String.format("#%06X", 0xFFFFFF and argb)
}
