# Controle de Estudos

Aplicativo Android (Kotlin + Jetpack Compose + Room) para organizar matérias, agendar cada aula individualmente no calendário, receber alertas, marcar conclusão, reagendar aulas perdidas e acompanhar o desempenho geral e por matéria.

## Funcionalidades

- Cadastro de matérias com nome e número de aulas (sem limite — suporta 30+ matérias e qualquer quantidade de aulas).
- Cada aula (Aula 1, Aula 2, ...) pode ser agendada em qualquer dia e horário do calendário, individualmente.
- Alerta de notificação por aula, com opção de ativar/desativar.
- Marcação de conclusão de cada aula.
- Reagendamento de aulas não concluídas (contador de quantas vezes foi reagendada).
- Aba "Agenda": todas as aulas agendadas, em ordem cronológica, agrupadas por dia.
- Aba "Desempenho": total de aulas concluídas vs. total geral, e o mesmo por matéria — recalculado automaticamente a qualquer mudança (novas aulas, conclusões, reagendamentos, exclusões).
- Tela de cada matéria lista suas aulas com campo de observação livre, editável a qualquer momento.
- Funciona 100% offline (banco de dados local Room/SQLite, sem backend ou login).

## Stack técnica

- Kotlin, Jetpack Compose (Material 3), Navigation Compose
- Room (SQLite local)
- AlarmManager + BroadcastReceiver para os alertas exatos, com BootReceiver para restaurar os alarmes após reiniciar o aparelho
- minSdk 26 (Android 8.0), targetSdk 34
- Sem dependências de nuvem

## Como abrir e compilar

1. Instale o [Android Studio](https://developer.android.com/studio) (versão recente, Ladybug ou superior).
2. Abra a pasta `ControleDeEstudos` como projeto (File > Open).
3. Deixe o Android Studio sincronizar o Gradle na primeira abertura — ele gera automaticamente o `gradlew`/`gradle-wrapper.jar` que não é versionado aqui.
4. Rode em um emulador ou aparelho físico (Run ▶). O `applicationId` é `com.pedrogavazzi.controleestudos`.

Este projeto foi escrito e revisado manualmente (sem acesso a internet/Android SDK no ambiente de geração, então não foi compilado automaticamente aqui). Em caso de algum erro pontual de sincronização do Gradle na primeira abertura, normalmente basta deixar o Android Studio baixar as dependências declaradas em `app/build.gradle.kts`.

## Build no GitHub

O repositório já está pronto para subir direto ao GitHub e buildar por lá:

- **`.github/workflows/android-ci.yml`**: workflow do GitHub Actions que roda a cada push/PR na branch `main` (e também manualmente, em Actions > Android CI > Run workflow). Ele instala JDK 17, Android SDK e Gradle, compila `assembleDebug`, roda os testes unitários e publica o APK gerado como artefato do job — vá em **Actions > (execução) > Artifacts > app-debug-apk** para baixá-lo.
- **`gradlew` / `gradlew.bat`**: presentes no repositório. O `gradle-wrapper.jar` (arquivo binário) **não** está commitado — o próprio workflow o gera no início de cada execução (`gradle wrapper --gradle-version 8.7`), e o Android Studio também o regenera sozinho na primeira sincronização caso você abra o projeto localmente. Se quiser rodar `./gradlew` localmente antes disso, gere o jar uma vez com `gradle wrapper --gradle-version 8.7` (requer o Gradle instalado) ou simplesmente abra o projeto no Android Studio primeiro.
- **`.gitignore`**: já exclui `build/`, `.gradle/`, `local.properties` e o `gradle-wrapper.jar`, então o `git add .` na raiz do projeto é seguro.
- **`app/debug.keystore`**: keystore de debug commitado no repositório (mesmo alias/senha padrão do Android: `androiddebugkey`/`android`), configurado em `app/build.gradle.kts` como assinatura do `buildType debug`. Isso garante que o APK de debug tenha sempre a mesma assinatura, não importa em qual máquina ou execução do Actions ele for gerado — sem isso, cada ambiente geraria seu próprio keystore automaticamente, e trocar de máquina/CI faria o Android bloquear a instalação por cima de uma versão já instalada (assinaturas diferentes). Não precisa mexer nele.

Para subir:

```bash
cd ControleDeEstudos
git init
git add .
git commit -m "Controle de Estudos: app inicial"
git branch -M main
git remote add origin <URL_DO_SEU_REPOSITORIO>
git push -u origin main
```

Assim que o push terminar, o Actions já deve começar a rodar automaticamente.

## Permissões solicitadas

- **Notificações** (Android 13+): pedida na primeira abertura do app.
- **Alarmes exatos** (Android 12+): o app orienta o usuário a liberar em Configurações caso não esteja concedida, para que os alertas toquem no horário exato agendado.

## Estrutura do projeto

```
app/src/main/java/com/pedrogavazzi/controleestudos/
├── data/            Entidades Room (Materia, Aula), DAOs, banco e repositório
├── notifications/   Agendamento de alarmes e notificações
└── ui/
    ├── materias/       Lista de matérias (criar/editar/excluir)
    ├── materiadetail/  Lista de aulas de uma matéria (agendar, concluir, reagendar, alerta, observação)
    ├── agenda/         Todas as aulas agendadas, em ordem cronológica
    ├── desempenho/      Painel de desempenho geral e por matéria
    ├── navigation/      Navegação entre as abas
    └── theme/           Cores, tipografia e tema Material 3
```

## Próximos passos sugeridos

- Adicionar testes instrumentados (Compose UI tests) para as telas principais.
- Configurar assinatura de release e publicação automática do APK/AAB no workflow, se for gerar uma versão para distribuição.
