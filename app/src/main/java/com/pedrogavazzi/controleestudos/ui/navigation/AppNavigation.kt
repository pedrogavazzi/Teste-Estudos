package com.pedrogavazzi.controleestudos.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.pedrogavazzi.controleestudos.ui.agenda.AgendaScreen
import com.pedrogavazzi.controleestudos.ui.agenda.AgendaViewModel
import com.pedrogavazzi.controleestudos.ui.caderno.CadernoEditorScreen
import com.pedrogavazzi.controleestudos.ui.caderno.CadernoEditorViewModel
import com.pedrogavazzi.controleestudos.ui.caderno.CadernoScreen
import com.pedrogavazzi.controleestudos.ui.caderno.CadernoViewModel
import com.pedrogavazzi.controleestudos.ui.configuracoes.ConfiguracoesScreen
import com.pedrogavazzi.controleestudos.ui.configuracoes.ConfiguracoesViewModel
import com.pedrogavazzi.controleestudos.ui.desempenho.DesempenhoScreen
import com.pedrogavazzi.controleestudos.ui.desempenho.DesempenhoViewModel
import com.pedrogavazzi.controleestudos.ui.materiadetail.MateriaDetailScreen
import com.pedrogavazzi.controleestudos.ui.materias.MateriasScreen
import com.pedrogavazzi.controleestudos.ui.materias.MateriasViewModel
import com.pedrogavazzi.controleestudos.ControleEstudosApp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/**
 * @param rotaInicial em qual aba o app abre — normalmente vem da preferência do usuário
 *   (Configurações > Aba inicial), lida uma vez no início do app.
 * @param aulaIdParaAbrirCaderno id de aula vindo de um toque em notificação (deep link);
 *   [onAulaAbertaPeloDeepLink] avisa que já foi tratado.
 */
@Composable
fun AppNavigation(
    rotaInicial: String = Destino.Agenda.rota,
    aulaIdParaAbrirCaderno: Long? = null,
    onAulaAbertaPeloDeepLink: () -> Unit = {}
) {
    val navController = rememberNavController()

    // Veio de um toque na notificação: abre direto o caderno daquela aula, por cima de
    // qualquer tela em que o app estivesse.
    LaunchedEffect(aulaIdParaAbrirCaderno) {
        if (aulaIdParaAbrirCaderno != null) {
            navController.navigate(Destino.CadernoEditor.rotaComId(aulaIdParaAbrirCaderno, somenteLeitura = false))
            onAulaAbertaPeloDeepLink()
        }
    }

    Scaffold(
        bottomBar = { BarraNavegacaoInferior(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = rotaInicial,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destino.Materias.rota) {
                val viewModel: MateriasViewModel = viewModel()
                MateriasScreen(
                    viewModel = viewModel,
                    onAbrirMateria = { id -> navController.navigate(Destino.MateriaDetail.rotaComId(id)) }
                )
            }
            composable(Destino.Agenda.rota) {
                val viewModel: AgendaViewModel = viewModel()
                AgendaScreen(
                    viewModel = viewModel,
                    onAbrirCaderno = { aulaId -> navController.navigate(Destino.CadernoEditor.rotaComId(aulaId, somenteLeitura = false)) }
                )
            }
            composable(Destino.Caderno.rota) {
                val viewModel: CadernoViewModel = viewModel()
                CadernoScreen(
                    viewModel = viewModel,
                    onAbrirAula = { aulaId -> navController.navigate(Destino.CadernoEditor.rotaComId(aulaId, somenteLeitura = false)) },
                    onIrParaAgenda = { navegarParaAba(navController, Destino.Agenda.rota) }
                )
            }
            composable(Destino.Desempenho.rota) {
                val viewModel: DesempenhoViewModel = viewModel()
                DesempenhoScreen(
                    viewModel = viewModel,
                    onIrParaMaterias = { navegarParaAba(navController, Destino.Materias.rota) }
                )
            }
            composable(Destino.Configuracoes.rota) {
                val viewModel: ConfiguracoesViewModel = viewModel()
                ConfiguracoesScreen(viewModel = viewModel)
            }
            composable(
                route = Destino.MateriaDetail.rota,
                arguments = listOf(navArgument("materiaId") { type = NavType.LongType })
            ) { backStackEntry ->
                val materiaId = backStackEntry.arguments?.getLong("materiaId") ?: return@composable
                MateriaDetailScreen(
                    materiaId = materiaId,
                    onVoltar = { navController.popBackStack() },
                    onAbrirCadernoDaAula = { aulaId -> navController.navigate(Destino.CadernoEditor.rotaComId(aulaId, somenteLeitura = true)) }
                )
            }
            composable(
                route = Destino.CadernoEditor.rota,
                arguments = listOf(
                    navArgument("aulaId") { type = NavType.LongType },
                    navArgument("somenteLeitura") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val aulaId = backStackEntry.arguments?.getLong("aulaId") ?: return@composable
                val somenteLeitura = backStackEntry.arguments?.getBoolean("somenteLeitura") ?: false
                val context = LocalContext.current
                val repository = (context.applicationContext as ControleEstudosApp).repository
                val viewModel: CadernoEditorViewModel = viewModel(
                    key = "caderno_editor_$aulaId",
                    factory = viewModelFactory {
                        initializer { CadernoEditorViewModel(repository, aulaId) }
                    }
                )
                CadernoEditorScreen(
                    aulaId = aulaId,
                    somenteLeituraInicial = somenteLeitura,
                    viewModel = viewModel,
                    onVoltar = { navController.popBackStack() }
                )
            }
        }
    }
}

/** Navega para uma aba do menu inferior preservando o estado de rolagem/formulário das
 *  outras abas — mesma lógica usada tanto ao tocar num item do menu quanto ao navegar
 *  programaticamente de uma tela pra outra (ex.: do estado vazio de uma aba pra outra),
 *  pra não duplicar essa configuração em mais de um lugar. */
private fun navegarParaAba(navController: NavHostController, rota: String) {
    navController.navigate(rota) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun BarraNavegacaoInferior(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = backStackEntry?.destination

    val destinosVisiveisComBarra = setOf(
        Destino.Materias.rota, Destino.Agenda.rota, Destino.Caderno.rota,
        Destino.Desempenho.rota, Destino.Configuracoes.rota
    )
    val mostrarBarra = rotaAtual?.hierarchy?.any { it.route in destinosVisiveisComBarra } == true

    if (mostrarBarra) {
        NavigationBar {
            itensNavegacaoInferior.forEach { item ->
                val selecionado = rotaAtual?.hierarchy?.any { it.route == item.destino.rota } == true
                NavigationBarItem(
                    selected = selecionado,
                    onClick = { navegarParaAba(navController, item.destino.rota) },
                    icon = { androidx.compose.material3.Icon(item.icone, contentDescription = item.rotulo) },
                    label = {
                        Text(
                            item.rotulo,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            softWrap = false,
                            // Reduzido de propósito: com 5 abas na barra, o rótulo "Desempenho"
                            // não cabia no tamanho padrão e cortava no meio ("Desemp...").
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                        )
                    }
                )
            }
        }
    }
}
