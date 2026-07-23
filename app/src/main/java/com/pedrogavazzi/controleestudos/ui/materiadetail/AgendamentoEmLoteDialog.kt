package com.pedrogavazzi.controleestudos.ui.materiadetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pedrogavazzi.controleestudos.ui.components.abrirSeletorDeDataEHora
import com.pedrogavazzi.controleestudos.ui.components.formatarDataHora

private data class OpcaoRecorrencia(val rotulo: String, val intervaloDias: Int)

private val opcoesRecorrencia = listOf(
    OpcaoRecorrencia("Todo dia", 1),
    OpcaoRecorrencia("A cada 2 dias", 2),
    OpcaoRecorrencia("A cada 3 dias", 3),
    OpcaoRecorrencia("Toda semana", 7)
)

/**
 * Diálogo para agendar várias aulas ainda sem data de uma vez: define a data/horário da
 * primeira aula, o padrão de repetição (todo dia, a cada 2/3 dias, toda semana no mesmo dia)
 * e quantas aulas devem receber esse padrão a partir da próxima ainda não agendada.
 */
@Composable
fun AgendamentoEmLoteDialog(
    quantidadeMaximaDisponivel: Int,
    onDismiss: () -> Unit,
    onConfirmar: (dataHoraInicialMillis: Long, intervaloDias: Int, quantidade: Int) -> Unit
) {
    val context = LocalContext.current
    var dataHoraInicial by remember { mutableStateOf<Long?>(null) }
    var recorrenciaSelecionada by remember { mutableStateOf(opcoesRecorrencia.first()) }
    var quantidadeTexto by remember {
        mutableStateOf(quantidadeMaximaDisponivel.coerceAtLeast(0).toString())
    }

    val quantidade = quantidadeTexto.toIntOrNull()
    val quantidadeValida = quantidade != null && quantidade in 1..quantidadeMaximaDisponivel
    val podeConfirmar = dataHoraInicial != null && quantidadeValida

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agendamento em lote") },
        text = {
            Column {
                Text("Agenda em sequência as próximas aulas que ainda não têm data, todas no mesmo horário do dia escolhido.")

                Row(modifier = Modifier.padding(top = 12.dp)) {
                    OutlinedButton(onClick = {
                        abrirSeletorDeDataEHora(context, dataHoraInicial) { novaData ->
                            dataHoraInicial = novaData
                        }
                    }) {
                        Text(
                            if (dataHoraInicial == null) "Escolher data/horário da 1ª aula"
                            else "1ª aula: ${formatarDataHora(dataHoraInicial)}"
                        )
                    }
                }

                Text("Repetir", modifier = Modifier.padding(top = 16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    opcoesRecorrencia.forEach { opcao ->
                        FilterChip(
                            selected = recorrenciaSelecionada == opcao,
                            onClick = { recorrenciaSelecionada = opcao },
                            label = { Text(opcao.rotulo) }
                        )
                    }
                }

                OutlinedTextField(
                    value = quantidadeTexto,
                    onValueChange = { novo -> if (novo.all { it.isDigit() }) quantidadeTexto = novo },
                    label = { Text("Quantidade de aulas a agendar (máx. $quantidadeMaximaDisponivel)") },
                    singleLine = true,
                    isError = quantidadeTexto.isNotBlank() && !quantidadeValida,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                enabled = podeConfirmar,
                onClick = {
                    dataHoraInicial?.let { inicio ->
                        onConfirmar(inicio, recorrenciaSelecionada.intervaloDias, quantidade ?: 0)
                    }
                }
            ) { Text("Agendar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
