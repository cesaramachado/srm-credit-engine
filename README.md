# SRM Credit Engine

API REST síncrona para cadastro de recebíveis, histórico local de cotações e simulação de preço. O projeto é um monólito Java organizado por domínio e camadas.

## Requisitos

- JDK 21.
- Maven 3.6.3 ou superior.
- Docker Engine e Docker Compose v2 para o ambiente completo.
- Acesso a um registry de imagens e ao Maven Central na primeira execução.
- Para Graylog Data Node, vm.max_map_count de pelo menos 262144 no kernel que executa o Docker Engine.

O Compose foi preparado para desenvolvimento e teste local. As portas publicadas ficam em 127.0.0.1; para uma implantação remota, configure exposição de rede, DNS, TLS e URLs públicas para o ambiente escolhido antes de iniciar os serviços.

## Configuração do ambiente

### Arquivo .env

Copie .env.example para .env e substitua todos os valores de exemplo. Não versione .env; mantenha-o em um cofre de segredos ou no armazenamento protegido da máquina.

PowerShell:

~~~powershell
Copy-Item .env.example .env
~~~

Linux, macOS ou WSL:

~~~bash
cp .env.example .env
~~~

Crie um valor novo e exclusivo para cada senha. As senhas de SQL Server, Redis e Keycloak devem usar somente letras e números; para satisfazer a política de senha do SQL Server, use maiúsculas, minúsculas e dígitos. Um gerador disponível em Bash é:

~~~bash
printf 'Aq9%s\n' "$(openssl rand -hex 32)"
~~~

Use a linha inteira produzida. Repita o processo para cada segredo. GRAYLOG_PASSWORD_SECRET deve ter no mínimo 64 caracteres; openssl rand -hex 32 produz esse tamanho.

O Graylog recebe o hash SHA-256 da senha escolhida para o administrador, não a senha em texto puro. Em Bash, calcule-o sem exibir a senha:

~~~bash
read -rsp 'Senha do administrador Graylog: ' graylog_admin_password
printf '\n'
printf '%s' "$graylog_admin_password" | sha256sum
unset graylog_admin_password
~~~

Copie os 64 caracteres hexadecimais do hash para GRAYLOG_ROOT_PASSWORD_SHA2 em .env. No PowerShell, é possível calcular o mesmo hash assim:

~~~powershell
$secure = Read-Host 'Senha do administrador Graylog' -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try {
    $plain = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    $sha = [Security.Cryptography.SHA256]::Create()
    $hash = [BitConverter]::ToString(
        $sha.ComputeHash([Text.Encoding]::UTF8.GetBytes($plain))
    ).Replace('-', '').ToLowerInvariant()
    $hash
}
finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
}
~~~

Configure em .env:

| Variável | Uso |
|---|---|
| MSSQL_SA_PASSWORD | Senha do administrador do SQL Server. |
| DB_NAME, DB_USERNAME, DB_PASSWORD | Banco e credenciais usados pela API. |
| REDIS_PASSWORD | Autenticação do Redis. |
| KEYCLOAK_DB_NAME, KEYCLOAK_DB_USERNAME, KEYCLOAK_DB_PASSWORD | Banco PostgreSQL do Keycloak. |
| KEYCLOAK_ADMIN_USERNAME, KEYCLOAK_ADMIN_PASSWORD | Administração local do Keycloak. |
| KEYCLOAK_PUBLIC_URL | URL do Keycloak acessível ao cliente e valor-base do issuer dos tokens. |
| ACTUATOR_METRICS_USERNAME, ACTUATOR_METRICS_PASSWORD | Acesso Basic Auth às métricas da API e credencial usada pelo Prometheus. |
| GRAYLOG_PASSWORD_SECRET, GRAYLOG_ROOT_PASSWORD_SHA2 | Segredo de sessão e hash da senha administrativa do Graylog. |
| GRAYLOG_PUBLIC_URL | URL pública usada pelo Graylog. |
| GRAFANA_ADMIN_PASSWORD | Senha do administrador local do Grafana. |
| SQLSERVER_HOST_PORT, REDIS_HOST_PORT, KEYCLOAK_HOST_PORT, APP_HOST_PORT, PROMETHEUS_HOST_PORT, GRAFANA_HOST_PORT, GRAYLOG_HOST_PORT, GRAYLOG_GELF_UDP_HOST_PORT | Portas publicadas na máquina host. |

