# CicloVida — App de Ciclo Menstrual

App Android nativo (Kotlin + Jetpack Compose), **100% local e offline** — sem
permissão de internet no Manifest, sem servidor, sem conta. Arquitetura MVVM +
Clean Architecture, igual ao padrão já validado no projeto anterior.

## Diferenciais do app

1. **Previsão inteligente baseada no histórico real da usuária** (`CycleCalculator`
   + `PredictNextCycleUseCase`): não usa um ciclo fixo de 28 dias — calcula uma
   média ponderada dos últimos ciclos (dando mais peso aos recentes), mede o
   desvio padrão para dar uma faixa de confiança honesta, e a confiança da
   previsão (`PredictionConfidence`) aumenta conforme mais ciclos são registrados.
2. **Insights holísticos** (`GenerateHolisticInsightsUseCase`): cruza humor,
   energia e sintomas registrados com a fase do ciclo em que ocorreram, revelando
   padrões próprios da usuária (ex.: "energia mais baixa na fase lútea").
3. **Privacidade como prioridade de design**: Room local, PIN guardado com
   `EncryptedSharedPreferences` (Android Keystore) em vez de preferências simples,
   e um toggle de **Modo discreto** nos Ajustes (disfarçar ícone/nome do app —
   ver TODO abaixo, a troca de ícone em si ainda não está implementada).

## Estrutura

Mesma separação em camadas do projeto anterior: `domain` (modelos, contratos,
regras de negócio puras) → `data` (Room + implementações) → `presentation`
(Compose + ViewModels), unidas por Hilt.

## O que falta implementar (marcado como TODO ou ausente)

1. **Tela de calendário mensal** com visualização dos dias de fluxo, janela
   fértil e previsão sobrepostos — hoje a Home mostra só o resumo textual.
2. **Troca real do ícone/nome do launcher** para o Modo Discreto: implementar via
   múltiplos `<activity-alias>` no Manifest + `PackageManager.setComponentEnabledSetting`
   alternando entre eles (o toggle na tela de Ajustes já está pronto, só falta
   ligar essa parte específica do Android).
3. **Notificações locais** (WorkManager já está nas dependências) avisando 1-2
   dias antes da menstruação prevista, ou lembrete diário pra registrar.
4. **BiometricPrompt real**: o botão "Usar biometria" existe na UI mas o disparo
   do prompt do sistema operacional precisa ser implementado na Activity.
5. **Edição de registros de dias passados**: hoje o registro diário sempre edita
   o dia de hoje; falta permitir navegar pra datas anteriores a partir da Home
   ou de um calendário.
6. **Exportação de dados** (ex.: PDF pra levar ao ginecologista) — natural
   funcionalidade de privacidade (dados são da usuária, ela decide exportar).

## Build

Mesmo fluxo já validado: `./gradlew assembleDebug`, ou via GitHub Actions
(`.github/workflows/build.yml` já incluído, roda automaticamente a cada push).
