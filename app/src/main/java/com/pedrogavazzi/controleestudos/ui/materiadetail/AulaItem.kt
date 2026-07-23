package com.pedrogavazzi.controleestudos.ui.materiadetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pedrogavazzi.controleestudos.data.Aula
import com.pedrogavazzi.controleestudos.data.StatusAula
import com.pedrogavazzi.controleestudos.data.TipoAlerta
import com.pedrogavazzi.controleestudos.data.statusAtual
import com.pedrogavazzi.controleestudos.ui.components.AnotacaoEditor
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
    onDefinirTipoAlerta: (TipoAlerta) -> Unit,
    onSalvarObservacao: (String) -> Unit,
    onSalvarAnotacaoCaderno: (String) -> Unit
) {
    val context = LocalContext.current
    var expandido by remember(aula.id) { mutableStateOf(false) }
    var mostrarCaderno by remember { mutableStateOf(false) }
    val status = aula.statusAtual()

    // Importante: apenas o cabeçalho é clicável para expandir/recolher — se o Card inteiro
    // fosse clicável, tocar no campo de observação (mais abaixo) podia recolher o card
    // antes do usuário conseguir digitar ou salvar.
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandido = !expandido }
            ) {
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
                    "Esta aula não foi concluída nem reagendada até o fim do dia previsto.",
                    color = VermelhoAlerta,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            AnimatedVisibility(visible = expandido) {
                Column(Modifier.padding(top = 12.dp)) {

                    // Um único botão de agendamento: "Reagendar" se estiver atrasada (reinicia a
                    // contagem de atraso e soma ao contador), senão "Agendar"/"Alterar data e horário".
                    if (status == StatusAula.ATRASADA) {
                        OutlinedButton(
                            onClick = {
                                abrirSeletorDeDataEHora(context, aula.dataHoraMillis) { novaData ->
                                    onReagendar(novaData)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Update, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text("Reagendar", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                abrirSeletorDeDataEHora(context, aula.dataHoraMillis) { novaData ->
                                    onAgendar(novaData)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text(
                                if (aula.dataHoraMillis == null) "Agendar" else "Alterar data e horário",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
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

                    if (aula.alertaAtivado) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = aula.tipoAlerta == TipoAlerta.COM_SOM,
                                onClick = { onDefinirTipoAlerta(TipoAlerta.COM_SOM) },
                                label = { Text("Com som") },
                                leadingIcon = { Icon(Icons.Filled.VolumeUp, contentDescription = null) }
                            )
                            FilterChip(
                                selected = aula.tipoAlerta == TipoAlerta.SEM_SOM,
                                onClick = { onDefinirTipoAlerta(TipoAlerta.SEM_SOM) },
                                label = { Text("Sem som") },
                                leadingIcon = { Icon(Icons.Filled.VolumeOff, contentDescription = null) }
                            )
                        }
                    }

                    if (aula.vezesReagendada > 0) {
                        Text(
                            "Reagendada ${aula.vezesReagendada}x",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.padding(top = 8.dp))
                    AnotacaoEditor(
                        chaveDeIdentidade = aula.id,
                        valorSalvo = aula.observacao,
                        onSalvar = onSalvarObservacao,
                        rotulo = "Observação"
                    )

                    Spacer(Modifier.padding(top = 8.dp))
                    TextButton(onClick = { mostrarCaderno = true }) {
                        Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text(if (aula.anotacoesCaderno.isBlank()) "Abrir caderno da aula" else "Ver caderno da aula")
                    }
                }
            }
        }
    }

    if (mostrarCaderno) {
        AlertDialog(
            onDismissRequest = { mostrarCaderno = false },
            title = { Text("Caderno — Aula ${aula.numero}") },
            text = {
                AnotacaoEditor(
                    chaveDeIdentidade = aula.id,
                    valorSalvo = aula.anotacoesCaderno,
                    onSalvar = onSalvarAnotacaoCaderno,
                    rotulo = "Anotações da aula",
                    minLinhas = 6
                )
            },
            confirmButton = {
                TextButton(onClick = { mostrarCaderno = false }) { Text("Fechar") }
            }
        )
    }
}
