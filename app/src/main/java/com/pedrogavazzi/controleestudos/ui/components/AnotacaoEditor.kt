package com.pedrogavazzi.controleestudos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Editor de texto livre reaproveitado tanto para a observação curta de uma aula (sempre
 * visível) quanto para as anotações longas do caderno (que iniciam resumidas/fechadas).
 *
 * Comportamento: toque no resumo abre a edição; tocar fora (ou o botão "Salvar"/"Recolher")
 * salva e, se [colapsavel], volta a mostrar o resumo. "Desfazer" descarta o que foi digitado
 * desde o último salvamento sem sair do modo de edição.
 */
@Composable
fun AnotacaoEditor(
    chaveDeIdentidade: Any,
    valorSalvo: String,
    onSalvar: (String) -> Unit,
    modifier: Modifier = Modifier,
    rotulo: String = "Observação",
    minLinhas: Int = 2,
    colapsavel: Boolean = false,
    textoResumoVazio: String = "Toque para anotar"
) {
    var expandido by remember(chaveDeIdentidade) { mutableStateOf(!colapsavel) }
    var texto by remember(chaveDeIdentidade) { mutableStateOf(valorSalvo) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    fun salvarESeColapsavelFechar() {
        if (texto != valorSalvo) onSalvar(texto)
        if (colapsavel) expandido = false
    }

    if (colapsavel && !expandido) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable {
                    texto = valorSalvo
                    expandido = true
                },
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Filled.EditNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 2.dp, end = 8.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(rotulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                Text(
                    text = valorSalvo.ifBlank { textoResumoVazio },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (valorSalvo.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Filled.ExpandMore, contentDescription = "Ampliar")
        }
    } else {
        Column(modifier.fillMaxWidth()) {
            if (colapsavel) {
                LaunchedEffect(chaveDeIdentidade) { focusRequester.requestFocus() }
            }
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text(rotulo) },
                minLines = minLinhas,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { estadoFoco ->
                        // Perder o foco (toque fora do campo, em outro item, etc.) salva e recolhe.
                        if (!estadoFoco.isFocused) salvarESeColapsavelFechar()
                    }
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (texto != valorSalvo) {
                    TextButton(onClick = { texto = valorSalvo }) { Text("Desfazer") }
                }
                if (colapsavel) {
                    TextButton(onClick = {
                        focusManager.clearFocus()
                        salvarESeColapsavelFechar()
                    }) {
                        Icon(Icons.Filled.ExpandLess, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Recolher")
                    }
                }
                TextButton(onClick = {
                    focusManager.clearFocus()
                    onSalvar(texto)
                    if (colapsavel) expandido = false
                }) { Text("Salvar") }
            }
        }
    }
}
