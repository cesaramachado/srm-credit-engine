# Revisão reversa do endpoint de liquidação

## Escopo

Esta revisão avalia o handler TypeScript do Anexo A do desafio. Os achados abaixo dizem respeito ao trecho apresentado; quando uma proteção poderia estar em middleware ou configuração fora dele, isso está indicado. Recomendações que dependem de regra financeira ainda pendente são tratadas como decisões necessárias, não como comportamento presumido.

## P0 — bloqueadores de produção

### 1. O cálculo usa escala percentual incorreta e ponto flutuante

`BASE_RATE = 1.0` e os spreads `1.5`/`2.5` são usados diretamente na fórmula. Para os Golden Cases, esses valores representam percentuais: 1% deve entrar como `0.01`, spread de 1,5% como `0.015` e spread de 2,5% como `0.025`. Com os valores literais atuais, o denominador é muito maior que o esperado e o valor pago fica incorreto. `face_value`, a divisão e `Math.pow` também usam `Number`, que não representa decimais monetários de forma exata.

**Impacto:** liquidação financeira por valor incorreto, com risco de prejuízo e divergência entre o valor calculado e os Golden Cases.

**Alterar:** modelar taxas como frações decimais e executar cálculo/intermediários com decimal exato, por exemplo `BigDecimal` no backend Java ou biblioteca decimal apropriada em TypeScript. Validar C1, C2 e C3 ao centavo, além de casos de borda.

### 2. As consultas concatenam dados da requisição no SQL

`receivableId` é interpolado no `SELECT`, no `INSERT` e no `UPDATE`; `currency` também é interpolada no `INSERT`. Uma entrada maliciosa pode alterar a consulta executada.

**Impacto:** SQL injection, exposição ou alteração de dados e possível comprometimento do banco.

**Alterar:** usar parâmetros vinculados em todas as consultas. Validar o formato do identificador e aceitar moeda/tipo somente por enumerações fechadas. Não montar SQL concatenando dados, mesmo quando a origem parece confiável.

### 3. As gravações não formam uma transação e as falhas são ocultadas

O `INSERT` e o `UPDATE` são operações separadas, sem transação explícita. O `catch` vazio engole a exceção; o handler sempre responde `200` com `ok: true`. Se o `INSERT` funcionar e o `UPDATE` falhar, a base fica em estado parcial. Se qualquer gravação falhar, o cliente recebe confirmação falsa.

**Impacto:** registros de liquidação sem atualização correspondente do recebível, ou operações reportadas como concluídas sem terem sido gravadas.

**Alterar:** executar as gravações numa única transação ACID e deixar a falha causar rollback. Propagar a exceção para o tratamento global, registrar contexto seguro e devolver erro semântico. Não confirmar sucesso antes do commit.

### 4. Não há idempotência nem proteção suficiente contra concorrência

Duas requisições concorrentes podem ler o recebível antes de qualquer uma atualizar o status. Ambas podem inserir liquidações e depois marcar o mesmo recebível como liquidado. O trecho não tem chave idempotente, controle de versão, transição condicional de status ou restrição no banco.

**Impacto:** pagamento duplicado, especialmente em retry de rede, duplo clique ou concorrência entre operadores.

**Alterar:** implementar idempotência persistente por chave, com restrição única e reprodução da resposta original quando a mesma chave e o mesmo conteúdo forem reenviados. A mesma chave com conteúdo diferente deve resultar em conflito. Proteger a transição de estado com `@Version`/optimistic locking ou atualização condicional dentro da transação; confirmar que exatamente uma requisição alterou o estado. Criar também restrições de banco compatíveis com a regra de negócio.

O desafio exige impedir a duplicação da mesma requisição, mas a política de liquidações parciais/múltiplas ainda precisa ser definida. Portanto, não se deve impor unicidade por recebível sem confirmar essa regra. Se parciais forem permitidas, a implementação deverá controlar saldo remanescente e impedir liquidações acima do saldo.