Mantenha nomes de bancos, usuários e clientes simples, sem espaços. Se alterar portas, atualize as URLs públicas correspondentes. Se alterar APP_HOST_PORT, atualize também redirectUris e webOrigins do cliente Swagger em infra/keycloak/realm-import/srm-credit-engine-realm.json.

### Execução fora do Docker Compose

O perfil `local` habilita a configuração do SQL Server e Redis. Para executar o JAR em outra máquina ou plataforma, disponibilize esses serviços e configure as variáveis abaixo no ambiente do processo. Não dependa de `.env`: Spring Boot lê essas configurações das variáveis de ambiente ou do mecanismo de configuração usado pela plataforma.

| Variável | Obrigatória | Configuração |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Sim | `local`; acrescente `graylog` somente se o envio GELF estiver configurado. |
| `DB_URL` | Sim | JDBC URL acessível pela aplicação, por exemplo `jdbc:sqlserver://<host>:1433;databaseName=<banco>;encrypt=true;trustServerCertificate=true` para ambiente local. |
| `DB_USERNAME`, `DB_PASSWORD` | Sim | Login do banco e sua senha. O login precisa criar/aplicar as migrations Flyway na inicialização. |
| `KEYCLOAK_ISSUER_URI` | Sim | URL do realm emissor dos JWTs, correspondente exatamente ao claim `iss`. |
| `KEYCLOAK_JWK_SET_URI` | Sim | URL do endpoint de chaves públicas acessível pela aplicação. |
| `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT` | Sim no perfil `local` | Host e porta do Redis acessíveis pela aplicação. |
| `SPRING_DATA_REDIS_PASSWORD` | Se exigida pelo Redis | Senha do Redis. |
| `ACTUATOR_METRICS_USERNAME`, `ACTUATOR_METRICS_PASSWORD` | Sim | Credenciais Basic Auth para `/actuator/metrics` e `/actuator/prometheus`. |
| `SERVER_PORT` | Não | Porta HTTP; padrão `8080`. |
| `KEYCLOAK_CLIENT_ID` | Não | Cliente API; padrão `srm-credit-engine-api`. |
| `KEYCLOAK_PUBLIC_URL` | Não | URL pública do Keycloak usada na documentação OpenAPI; padrão `http://localhost:18180`. |
| `KEYCLOAK_SWAGGER_CLIENT_ID` | Não | Cliente OAuth do Swagger; padrão `srm-credit-engine-swagger`. |
| `GRAYLOG_HOST`, `GRAYLOG_PORT` | Se o perfil `graylog` estiver ativo | Host e porta UDP GELF acessíveis pela aplicação; padrão `graylog:12201`. |

Use o endereço de banco e Redis correspondente à rede do processo: `localhost` quando executado diretamente no host, ou nomes DNS de serviço quando executado na mesma rede de containers. O issuer deve continuar sendo a URL publicada pelo Keycloak para que o `iss` do token corresponda, ainda que a URL do JWK seja interna. Configure o banco para aceitar o login da aplicação e permita a execução de migrations. Mantenha as credenciais no gerenciador de segredos da plataforma.

Com as variáveis definidas, compile e inicie a aplicação:

~~~bash
mvn verify
mvn spring-boot:run
~~~

Para executar o JAR já compilado, use `java -jar target/srm-credit-engine-0.1.0-SNAPSHOT.jar` com o mesmo ambiente configurado. Em plataformas sem Maven, JDK ou Docker no host, use a imagem construída pelo Dockerfile; nesse caso, forneça as mesmas variáveis no runtime e conectividade de rede para SQL Server, Redis e Keycloak.

### Requisito do Graylog

Defina vm.max_map_count no kernel que executa o Docker Engine antes de subir o Data Node:

~~~bash
sudo sysctl -w vm.max_map_count=262144
~~~

Em Docker Desktop, aplique o ajuste à VM Linux usada pelo Docker, conforme a documentação da plataforma. A alteração por sysctl pode não persistir após reiniciar o host/VM; configure-a no mecanismo de inicialização do sistema se necessário.

## Inicialização com Docker Compose

Execute os comandos na pasta do projeto. O Compose isolado usa somente os serviços declarados em compose.yaml; ele não altera outros projetos Compose da máquina.

PowerShell:

