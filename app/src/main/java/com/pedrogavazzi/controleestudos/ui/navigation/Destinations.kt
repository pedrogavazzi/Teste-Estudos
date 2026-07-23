package com.pedrogavazzi.controleestudos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destino(val rota: String) {
    data object Materias : Destino("materias")
    data object Agenda : Destino("agenda")
    data object Caderno : Destino("caderno")
    data object Desempenho : Destino("desempenho")
    data object MateriaDetail : Destino("materia/{materiaId}") {
        fun rotaComId(materiaId: Long) = "materia/$materiaId"
    }
    data object CadernoEditor : Destino("caderno_editor/{aulaId}") {
        fun rotaComId(aulaId: Long) = "caderno_editor/$aulaId"
    }
}

data class ItemNavegacao(val destino: Destino, val rotulo: String, val icone: ImageVector)

val itensNavegacaoInferior = listOf(
    ItemNavegacao(Destino.Materias, "Matérias", Icons.Filled.MenuBook),
    ItemNavegacao(Destino.Agenda, "Agenda", Icons.Filled.CalendarMonth),
    ItemNavegacao(Destino.Caderno, "Caderno", Icons.Filled.EditNote),
    ItemNavegacao(Destino.Desempenho, "Desempenho", Icons.Filled.BarChart)
)
