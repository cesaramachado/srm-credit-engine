# Revisão reversa do endpoint de liquidação

## P0 — cálculo financeiro interpreta taxas incorretamente

`BASE_RATE` vale `1.0` e os spreads `1.5`/`2.5`, mas esses valores representam percentuais mensais. A fórmula os soma como números inteiros (`1 + 1 + 1.5`), em vez de usar frações decimais (`1 + 0.01 + 0.015`). Isso produz um valor presente incorreto e pode causar pagamento muito inferior ao devido.

**Correção:** representar todos os cálculos com decimal exato, por exemplo `BigDecimal`, e valores percentuais como frações. Validar os três Golden Cases ao centavo antes de liberar a liquidação.

## P0 — consultas vulneráveis a SQL injection

`receivableId` e `currency` são interpolados diretamente nas consultas SQL. Uma requisição construída para esse fim pode alterar o comando executado no banco, ler dados ou modificar registros.

**Correção:** usar parâmetros vinculados em todas as consultas e validar identificadores e códigos de moeda com tipos e restrições explícitos.

## P0 — gravações não são atômicas e erros são ocultados

O `INSERT` e o `UPDATE` são executados em chamadas separadas, sem transação explícita. Se a primeira gravação funcionar e a segunda falhar, a liquidação pode ficar parcial. O `catch` vazio ignora a falha e a rota ainda responde `200 OK` com `ok: true`.

**Correção:** executar as gravações em uma única transação ACID, propagar falhas ao tratamento global e responder com status coerente. Qualquer falha deve desfazer a liquidação inteira.

## P0 — não há idempotência nem proteção contra concorrência

Retries, cliques repetidos ou duas requisições simultâneas podem criar mais de uma liquidação para o mesmo recebível. A consulta de estado, o `INSERT` e a atualização não estão protegidos por bloqueio, versão ou restrição única.

**Correção:** exigir `Idempotency-Key`, persistir a chave com restrição única, proteger o recebível com controle otimista de versão e garantir no banco que não haja liquidações incompatíveis para o mesmo recebível. Testar duas requisições simultâneas.

## P1 — câmbio não é selecionado pela vigência nem preservado

`getLatestRate("USD")` escolhe a cotação mais recente sem demonstrar que ela é válida para o instante da liquidação. O valor aplicado também não é salvo no registro de liquidação. Uma atualização posterior pode impedir a reprodução do cálculo.

**Correção:** resolver a cotação conforme a política de vigência aprovada pelo negócio, falhar quando não houver uma cotação aplicável e persistir a cotação efetivamente usada junto ao resultado e aos demais dados do cálculo.

## P1 — aritmética usa ponto flutuante binário

`face_value` e `Math.pow` operam como números JavaScript, que não representam todos os decimais monetários exatamente. `toFixed(2)` não corrige erros acumulados nem garante a política `HALF_EVEN` requerida.

**Correção:** manter valores, taxas e operações intermediárias em decimal exato; aplicar `HALF_EVEN` nos pontos definidos pela especificação, incluindo o arredondamento do VP em BRL antes da conversão do Golden Case C3.

## P1 — liquidação não registra um snapshot de auditoria

O `INSERT` grava apenas recebível, valor e moeda. Não preserva taxa base, spread, prazo, cotação utilizada ou outros valores necessários para reconstruir a operação. O trecho não oferece uma trilha imutável suficiente para auditoria.

**Correção:** persistir os dados de cálculo e os timestamps no registro imutável da liquidação; não oferecer atualização ou exclusão desse registro pela aplicação.

## P1 — entradas e estado do recebível não são validados

Não há validação do corpo, da moeda, do identificador ou dos campos necessários ao cálculo. Se o recebível não existir, o acesso a `receivable.type` falha sem tratamento de domínio. Também não há verificação explícita de que o recebível ainda pode ser liquidado.

**Correção:** validar a entrada na borda da API e validar regras/estado na camada de negócio, com exceções próprias para recebível inexistente, estado incompatível e cotação indisponível.

## P2 — status HTTP não representa o resultado

A rota responde `200 OK` tanto após sucesso quanto após falhas ocultadas. Isso dificulta que clientes diferenciem criação, conflito, validação e falha técnica.

**Correção:** usar códigos semânticos documentados, como `201` para nova liquidação, `400` para entrada inválida, `404` para recebível inexistente, `409` para duplicidade/conflito e `5xx` para falha técnica.

## P2 — consulta usa `SELECT *` e não limita o acesso a dados

`SELECT *` carrega colunas que o cálculo pode não usar e acopla o endpoint a mudanças do esquema. O trecho não demonstra restrição de acesso aos dados do cedente ou das liquidações.

**Correção:** selecionar apenas os campos necessários e aplicar a política de autenticação/autorização definida para os usuários da API. Evitar expor colunas internas na resposta.
