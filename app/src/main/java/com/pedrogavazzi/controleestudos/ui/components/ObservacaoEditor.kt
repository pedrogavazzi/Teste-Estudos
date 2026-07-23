package com.pedrogavazzi.controleestudos.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged

/**
 * Campo de observação livre com botão de salvar. Mantido como componente único (usado tanto
 * na tela de uma matéria quanto no Caderno) para garantir que o comportamento de salvar seja
 * sempre o mesmo. Salva também automaticamente quando o campo perde o foco, como garantia
 * extra contra perda de texto digitado (ex.: usuário toca fora para fechar o teclado).
 */
@Composable
fun ObservacaoEditor(
    chaveDeIdentidade: Any,
    observacaoSalva: String,
    onSalvar: (String) -> Unit,
    modifier: Modifier = Modifier,
    rotulo: String = "Observação"
) {
    var texto by remember(chaveDeIdentidade) { mutableStateOf(observacaoSalva) }
    val alterado = texto != observacaoSalva

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            label = { Text(rotulo) },
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { estadoFoco ->
                    if (!estadoFoco.isFocused && texto != observacaoSalva) {
                        onSalvar(texto)
                    }
                },
            minLines = 2
        )
        if (alterado) {
            IconButton(onClick = { onSalvar(texto) }) {
                Icon(Icons.Filled.Save, contentDescription = "Salvar observação")
            }
        }
    }
}