### 5. O handler não demonstra autenticação nem autorização

O trecho registra diretamente `app.post("/settlements", ...)`, sem demonstrar middleware de autenticação ou autorização. A proteção pode existir na configuração global, mas precisa ser verificada no ponto de entrada efetivo.

**Impacto:** se a rota estiver exposta sem proteção, usuários não autenticados ou sem permissão podem liquidar recebíveis.

**Alterar:** exigir JWT válido e aplicar autorização para a operação conforme a política de perfis aprovada pelo negócio. Verificar também se o usuário pode operar o recebível solicitado; não confiar somente no UUID recebido. Testar `401`, `403` e tentativas de acesso a recebíveis fora do escopo do usuário.

## P1 — alta prioridade

### 6. Entradas, recebível e estado não são validados

O handler não valida formato/presença de `receivableId`, moeda suportada, tipo do recebível ou campos do cálculo. Se `queryOne` não encontrar registro, o acesso a `receivable.type` falha. O ternário trata todo tipo diferente de `DUPLICATA` como cheque, inclusive valores inválidos. Qualquer moeda diferente de `USD` é implicitamente tratada como BRL, mas gravada como veio na requisição. Também não há verificação de que o recebível está em estado elegível.

**Impacto:** erros não controlados, cálculo com regra indevida e registro com moeda incompatível com o valor.

**Alterar:** validar o DTO na borda da API; rejeitar tipos/moedas desconhecidos; tratar ausência com `404`; validar valores e estado do recebível na camada de domínio. Aplicar limites somente depois de definidos pelo negócio.

### 7. A cotação cambial não identifica claramente o par nem a vigência e não é preservada

`getLatestRate("USD")` não informa o par completo nem o instante de referência. “Mais recente” pode não significar vigente para a liquidação. A divisão por `rate` só é correta para uma convenção específica de cotação; o código não a declara. O valor efetivamente usado não é gravado na liquidação.

**Impacto:** conversão na direção errada ou com taxa inválida para a data, além da impossibilidade de reproduzir uma liquidação depois que a cotação mudar.

**Alterar:** explicitar moeda de origem, moeda de destino, direção e instante de vigência; selecionar a cotação segundo a política aprovada; validar taxa positiva e disponível; guardar o valor e a identificação/instante da cotação junto ao resultado. A ausência de cotação deve seguir a política definida pelo negócio, sem usar uma taxa antiga silenciosamente.

### 8. A chamada ao provedor cambial não tem política de timeout ou resiliência

`fxService.getLatestRate` é uma dependência externa chamada antes do bloco `try`, sem timeout, retry limitado ou Circuit Breaker demonstrado. Sua falha pode resultar em resposta inconsistente e não há política explícita para indisponibilidade.

**Impacto:** requisições podem ficar presas, aumentar a carga sobre a aplicação e o provedor ou falhar sem resposta controlada.

**Alterar:** definir timeout e comportamento de erro; implementar Circuit Breaker e, se necessário, retry limitado com backoff. Não usar fallback/cotação em cache sem uma política de validade aprovada. Evitar manter transação ou lock de banco aberto durante a chamada de rede.

### 9. O registro não preserva dados suficientes para auditoria

O `INSERT` grava apenas `receivable_id`, `amount` e `currency`. Não preserva os parâmetros que formaram o resultado, como taxa base, spread, prazo, cotação aplicada e valor intermediário em BRL. O trecho também não mostra timestamps ou imutabilidade do registro.

**Impacto:** não é possível reconstruir nem explicar com segurança o valor liquidado; mudanças posteriores de taxa/cotação apagam o contexto usado.

**Alterar:** persistir um snapshot suficiente para reproduzir o cálculo, incluindo parâmetros financeiros efetivamente usados e timestamps. Tratar liquidação como registro imutável na API e proteger contra atualização/exclusão indevida.

