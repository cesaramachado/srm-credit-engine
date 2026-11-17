# Decisões técnicas

## Decisões em vigor

- **Escopo:** backend Java com API REST. O projeto não inclui interface gráfica; o pedido de desenvolvimento define a aplicação como exclusivamente REST.
- **Arquitetura:** monólito organizado em camadas e pacotes por domínio, sem Gateway, mensageria ou processamento assíncrono.
- **Stack:** Java 21, Spring Boot 3, Maven, Spring Data JPA, Hibernate, SQL Server e Flyway, conforme as instruções do projeto.
- **Precificação:** Strategy por tipo de recebível. O motor usa `BigDecimal`, representa 1% como `0.01` e arredonda valores monetários com `HALF_EVEN` para duas casas. No fluxo BRL/USD, arredonda o VP em BRL antes de converter.
- **Persistência de valores:** SQL Server `DECIMAL(38,2)` para valores monetários e `DECIMAL(38,18)` para taxas/cotações. São escalas técnicas iniciais; devem ser revistas se o negócio definir limites máximos ou precisão diferente.
- **Cotações:** armazenamento histórico com par de moedas e vigência. A cotação de referência dos Golden Cases é expressa como USD para BRL; o valor em BRL é dividido pela cotação para chegar ao valor em USD.
- **Autenticação:** Spring Security Resource Server valida JWT do Keycloak. O conversor lê roles de realm e de client. As permissões por operação aguardam a definição das roles do negócio.
- **Swagger:** autenticação OIDC Authorization Code com PKCE para o client local; não habilita fluxo de senha.
- **Métricas:** Actuator/Micrometer expõem health, HTTP, JVM, pool JDBC e duração de simulações. Prometheus usa credencial Basic dedicada somente em `/actuator/**`; o restante da API aceita JWT.
- **Logs:** JSON GELF no console; perfil `graylog` encaminha por GELF UDP usando fila Logback que não bloqueia a operação. Um filtro valida ou cria `X-Correlation-ID` e o inclui no MDC.
- **Redis:** serviço e Redis Exporter incluídos no Compose. O app verifica a saúde do Redis, Prometheus coleta métricas do Exporter e Grafana mostra memória/operações. Nenhum dado é armazenado em cache até haver política de invalidação e consistência.
- **Compose:** ambiente versionado e isolado, com SQL Server, Redis, PostgreSQL para Keycloak, Keycloak, Prometheus, Grafana e Graylog com MongoDB/Data Node. RabbitMQ foi excluído por restrição explícita; MySQL não é dependência deste backend.
- **Operação:** o Compose usa portas de host alternativas e loopback para não disputar as portas comuns do Compose pessoal. Graylog requer ajuste do `vm.max_map_count`, preflight do Data Node e criação da entrada GELF na interface.
- **CI/OpenShift:** Jenkinsfile executa testes, Checkstyle, `verify` e o gate JaCoCo; manifests de OpenShift são preparatórios e não foram aplicados.
- **Escopo da simulação:** o endpoint recebe taxa base, prazo e câmbio explicitamente. Não seleciona cotação nem deriva prazo do vencimento; são decisões pendentes.
- **Integração cambial externa:** ainda não existe cliente de provedor; sem uma decisão sobre atualização manual/automática, não há chamada de rede real para proteger. Resilience4j está no build para a futura implementação, sem circuit breaker ativo neste estágio.
- **API Gateway:** avaliado e não adotado. O escopo contém uma única API REST; um Gateway acrescentaria operação sem benefício proporcional. Pode ser reconsiderado se surgirem múltiplos serviços, roteamento centralizado ou políticas compartilhadas.
- **Testes de banco:** Testcontainers com SQL Server para isolamento. A suite JUnit/Testcontainers ainda não foi reexecutada após habilitar Docker no WSL. O Compose iniciou SQL Server e a API aplicou as migrations Flyway, ficando saudável no banco local.

## Decisões de implementação ainda pendentes

- Valor/origem da taxa base para operações reais e política de atualização. O 1% ao mês permanece específico dos Golden Cases.
- Instante usado para escolher a cotação cambial, comportamento quando não existir uma cotação aplicável e convenção final dos intervalos de vigência.
- Regra para derivar meses a partir da data de vencimento; aceitação de prazo fracionado.
- Possibilidade de liquidações parciais ou múltiplas para um recebível.
- Roles do Keycloak e quais operações cada role poderá executar.
- Precisão máxima e limites operacionais para valor de face, prazo, taxas e cotações.
- Política de cache para dados de taxa e cotação; Redis está disponível, mas não é usado para manter cache.

Esses pontos não foram convertidos em regras de liquidação. A implementação da liquidação depende das definições necessárias para evitar assumir comportamento financeiro.
