package com.pedrogavazzi.controleestudos.ui.materiadetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pedrogavazzi.controleestudos.data.Aula
import com.pedrogavazzi.controleestudos.data.StatusAula
import com.pedrogavazzi.controleestudos.data.statusAtual
import com.pedrogavazzi.controleestudos.ui.components.StatusChip
import com.pedrogavazzi.controleestudos.ui.components.abrirSeletorDeDataEHora
import com.pedrogavazzi.controleestudos.ui.components.formatarDataHora
import com.pedrogavazzi.controleestudos.ui.theme.VermelhoAlerta

@Composable
fun AulaItem(
    aula: Aula,
    onAgendar: (Long) -> Unit,
    onReagendar: (Long) -> Unit,
    onMarcarConclusao: (Boolean) -> Unit,
    onDefinirAlerta: (Boolean) -> Unit,
    onSalvarObservacao: (String) -> Unit
) {
    val context = LocalContext.current
    var expandido by remember(aula.id) { mutableStateOf(false) }
    var textoObservacao by remember(aula.id) { mutableStateOf(aula.observacao) }
    val status = aula.statusAtual()
    val observacaoAlterada = textoObservacao != aula.observacao

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandido = !expandido }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (aula.concluida) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (aula.concluida) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.padding(start = 8.dp))
                Text(
                    "Aula ${aula.numero}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(status)
            }
            Spacer(Modifier.padding(top = 4.dp))
            Text(formatarDataHora(aula.dataHoraMillis), style = MaterialTheme.typography.bodyLarge)

            if (status == StatusAula.ATRASADA) {
                Text(
                    "Esta aula não foi concluída no horário previsto.",
                    color = VermelhoAlerta,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            AnimatedVisibility(visible = expandido) {
                Column(Modifier.padding(top = 12.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = {
                            abrirSeletorDeDataEHora(context, aula.dataHoraMillis) { novaData ->
                                onAgendar(novaData)
                            }
                        }) {
                            Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text(if (aula.dataHoraMillis == null) "Agendar" else "Alterar data/horário")
                        }

                        if (!aula.concluida && aula.dataHoraMillis != null) {
                            Spacer(Modifier.padding(start = 8.dp))
                            OutlinedButton(onClick = {
                                abrirSeletorDeDataEHora(context, aula.dataHoraMillis) { novaData ->
                                    onReagendar(novaData)
                                }
                            }) {
                                Icon(Icons.Filled.Update, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                Text("Reagendar")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = aula.concluida, onCheckedChange = onMarcarConclusao)
                        Text("Aula concluída", modifier = Modifier.weight(1f))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (aula.alertaAtivado) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsOff,
                            contentDescription = null
                        )
                        Text("Alerta de notificação", modifier = Modifier.weight(1f).padding(start = 8.dp))
                        Switch(checked = aula.alertaAtivado, onCheckedChange = onDefinirAlerta)
                    }

                    if (aula.vezesReagendada > 0) {
                        Text(
                            "Reagendada ${aula.vezesReagendada}x",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.padding(top = 8.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        OutlinedTextField(
                            value = textoObservacao,
                            onValueChange = { textoObservacao = it },
                            label = { Text("Observação") },
                            modifier = Modifier.weight(1f),
                            minLines = 2
                        )
                        if (observacaoAlterada) {
                            IconButton(onClick = { onSalvarObservacao(textoObservacao) }) {
                                Icon(Icons.Filled.Save, contentDescription = "Salvar observação")
                            }
                        }
                    }
                }
            }
        }
    }
}