### 10. O arredondamento não segue a precisão financeira definida

`toFixed(2)` formata um `Number` binário e não garante o arredondamento decimal `HALF_EVEN`. No caso BRL/USD, o código converte `presentValue` antes de arredondar o valor presente em BRL, embora o Golden Case C3 exija arredondá-lo antes da conversão.

**Impacto:** diferenças de centavos e divergência dos resultados esperados.

**Alterar:** manter precisão decimal até o ponto de arredondamento definido; aplicar `HALF_EVEN` no valor presente em BRL antes da conversão USD e arredondar o resultado final conforme a especificação. Não converter valores monetários para `Number` em nenhuma etapa.

## P2 — clareza, operação e eficiência

### 11. Contrato HTTP e representação do valor não estão definidos corretamente

O handler devolve `200 OK` para criação. `toFixed(2)` retorna uma string, então `amount` no JSON é texto, não número. O formato pode ser aceitável para evitar perda decimal no cliente, mas precisa ser deliberado e documentado.

**Impacto:** clientes podem interpretar o resultado de modo diferente e não distinguir criação, repetição idempotente, conflito e falha.

**Alterar:** definir e documentar o contrato de resposta decimal; usar `201 Created` para nova liquidação e códigos adequados para validação, ausência, autorização, conflito e indisponibilidade. Uma repetição idempotente deve recuperar o resultado original, não gerar nova liquidação.

### 12. `SELECT *` carrega dados desnecessários e acopla o handler ao esquema

O cálculo utiliza poucos campos, mas a consulta carrega todas as colunas. Isso amplia leitura e torna o handler sensível a alterações no esquema.

**Impacto:** transferência e mapeamento desnecessários, maior acoplamento e risco de expor campos internos caso a entidade seja retornada no futuro.

**Alterar:** selecionar somente os campos necessários, projetar os dados para um DTO interno e manter a resposta da API separada da entidade persistida.

### 13. Erros fora do bloco `try` não têm tratamento visível

A consulta do recebível e a chamada cambial ocorrem antes do `try`. Assim, falhas de banco, recebível inexistente e falha do provedor não passam pelo tratamento local mostrado. O tratamento global não aparece no trecho.

**Impacto:** rejeições assíncronas podem produzir respostas inconsistentes e logs insuficientes, dependendo da versão do framework e do middleware instalado.

**Alterar:** usar middleware de erro assíncrono compatível com a versão do Express ou encaminhar explicitamente as exceções ao handler global. Mapear falhas conhecidas para respostas seguras e registrar correlação/diagnóstico sem credenciais ou dados sensíveis.

## Pontos que o trecho não permite concluir

- **N+1:** não há evidência de N+1 neste handler isolado, pois ele faz uma consulta de recebível e não percorre uma coleção disparando consultas adicionais. O extrato/relatório não aparece no trecho e deve ser revisado separadamente.
- **Autenticação global, logs e transações configuradas fora da rota:** o trecho não mostra esses componentes; a ausência no trecho é um ponto para confirmar na aplicação completa.
- **Liquidação parcial, política cambial, fallback e limites de negócio:** dependem de decisão explícita. Não devem ser inventados durante a correção.

## Sequência recomendada de correção

1. Confirmar as regras ainda pendentes: política cambial e de indisponibilidade, permissões, elegibilidade e possibilidade de liquidação parcial.
2. Corrigir o cálculo e validar os Golden Cases com aritmética decimal exata.
3. Introduzir DTO validado, autorização e consultas parametrizadas.
4. Implementar transação, idempotência e concorrência com proteção também no banco.
5. Persistir snapshot imutável e mapear respostas/erros semânticos.
6. Implementar e testar timeout, Circuit Breaker e comportamento de indisponibilidade do provedor.
7. Adicionar testes de concorrência, repetição idempotente, rollback, auditoria, falhas de integração e contrato HTTP.