~~~powershell
docker compose --env-file .env config --quiet
docker compose --env-file .env up --build -d
docker compose --env-file .env ps
~~~

Linux, macOS ou WSL:

~~~bash
docker compose --env-file .env config --quiet
docker compose --env-file .env up --build -d
docker compose --env-file .env ps
~~~

Se a conta local não tiver permissão para acessar o Docker Engine em Linux/WSL, prefixe os comandos Docker por sudo. Aguarde até que os serviços com healthcheck estejam saudáveis. Consulte logs com:

~~~bash
docker compose --env-file .env logs -f app
docker compose --env-file .env logs -f sqlserver-init
docker compose --env-file .env logs -f graylog graylog-datanode
~~~

O serviço sqlserver-init cria o banco e o login da aplicação na primeira inicialização. O Flyway aplica o esquema da aplicação. O login local da aplicação recebe db_owner para permitir as migrations; não reutilize essa configuração de privilégio em produção.

Os dados são mantidos em volumes nomeados do Compose. docker compose down para e remove os containers, preservando esses volumes. docker compose down -v remove também os volumes e apaga o banco e as configurações persistidas; use apenas quando quiser reinicializar o ambiente e puder perder esses dados.

### Configuração inicial do Graylog

Em uma instalação com volumes Graylog novos:

1. Abra http://localhost:<GRAYLOG_HOST_PORT> e entre com o usuário admin e a senha cujo hash foi configurado em GRAYLOG_ROOT_PASSWORD_SHA2.
2. Conclua o preflight do Data Node. Crie a CA local e provisione o certificado do Data Node pelo assistente do Graylog.
3. Em System → Inputs, crie uma entrada GELF UDP com bind address 0.0.0.0 e porta 12201.
4. Termine o assistente e inicie a entrada. A entrada em setup mode ainda não recebe mensagens.
5. Selecione um stream existente para o roteamento; o Default Stream é a opção padrão do assistente.

A API envia GELF pela rede Compose para graylog:12201. A porta host GRAYLOG_GELF_UDP_HOST_PORT encaminha UDP para a porta interna 12201 e recebe emissores externos locais. O appender GELF assíncrono não bloqueia requisições de negócio se a coleta de logs estiver indisponível.

## Keycloak e JWT

O Compose importa o realm srm-credit-engine e os clientes definidos em infra/keycloak/realm-import/srm-credit-engine-realm.json. No console do Keycloak:

1. Crie um usuário local no realm srm-credit-engine.
2. Atribua a role de cliente srm-credit-engine-api:OPERADOR.
3. Use o cliente srm-credit-engine-swagger para o fluxo Authorization Code com PKCE no Swagger UI.

Todas as rotas de negócio exigem um Bearer JWT assinado pelo realm. A API valida assinatura, issuer e validade do token; as roles do realm e do cliente são convertidas para autoridades Spring. A política de autorização por role ainda não foi definida, então as rotas autenticadas não diferenciam essas roles.

O iss do JWT deve corresponder exatamente a KEYCLOAK_ISSUER_URI usado pela API. No Compose, KEYCLOAK_PUBLIC_URL fornece a URL pública do issuer e KEYCLOAK_JWK_SET_URI usa a rede interna para buscar as chaves. Fora do Compose, configure KEYCLOAK_ISSUER_URI e KEYCLOAK_JWK_SET_URI para URLs alcançáveis pela API e mantenha o issuer igual ao claim iss emitido pelo Keycloak. Se mudar APP_HOST_PORT, ajuste o redirect URI e a origem web do cliente Swagger.

## Endpoints

| Método | Caminho | Acesso | Função |
|---|---|---|---|
| POST | /api/v1/pricing/simulations | Bearer JWT | Simula o preço com prazo, taxa base, tipo e moeda enviados na requisição. |
| POST | /api/v1/receivables | Bearer JWT | Cadastra um recebível. |
| GET | /api/v1/receivables/{id} | Bearer JWT | Consulta um recebível por UUID. |
| POST | /api/v1/exchange-rates | Bearer JWT | Registra uma cotação local e sua vigência. |
| GET | /api/v1/exchange-rates | Bearer JWT | Lista o histórico, com paginação e filtros de par de moedas. |
| GET | /actuator/health e /actuator/health/readiness | Público | Verifica saúde e prontidão agregadas. |
| GET | /actuator/metrics e /actuator/prometheus | Basic Auth | Métricas da aplicação, usando ACTUATOR_METRICS_USERNAME e ACTUATOR_METRICS_PASSWORD. |
| GET | /v3/api-docs e /swagger-ui/index.html | Público | Especificação OpenAPI e interface Swagger. |

