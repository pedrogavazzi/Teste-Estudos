package com.pedrogavazzi.controleestudos.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Tema visual do app. */
enum class TemaApp { CLARO, ESCURO, SISTEMA }

/** Qual aba abre primeiro quando o app é iniciado. Fica no modelo de dados (não na camada de
 *  navegação) de propósito, pra essa preferência não depender de nada da UI — quem traduz isso
 *  pra uma rota de navegação é a própria tela de navegação, não este arquivo. */
enum class AbaInicial { MATERIAS, AGENDA, CADERNO, FAVORITOS }

/** Opções de antecedência do alerta, em minutos antes do horário agendado da aula. */
val OPCOES_ANTECEDENCIA_MINUTOS = listOf(0, 5, 10, 15, 30, 45, 60)

/**
 * Preferências globais do app (tema, notificações, som, antecedência do alerta), salvas em
 * SharedPreferences e expostas como StateFlow para a UI reagir automaticamente. Substitui as
 * antigas configurações por aula — agora tudo é definido uma vez, na aba Configurações, para
 * não poluir a tela de cada aula.
 */
class PreferenciasApp(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(NOME_ARQUIVO, Context.MODE_PRIVATE)

    private val _tema = MutableStateFlow(
        runCatching { TemaApp.valueOf(prefs.getString(CHAVE_TEMA, null) ?: TemaApp.SISTEMA.name) }
            .getOrDefault(TemaApp.SISTEMA)
    )
    val tema: StateFlow<TemaApp> = _tema.asStateFlow()

    // Desligada por padrão: a paleta de cores das matérias e a identidade visual roxa do app
    // foram pensadas para funcionar juntas — a cor dinâmica do Android (derivada do papel de
    // parede) pode destoar delas. O usuário pode ligar de volta em Configurações se preferir.
    private val _usarCorDinamica = MutableStateFlow(prefs.getBoolean(CHAVE_COR_DINAMICA, false))
    val usarCorDinamica: StateFlow<Boolean> = _usarCorDinamica.asStateFlow()

    fun definirUsarCorDinamica(ativo: Boolean) {
        _usarCorDinamica.value = ativo
        prefs.edit().putBoolean(CHAVE_COR_DINAMICA, ativo).apply()
    }

    private val _somAtivado = MutableStateFlow(prefs.getBoolean(CHAVE_SOM, true))
    val somAtivado: StateFlow<Boolean> = _somAtivado.asStateFlow()

    private val _notificacoesAtivadas = MutableStateFlow(prefs.getBoolean(CHAVE_NOTIFICACOES, true))
    val notificacoesAtivadas: StateFlow<Boolean> = _notificacoesAtivadas.asStateFlow()

    private val _minutosAntecedencia = MutableStateFlow(prefs.getInt(CHAVE_ANTECEDENCIA, 0))
    val minutosAntecedencia: StateFlow<Int> = _minutosAntecedencia.asStateFlow()

    // Agenda por padrão: é a tela que responde "o que eu preciso fazer hoje", que é o motivo
    // mais comum de abrir o app no dia a dia — Matérias é uma tela de configuração, usada com
    // menos frequência depois que o semestre já está cadastrado.
    private val _abaInicial = MutableStateFlow(
        runCatching { AbaInicial.valueOf(prefs.getString(CHAVE_ABA_INICIAL, null) ?: AbaInicial.AGENDA.name) }
            .getOrDefault(AbaInicial.AGENDA)
    )
    val abaInicial: StateFlow<AbaInicial> = _abaInicial.asStateFlow()

    fun definirAbaInicial(novo: AbaInicial) {
        _abaInicial.value = novo
        prefs.edit().putString(CHAVE_ABA_INICIAL, novo.name).apply()
    }

    fun definirTema(novo: TemaApp) {
        _tema.value = novo
        prefs.edit().putString(CHAVE_TEMA, novo.name).apply()
    }

    fun definirNotificacoesAtivadas(ativo: Boolean) {
        _notificacoesAtivadas.value = ativo
        prefs.edit().putBoolean(CHAVE_NOTIFICACOES, ativo).apply()
    }

    fun definirSomAtivado(ativo: Boolean) {
        _somAtivado.value = ativo
        prefs.edit().putBoolean(CHAVE_SOM, ativo).apply()
    }

    fun definirMinutosAntecedencia(minutos: Int) {
        _minutosAntecedencia.value = minutos
        prefs.edit().putInt(CHAVE_ANTECEDENCIA, minutos).apply()
    }

    // Configuração do agendamento automático: quantas aulas por dia, em quais horários (um
    // por posição do dia — precisa ter sempre o mesmo tamanho de aulasPorDia, por isso os
    // quatro valores são lidos/gravados juntos, nunca separados) e se inclui sábado/domingo.
    // Padrão: 1 aula por dia, às 19h, sem fim de semana — um ponto de partida razoável que
    // o aluno é livre pra mudar antes do primeiro uso.
    private val _configuracaoAutomatica = MutableStateFlow(lerConfiguracaoAutomatica())
    val configuracaoAutomatica: StateFlow<ConfiguracaoAgendamentoAutomatico> = _configuracaoAutomatica.asStateFlow()

    fun definirConfiguracaoAutomatica(nova: ConfiguracaoAgendamentoAutomatico) {
        _configuracaoAutomatica.value = nova
        prefs.edit()
            .putInt(CHAVE_AUTO_AULAS_POR_DIA, nova.aulasPorDia)
            .putString(CHAVE_AUTO_HORARIOS, nova.horariosMinutos.joinToString(","))
            .putBoolean(CHAVE_AUTO_SABADO, nova.incluirSabado)
            .putBoolean(CHAVE_AUTO_DOMINGO, nova.incluirDomingo)
            .apply()
    }

    private fun lerConfiguracaoAutomatica(): ConfiguracaoAgendamentoAutomatico {
        val aulasPorDia = prefs.getInt(CHAVE_AUTO_AULAS_POR_DIA, 1).coerceAtLeast(1)
        val horariosSalvos = prefs.getString(CHAVE_AUTO_HORARIOS, null)
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.filter { it in 0..1439 }
            .orEmpty()
        // Se o número de horários salvos não bate mais com aulasPorDia (nunca configurado
        // ainda, ou dado corrompido), preenche com um horário padrão (19h) repetido — a tela
        // de configuração deixa o aluno ajustar cada um antes de usar de verdade.
        val horarios = if (horariosSalvos.size == aulasPorDia) {
            horariosSalvos
        } else {
            List(aulasPorDia) { 19 * 60 }
        }
        return ConfiguracaoAgendamentoAutomatico(
            aulasPorDia = aulasPorDia,
            horariosMinutos = horarios,
            incluirSabado = prefs.getBoolean(CHAVE_AUTO_SABADO, false),
            incluirDomingo = prefs.getBoolean(CHAVE_AUTO_DOMINGO, false)
        )
    }

    private companion object {
        const val NOME_ARQUIVO = "preferencias_app"
        const val CHAVE_TEMA = "tema"
        const val CHAVE_COR_DINAMICA = "usar_cor_dinamica"
        const val CHAVE_NOTIFICACOES = "notificacoes_ativadas"
        const val CHAVE_SOM = "som_ativado"
        const val CHAVE_ANTECEDENCIA = "minutos_antecedencia"
        const val CHAVE_ABA_INICIAL = "aba_inicial"
        const val CHAVE_AUTO_AULAS_POR_DIA = "auto_aulas_por_dia"
        const val CHAVE_AUTO_HORARIOS = "auto_horarios_minutos"
        const val CHAVE_AUTO_SABADO = "auto_incluir_sabado"
        const val CHAVE_AUTO_DOMINGO = "auto_incluir_domingo"
    }
}

/**
 * Configuração do agendamento automático — [horariosMinutos] tem sempre o mesmo tamanho de
 * [aulasPorDia] (um horário por posição do dia, em minutos desde meia-noite), pra nunca gerar
 * dois horários iguais no mesmo dia.
 */
data class ConfiguracaoAgendamentoAutomatico(
    val aulasPorDia: Int,
    val horariosMinutos: List<Int>,
    val incluirSabado: Boolean,
    val incluirDomingo: Boolean
)
