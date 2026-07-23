package com.pedrogavazzi.controleestudos.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import java.util.Calendar

/**
 * Abre o seletor nativo de data e depois de horário, retornando o instante escolhido
 * em milissegundos (epoch). Usado para agendar ou reagendar uma aula em qualquer
 * dia e horário do calendário.
 */
fun abrirSeletorDeDataEHora(
    context: Context,
    dataHoraInicialMillis: Long? = null,
    onDataHoraSelecionada: (Long) -> Unit
) {
    val calendario = Calendar.getInstance()
    if (dataHoraInicialMillis != null) {
        calendario.timeInMillis = dataHoraInicialMillis
    }

    val ano = calendario.get(Calendar.YEAR)
    val mes = calendario.get(Calendar.MONTH)
    val dia = calendario.get(Calendar.DAY_OF_MONTH)
    val hora = calendario.get(Calendar.HOUR_OF_DAY)
    val minuto = calendario.get(Calendar.MINUTE)

    DatePickerDialog(
        context,
        { _, anoEscolhido, mesEscolhido, diaEscolhido ->
            TimePickerDialog(
                context,
                { _, horaEscolhida, minutoEscolhido ->
                    val resultado = Calendar.getInstance().apply {
                        set(anoEscolhido, mesEscolhido, diaEscolhido, horaEscolhida, minutoEscolhido, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onDataHoraSelecionada(resultado.timeInMillis)
                },
                hora,
                minuto,
                true
            ).show()
        },
        ano,
        mes,
        dia
    ).show()
}
