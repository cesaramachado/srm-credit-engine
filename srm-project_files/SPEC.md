# Especificação

## Premissas adotadas

- O prazo é expresso em meses inteiros, e os juros são compostos.
- A taxa base nos Golden Cases é de 1% ao mês.
- O spread é de 1,5% para duplicatas e 2,5% para cheques.
- O cálculo do valor presente (VP) a partir do valor futuro (VF) é:

  ```text
  VP = VF / (1 + taxa base + spread) ^ prazo
  ```

- Taxas e cotações mantêm histórico e datas de vigência.
- Em operações BRL/USD, o cálculo é realizado primeiro em BRL e, em seguida, o valor é convertido.
- A cotação utilizada deve ser registrada em cada liquidação.
- Liquidações registradas são imutáveis.
- Liquidações duplicadas devem ser impedidas.

## Perguntas ao negócio

- Qual taxa base e origem devem ser usadas nas operações fora dos Golden Cases?
- A taxa base será fixa ou poderá ser atualizada?
- A atualização das taxas e cotações será manual ou automática?
- Qual cotação cambial deverá ser utilizada na liquidação?
- O que fazer quando não houver cotação disponível?
- Como interpretar os limites inicial e final da vigência de taxas e cotações: inclusivos ou exclusivos?
- Como calcular o prazo em meses entre a data da operação e a data de vencimento?
- Serão permitidos prazos fracionados?
- Serão permitidas liquidações parciais? Se sim, como deverá ser calculado o saldo remanescente?
- Quais operações cada perfil (OPERADOR, AUDITOR e ADMINISTRADOR) poderá executar?
- Quais limites máximos devem ser aplicados aos valores, prazos, taxas e cotações?

## Precisão numérica

- Na aplicação, utilizar `BigDecimal`.
- No banco de dados, utilizar `NUMERIC`.
- Representar valores monetários com duas casas decimais.
- Manter precisão adicional para taxas e cotações.
- Utilizar arredondamento `HALF_EVEN` somente no resultado final.
- Em operações BRL/USD, arredondar o valor em BRL antes da conversão.
- Não utilizar `float` ou `double` para valores monetários.

## Critérios de aceite

- Calcular corretamente duplicatas e cheques.
- Manter histórico e vigência de taxas e cotações.
- Realizar conversões entre BRL e USD.
- Reproduzir os três Golden Cases em testes automatizados.
- Impedir liquidações duplicadas, inclusive em operações simultâneas.
- Garantir transações ACID e controle de concorrência.
- Preservar liquidações e registros de auditoria.
- Validar entradas e tratar erros adequadamente.
- Disponibilizar uma API REST documentada.
- Oferecer extrato com filtros e paginação.
- Utilizar consultas eficientes.
- Implementar logs estruturados e métricas.
- Implementar timeout e mecanismo de resiliência na integração cambial.
- Executar testes e análise de código em integração contínua.
