package com.pedrogavazzi.controleestudos.ui.materiadetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.unit.dp
import com.pedrogavazzi.controleestudos.ui.components.abrirSeletorDeDataEHora
import com.pedrogavazzi.controleestudos.ui.components.formatarDataHora

/**
 * Diálogo para agendar várias aulas ainda sem data de uma vez: define a data/horário da
 * primeira aula, o intervalo em dias entre cada uma (qualquer número, escolhido livremente),
 * se deve pular fins de semana, e quantas aulas devem receber esse padrão a partir da
 * próxima ainda não agendada.
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

                OutlinedTextField(
                    value = intervaloTexto,
                    onValueChange = { novo -> if (novo.all { it.isDigit() }) intervaloTexto = novo },
                    label = { Text("Repetir a cada quantos dias") },
                    supportingText = { Text("Ex.: 1 = todo dia, 7 = toda semana, ou qualquer número") },
                    singleLine = true,
                    isError = intervaloTexto.isNotBlank() && !intervaloValido,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = apenasDiasUteis, onCheckedChange = { apenasDiasUteis = it })
                    Text("Apenas em dias úteis (pula sábado e domingo)")
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
                        onConfirmar(inicio, intervalo ?: 1, quantidade ?: 0, apenasDiasUteis)
                    }
                }
            ) { Text("Agendar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
