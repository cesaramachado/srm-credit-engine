# Uso de IA e participação do usuário

## Materiais e orientação fornecidos pelo usuário

O usuário forneceu os documentos de referência e definiu o escopo do trabalho:

- `desafio-tecnico-srm-credit-engine-v2.md`, com o enunciado, fases, critérios de avaliação e Golden Cases.
- `docker-compose.txt`, com o Compose que já existia no ambiente local e que deveria ser considerado ao preparar a infraestrutura do projeto.
- `Texto colado.txt`, com as instruções completas de desenvolvimento: stack, arquitetura, segurança, testes, observabilidade, documentação, restrições e forma incremental de trabalho.
- O texto-base identificado pelo usuário como `SPEC.txt`, contendo as premissas financeiras, questões ao negócio, precisão numérica e critérios de aceite para organização em `SPEC.md`.

O Codex foi usado como assistente de implementação e revisão. Os documentos e as mensagens do usuário foram tratados como fonte de escopo. Regras financeiras sem resposta não foram completadas por inferência.

## Decisões e autorizações explícitas do usuário

- Pediu que o conteúdo fornecido como `SPEC.txt` fosse organizado em `SPEC.md`, com melhorias de clareza, sem inventar regras.
- Definiu que as dúvidas de negócio permanecessem sem resposta. Pediu que fossem acrescentadas ao `SPEC.md` somente perguntas ausentes, sem duplicar as existentes.
- Forneceu como premissas para os Golden Cases: prazo em meses inteiros, juros compostos, taxa base mensal de 1%, spreads de 1,5% para duplicatas e 2,5% para cheques, fórmula de valor presente, conversão BRL/USD após o cálculo em BRL, precisão decimal e arredondamento `HALF_EVEN` nos pontos descritos no texto-base.
- Autorizou iniciar o desenvolvimento depois da análise e planejamento.
- Orientou a manter um Compose próprio e isolado para o projeto. Autorizou preparar nele os serviços necessários, inclusive os que também existiam no Compose local, sem misturar os arquivos ou alterar o ambiente pessoal.
- Autorizou tentar iniciar e verificar os serviços Docker com `sudo` no WSL. Também aprovou a criação da CA local do Graylog pela interface. Nenhuma senha ou credencial fornecida durante a configuração é registrada neste arquivo.
- As instruções fornecidas excluíram API Gateway, RabbitMQ, mensageria e processamento assíncrono desta versão. Também limitaram OpenShift e Jenkins à preparação de arquivos, sem deploy.
- Pediu que o README explicasse pré-requisitos, configuração e operação de forma reproduzível em outros ambientes, não apenas na máquina local.
- Pediu a verificação dos endpoints da API, dos testes automatizados, das métricas no Grafana, dos logs no Graylog, da autenticação JWT, de idempotência e N+1, além da tentativa de simular o Circuit Breaker.

Esses pontos distinguem as decisões e autorizações do usuário das escolhas técnicas feitas durante a implementação. A taxa base para operações reais, a política de atualização e seleção cambial, a regra do prazo derivado do vencimento, liquidações parciais e permissões detalhadas por perfil continuam pendentes de decisão do negócio.

## Como a IA foi utilizada

O Codex apoiou a análise do desafio, a organização da arquitetura Java/Spring, a implementação da API e das estratégias de preço, persistência, autenticação, configuração de observabilidade, infraestrutura Compose, pipeline/manifests preparatórios, documentação e testes. Também executou verificações e atualizou a documentação conforme as orientações posteriores do usuário.

O Codex não tomou as decisões de negócio em aberto. Como a aplicação ainda não possui integração com provedor cambial nem endpoint de liquidação, não foram inventados fluxo externo, seleção de cotação, política de liquidação ou comportamento de Circuit Breaker para completar artificialmente os requisitos.

## Erros e sugestões corrigidos

1. A primeira configuração Maven usou `flyway-database-sqlserver`, que não estava gerenciado pelo Spring Boot. A resolução/build identificou o problema e a dependência foi corrigida para `flyway-sqlserver`.
2. O teste SQL Server usou a configuração de nome de banco não suportada por `MSSQLServerContainer` na versão de Testcontainers adotada. O erro foi reproduzido e a configuração removida; o teste de persistência passou no SQL Server do Testcontainers.
3. A primeira configuração OAuth do Swagger passou um `Map` para o campo `Scopes` do springdoc. A compilação apontou o tipo incompatível e a configuração foi ajustada para o tipo esperado pelo OpenAPI.
4. Uma versão anterior deste arquivo dizia que o Docker/Testcontainers não haviam sido executados. Essa informação ficou desatualizada depois que o Docker no WSL foi iniciado com a autorização do usuário. O registro foi corrigido: a execução completa de `mvn verify` no ambiente com Docker terminou com 40 testes, sem falhas, erros ou testes ignorados; Checkstyle e o gate de cobertura JaCoCo também passaram.

Esses casos foram detectados por compilação, execução de testes ou conferência do estado real do ambiente. A saída gerada pela IA não foi considerada correta sem validação.

## Validações solicitadas e limites observados

- Foram exercitados os endpoints existentes e os controles JWT, com tokens válidos, ausentes, inválidos e de issuer incorreto.
- Prometheus encontrou os alvos configurados; o dashboard provisionado foi aberto no Grafana e as métricas da aplicação foram consultadas.
- A busca do Graylog mostrou logs emitidos pela API e a mensagem de verificação GELF.
- A repetição do cadastro de recebível com o mesmo `Idempotency-Key` gerou outro registro. A API não implementa idempotência; em particular, ainda não há endpoint de liquidação para implementar ou validar a idempotência exigida para liquidações.
- A inspeção do modelo não encontrou relacionamentos ORM que produzam N+1 nas consultas atuais; o histórico de cotações usa paginação. Essa observação não substitui um teste de carga.
- Não foi possível simular um Circuit Breaker funcional: não há cliente de provedor cambial nem chamada externa protegida na versão implementada. Resilience4j está no build, mas não há circuito ativo.
- Os arquivos de Jenkins e OpenShift são preparatórios. Não foi executado pipeline Jenkins nem realizado deploy OpenShift.

## Responsabilidade pela solução

O usuário forneceu o desafio, as instruções e as premissas iniciais, delimitou o escopo, manteve as regras de negócio não respondidas como pendências e autorizou as ações de infraestrutura registradas acima. O Codex auxiliou na implementação, análise e validação. A autoria e a responsabilidade por compreender e defender as decisões do projeto permanecem com o usuário.