Exemplo de simulação:

~~~json
{
  "faceValueBrl": 100000.00,
  "termMonths": 3,
  "baseMonthlyRate": 0.01,
  "receivableType": "DUPLICATA_MERCANTIL",
  "paymentCurrency": "USD",
  "brlPerUsd": 5.4321
}
~~~

O cálculo usa BigDecimal. Em pagamento USD, o valor presente é calculado e arredondado em BRL antes de ser convertido. Para os três casos de aferição, consulte SPEC.md.

## Endereços locais padrão

As portas podem ser alteradas em .env.

| Serviço | Endereço |
|---|---|
| API | http://localhost:18080 |
| Swagger UI / OpenAPI | http://localhost:18080/swagger-ui/index.html / http://localhost:18080/v3/api-docs |
| Health | http://localhost:18080/actuator/health |
| Prometheus | http://localhost:19090 |
| Grafana | http://localhost:13000 |
| Keycloak | http://localhost:18180 |
| Graylog | http://localhost:19000 |
| SQL Server | localhost:11433 |
| Redis | localhost:16379 |
| GELF UDP externo | localhost:12202 |

MongoDB, Data Node e PostgreSQL do Keycloak são acessíveis somente dentro da rede Compose, salvo alteração explícita do arquivo.

## Métricas e logs

- A API expõe métricas em /actuator/prometheus; esse endpoint requer Basic Auth.
- Prometheus coleta API, Keycloak e Redis Exporter com a configuração de infra/prometheus/prometheus.yml.
- Grafana provisiona automaticamente a fonte Prometheus e o dashboard SRM Credit Engine.
- A aplicação envia logs GELF UDP ao Graylog. Consulte os logs no Graylog por application:srm-credit-engine; a busca depende de a entrada GELF estar ativa e do app estar iniciado com o perfil graylog.
- O Redis está disponível e monitorado; a aplicação não usa cache ativo.

Verificações úteis:

~~~bash
curl http://localhost:18080/actuator/health
curl http://localhost:19090/api/v1/targets
docker compose --env-file .env ps
~~~

No endpoint de targets do Prometheus, confirme que os jobs srm-credit-engine, keycloak e redis estão UP. No Grafana, confirme a fonte Prometheus e abra o dashboard provisionado.

## Testes

Execute o ciclo completo:

~~~bash
mvn verify
~~~

O ciclo compila, executa JUnit, gera relatório JaCoCo, aplica Checkstyle e verifica a cobertura configurada. O teste de persistência usa Testcontainers e SQL Server 2022; o processo Maven precisa conseguir acessar o Docker Engine. PersistenceIntegrationTest está marcado para ser ignorado quando o Testcontainers não detecta Docker, portanto confira o resumo Maven: um BUILD SUCCESS com teste ignorado não significa que a integração de persistência foi executada.

Os testes de domínio e API cobrem os três Golden Cases, validações, autenticação, criação/consulta de recebíveis, histórico de cotações e paginação. A suíte não testa uma integração cambial externa, idempotência de liquidação, liquidações simultâneas ou um Circuit Breaker ativo.

## Escopo e decisões pendentes

- A simulação recebe taxa base, prazo e cotação cambial na própria requisição. O serviço não escolhe uma taxa vigente nem consulta provedor cambial.
- A API registra recebíveis e cotações locais. Liquidação, extrato de liquidações, auditoria de liquidação e idempotência ainda não estão implementados; regras pendentes estão em SPEC.md.
- Resilience4j está no build, mas não há chamada a provedor externo decorada com Circuit Breaker, timeout ou retry. Não há Circuit Breaker operacional para simular nesta versão.
- MySQL e RabbitMQ não são dependências desta API e não fazem parte do Compose.

## Referências do projeto

- SPEC.md: premissas financeiras e perguntas ao negócio.
- DECISIONS.md: decisões e simplificações técnicas.
- REVIEW.md: revisão reversa do endpoint de liquidação do desafio.
- AI_USAGE.md: registro de uso de IA no desenvolvimento.
- compose.yaml: ambiente local isolado.
