# OpenShift

Os manifests desta pasta são um ponto de partida e não foram aplicados a um cluster.

Antes de usar:

1. Crie o namespace `srm-credit-engine` e disponibilize SQL Server, Redis, Keycloak e Graylog nas URLs/serviços configurados.
2. Ajuste os hosts de banco, Keycloak e Graylog em `configmap.yaml`.
3. Crie o Secret `srm-credit-engine-secrets` com `DB_USERNAME`, `DB_PASSWORD`, `SPRING_DATA_REDIS_PASSWORD` e, se a coleta Prometheus for habilitada, `ACTUATOR_METRICS_PASSWORD`.
4. Publique a imagem da aplicação e ajuste a referência do Deployment.
5. Revise a URL pública da Route, política de roles, recursos e acesso aos endpoints Actuator para o cluster.

O profile `local` ativa SQL Server e Flyway. Não contém um cluster de dependências nem um procedimento de deploy.
