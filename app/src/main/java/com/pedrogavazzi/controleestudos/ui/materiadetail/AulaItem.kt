package com.pedrogavazzi.controleestudos.ui.materiadetail

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.pedrogavazzi.controleestudos.data.nomeExibido
import com.pedrogavazzi.controleestudos.data.statusAtual
import com.pedrogavazzi.controleestudos.ui.components.AnotacaoEditor
import com.pedrogavazzi.controleestudos.ui.components.StatusChip
import com.pedrogavazzi.controleestudos.ui.components.abrirSeletorDeDataEHora
import com.pedrogavazzi.controleestudos.ui.components.formatarDataHora
import com.pedrogavazzi.controleestudos.ui.theme.VermelhoAlerta

@Composable
fun AulaItem(
    aula: Aula,
    expandido: Boolean,
    onToggleExpandir: () -> Unit,
    onAgendar: (Long) -> Unit,
    onReagendar: (Long) -> Unit,
    onMarcarConclusao: (Boolean) -> Unit,
    onDefinirAlerta: (Boolean) -> Unit,
    onDefinirTipoAlerta: (TipoAlerta) -> Unit,
    onSalvarObservacao: (String) -> Unit,
    onAbrirCaderno: () -> Unit,
    onRenomear: (String?) -> Unit,
    onExcluir: () -> Unit
) {
    val context = LocalContext.current
    var mostrarDialogoRenomear by remember { mutableStateOf(false) }
    var mostrarConfirmacaoExclusao by remember { mutableStateOf(false) }
    val status = aula.statusAtual()

    // Importante: apenas o cabeçalho é clicável para expandir/recolher — se o Card inteiro
    // fosse clicável, tocar no campo de observação (mais abaixo) podia recolher o card
    // antes do usuário conseguir digitar ou salvar. O ícone de conclusão, o nome e o lápis
    // de renomear têm seus próprios toques, que não disparam esse clique de fundo.
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpandir)
            ) {
                IconButton(onClick = { onMarcarConclusao(!aula.concluida) }) {
                    Icon(
                        imageVector = if (aula.concluida) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = if (aula.concluida) "Marcar como não concluída" else "Marcar como concluída",
                        tint = if (aula.concluida) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(Modifier.padding(start = 8.dp))
                Text(
                    aula.nomeExibido(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { Toast.makeText(context, aula.nomeExibido(), Toast.LENGTH_SHORT).show() }
                )
                IconButton(onClick = { mostrarDialogoRenomear = true }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Renomear aula")
                }
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
                        Text(
                            if (aula.concluida) "Aula concluída (toque para desmarcar)" else "Aula concluída",
                            modifier = Modifier.weight(1f)
                        )
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
                        Text(
                            "Forma da notificação",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val comSomSelecionado = aula.tipoAlerta == TipoAlerta.COM_SOM
                            if (comSomSelecionado) {
                                Button(onClick = {}, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                    Icon(Icons.Filled.VolumeUp, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                    Text("Com som")
                                }
                            } else {
                                OutlinedButton(onClick = { onDefinirTipoAlerta(TipoAlerta.COM_SOM) }, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Filled.VolumeUp, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                    Text("Com som")
                                }
                            }
                            val semSomSelecionado = aula.tipoAlerta == TipoAlerta.SEM_SOM
                            if (semSomSelecionado) {
                                Button(onClick = {}, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                    Icon(Icons.Filled.VolumeOff, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                    Text("Sem som")
                                }
                            } else {
                                OutlinedButton(onClick = { onDefinirTipoAlerta(TipoAlerta.SEM_SOM) }, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Filled.VolumeOff, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                    Text("Sem som")
                                }
                            }
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
                    com.pedrogavazzi.controleestudos.ui.caderno.PreviaDoCaderno(
                        anotacoesCaderno = aula.anotacoesCaderno,
                        onClick = onAbrirCaderno
                    )

                    Spacer(Modifier.padding(top = 16.dp))
                    TextButton(
                        onClick = { mostrarConfirmacaoExclusao = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Excluir aula", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (mostrarDialogoRenomear) {
        var nomeDigitado by remember { mutableStateOf(aula.nomePersonalizado ?: "") }
        AlertDialog(
            onDismissRequest = { mostrarDialogoRenomear = false },
            title = { Text("Renomear aula") },
            text = {
                Column {
                    Text(
                        "Deixe em branco para usar o nome padrão (\"Aula ${aula.numero}\").",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = nomeDigitado,
                        onValueChange = { nomeDigitado = it },
                        label = { Text("Nome da aula") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onRenomear(nomeDigitado.trim().ifBlank { null })
                    mostrarDialogoRenomear = false
                }) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoRenomear = false }) { Text("Cancelar") }
            }
        )
    }

    if (mostrarConfirmacaoExclusao) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacaoExclusao = false },
            title = { Text("Excluir ${aula.nomeExibido()}?") },
            text = { Text("As anotações e o caderno dessa aula serão perdidos. Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    onExcluir()
                    mostrarConfirmacaoExclusao = false
                }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacaoExclusao = false }) { Text("Cancelar") }
            }
        )
    }
}
