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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pedrogavazzi.controleestudos.data.Aula
import com.pedrogavazzi.controleestudos.data.StatusAula
import com.pedrogavazzi.controleestudos.data.nomeExibido
import com.pedrogavazzi.controleestudos.data.statusAtual
import com.pedrogavazzi.controleestudos.ui.components.AnotacaoEditor
import com.pedrogavazzi.controleestudos.ui.components.IconeConclusao
import com.pedrogavazzi.controleestudos.ui.components.StatusChip
import com.pedrogavazzi.controleestudos.ui.components.TAMANHO_MAXIMO_NOME_MATERIA
import com.pedrogavazzi.controleestudos.ui.components.abrirLinkDaAula
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
    onRemoverAgendamento: () -> Unit,
    onMarcarConclusao: (Boolean) -> Unit,
    onSalvarObservacao: (String) -> Unit,
    onSalvarLink: (String) -> Unit,
    onAbrirCaderno: () -> Unit,
    onRenomear: (String?) -> Unit,
    onExcluir: () -> Unit,
    onEditandoAlterado: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var mostrarDialogoRenomear by remember { mutableStateOf(false) }
    var mostrarConfirmacaoExclusao by remember { mutableStateOf(false) }
    val status = aula.statusAtual()

    androidx.compose.runtime.LaunchedEffect(mostrarDialogoRenomear) {
        onEditandoAlterado(mostrarDialogoRenomear)
    }

    // O card inteiro abre/fecha ao tocar em qualquer lugar — os botões "Renomear", "Excluir"
    // e o ícone de conclusão são seus próprios elementos clicáveis e consomem o toque antes
    // que ele chegue ao card, então não disparam a abertura por engano.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpandir),
        shape = com.pedrogavazzi.controleestudos.ui.theme.FormaCard,
        colors = com.pedrogavazzi.controleestudos.ui.theme.corDeCardTonal()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                IconeConclusao(concluida = aula.concluida, onAlterar = onMarcarConclusao)
                Spacer(Modifier.padding(start = 4.dp))
                Text(
                    aula.nomeExibido(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f).padding(top = 12.dp)
                )
                Spacer(Modifier.padding(start = 4.dp))
                StatusChip(status, modifier = Modifier.padding(top = 8.dp))
                if (aula.link.isNotBlank()) {
                    IconButton(
                        onClick = { abrirLinkDaAula(context, aula.link) },
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Filled.OpenInNew,
                            contentDescription = "Abrir link da aula",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextButton(onClick = { mostrarDialogoRenomear = true }) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Renomear")
                }
                TextButton(onClick = { mostrarConfirmacaoExclusao = true }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            }

            if (aula.dataHoraMillis != null) {
                Text(formatarDataHora(aula.dataHoraMillis), style = MaterialTheme.typography.bodyLarge)
            } else if (aula.observacao.isNotBlank()) {
                Text(
                    aula.observacao,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (status == StatusAula.ATRASADA) {
                Text(
                    "Esta aula não foi concluída nem reagendada até o fim do dia previsto.",
                    color = VermelhoAlerta,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            AnimatedVisibility(
                visible = expandido,
                enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(180)) +
                    androidx.compose.animation.expandVertically(
                        animationSpec = androidx.compose.animation.core.tween(220),
                        expandFrom = Alignment.Top
                    ),
                exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(120)) +
                    androidx.compose.animation.shrinkVertically(
                        animationSpec = androidx.compose.animation.core.tween(200),
                        shrinkTowards = Alignment.Top
                    )
            ) {
                Column(Modifier.padding(top = 12.dp)) {

                    // Uma linha só para as ações de agendamento, em vez de dois botões
                    // empilhados — "Remover" fica ao lado quando já existe uma data marcada.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (status == StatusAula.ATRASADA) {
                            OutlinedButton(
                                onClick = {
                                    abrirSeletorDeDataEHora(context, aula.dataHoraMillis) { novaData ->
                                        onReagendar(novaData)
                                    }
                                },
                                modifier = Modifier.weight(1f)
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
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                Text(
                                    if (aula.dataHoraMillis == null) "Agendar" else "Alterar data",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (aula.dataHoraMillis != null) {
                            OutlinedButton(onClick = onRemoverAgendamento) {
                                Icon(
                                    Icons.Filled.EventBusy,
                                    // Esse botão não tem texto do lado (só o ícone), então
                                    // precisa da própria descrição pra quem usa leitor de tela.
                                    contentDescription = "Remover agendamento, voltar para não agendada"
                                )
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
                    AnotacaoEditor(
                        chaveDeIdentidade = aula.id,
                        valorSalvo = aula.link,
                        onSalvar = onSalvarLink,
                        rotulo = "Link da aula (opcional)",
                        minLinhas = 1
                    )
                    if (aula.link.isNotBlank()) {
                        TextButton(
                            onClick = { abrirLinkDaAula(context, aula.link) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text("Abrir link")
                        }
                    }

                    Spacer(Modifier.padding(top = 8.dp))
                    com.pedrogavazzi.controleestudos.ui.caderno.PreviaDoCaderno(
                        anotacoesCaderno = aula.anotacoesCaderno,
                        onClick = onAbrirCaderno
                    )
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
                        onValueChange = { novo -> if (novo.length <= TAMANHO_MAXIMO_NOME_MATERIA) nomeDigitado = novo },
                        label = { Text("Nome da aula") },
                        supportingText = { Text("${nomeDigitado.length}/$TAMANHO_MAXIMO_NOME_MATERIA") },
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
