package com.pedrogavazzi.controleestudos.ui.materiadetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
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
import androidx.compose.ui.unit.dp
import com.pedrogavazzi.controleestudos.data.AgendamentoUtil
import com.pedrogavazzi.controleestudos.ui.components.abrirSeletorDeDataEHora
import com.pedrogavazzi.controleestudos.ui.components.formatarDataHora

private const val QUANTIDADE_MAXIMA_NA_PREVIA = 5

/**
 * Diálogo para agendar várias aulas ainda sem data de uma vez. Organizado em três perguntas
 * (quando começar, com que frequência, quantas aulas), com uma prévia das primeiras datas
 * resultantes antes de confirmar — pra deixar claro o que vai acontecer antes de aplicar.
 */
@Composable
fun AgendamentoEmLoteDialog(
    quantidadeMaximaDisponivel: Int,
    onDismiss: () -> Unit,
    onConfirmar: (dataHoraInicialMillis: Long, intervaloDias: Int, quantidade: Int, apenasDiasUteis: Boolean) -> Unit
) {
    val context = LocalContext.current
    var dataHoraInicial by remember { mutableStateOf<Long?>(null) }
    var intervaloTexto by remember { mutableStateOf("1") }
    var apenasDiasUteis by remember { mutableStateOf(false) }
    var quantidadeTexto by remember {
        mutableStateOf(quantidadeMaximaDisponivel.coerceAtLeast(0).toString())
    }

    val intervalo = intervaloTexto.toIntOrNull()
    val intervaloValido = intervalo != null && intervalo >= 1
    val quantidade = quantidadeTexto.toIntOrNull()
    val quantidadeValida = quantidade != null && quantidade in 1..quantidadeMaximaDisponivel
    val podeConfirmar = dataHoraInicial != null && intervaloValido && quantidadeValida

    val datasPreview = remember(dataHoraInicial, intervalo, quantidade, apenasDiasUteis) {
        val inicio = dataHoraInicial
        if (inicio != null && intervaloValido && quantidadeValida) {
            AgendamentoUtil.calcularDatas(inicio, intervalo!!, minOf(quantidade!!, QUANTIDADE_MAXIMA_NA_PREVIA), apenasDiasUteis)
        } else {
            emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agendamento em lote") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Preenche a data das próximas aulas que ainda não têm horário marcado " +
                        "($quantidadeMaximaDisponivel disponível(is)), a partir da próxima na ordem.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                RotuloSecao("Quando começar")
                OutlinedButton(
                    onClick = {
                        abrirSeletorDeDataEHora(context, dataHoraInicial) { novaData ->
                            dataHoraInicial = novaData
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (dataHoraInicial == null) "Escolher data/horário da 1ª aula *"
                        else "1ª aula: ${formatarDataHora(dataHoraInicial)}"
                    )
                }

                RotuloSecao("Com que frequência")
                OutlinedTextField(
                    value = intervaloTexto,
                    onValueChange = { novo -> if (novo.all { it.isDigit() }) intervaloTexto = novo },
                    label = { Text("Repetir a cada quantos dias *") },
                    supportingText = { Text("Ex.: 1 = todo dia, 7 = toda semana, ou qualquer número") },
                    singleLine = true,
                    isError = intervaloTexto.isNotBlank() && !intervaloValido,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = apenasDiasUteis, onCheckedChange = { apenasDiasUteis = it })
                    Text("Apenas em dias úteis (pula sábado e domingo)")
                }

                RotuloSecao("Quantas aulas")
                OutlinedTextField(
                    value = quantidadeTexto,
                    onValueChange = { novo -> if (novo.all { it.isDigit() }) quantidadeTexto = novo },
                    label = { Text("Quantidade de aulas *") },
                    supportingText = { Text("Máximo disponível: $quantidadeMaximaDisponivel") },
                    singleLine = true,
                    isError = quantidadeTexto.isNotBlank() && !quantidadeValida,
                    modifier = Modifier.fillMaxWidth()
                )

                if (datasPreview.isNotEmpty()) {
                    Divider(Modifier.padding(top = 16.dp, bottom = 8.dp))
                    Text(
                        "Prévia",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    datasPreview.forEach { data ->
                        Text(
                            "• ${formatarDataHora(data)}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    val restantes = (quantidade ?: 0) - datasPreview.size
                    if (restantes > 0) {
                        Text(
                            "... e mais $restantes aula(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Text(
                    "* obrigatório",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                enabled = podeConfirmar,
                onClick = {
                    dataHoraInicial?.let { inicio ->
                        onConfirmar(inicio, intervalo ?: 1, quantidade ?: 0, apenasDiasUteis)
                    }
                }
            ) {
                Text(if (quantidadeValida) "Agendar $quantidade aula(s)" else "Agendar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun RotuloSecao(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
    )
}
