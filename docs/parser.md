# Analisador sintático

[Voltar ao README](../README.md)

- [Funcionamento](#funcionamento)
- [Funções auxiliares](#funções-auxiliares)
- [Formato da saída](#formato-da-saída)
- [Erros sintáticos](#erros-sintáticos)
- [Exemplo de uso](#exemplo-de-uso)
- [Testes](#testes)
- [Diferenças em relação ao tutorial](#diferenças-em-relação-ao-tutorial)

## Funcionamento

O analisador sintático, ou parser, confere se a sequência de tokens segue a gramática da linguagem Jack e monta a árvore sintática do programa. A técnica é a descida recursiva. Cada regra da gramática tem uma função própria no parser, como `parseClass`, `parseLet` ou `parseExpression`, e uma função chama as outras do mesmo jeito que a gramática descreve.

O parser sempre olha um token à frente antes de decidir o que fazer. Na maior parte da gramática, esse token basta. Um `let` indica uma atribuição, e um `while` indica um laço.

Com a opção `--xml`, o compilador grava a árvore em XML, no formato dos arquivos de referência do Project 10 do Nand2Tetris. O arquivo sai ao lado do `.jack`, com o sufixo P, e o `Square.jack` gera o `SquareP.xml`. Na terceira etapa, o mesmo parser passou a gerar o código VM enquanto percorre a árvore, como descreve o [documento do gerador de código](codegen.md).

O código fica no pacote `parser`, em `src/main/java/parser/`. O `Parser.java` contém o analisador, e o `SyntaxException.java` define a exceção dos erros sintáticos.

## Funções auxiliares

A descrição da segunda etapa, fornecida pela disciplina, chama as funções auxiliares de `advance`, `peek`, `match` e `consume`. O parser usa os nomes do tutorial, e a tabela mostra a correspondência.

| Descrição da etapa | Parser | O que faz |
| --- | --- | --- |
| `advance()` | `nextToken()` | Passa para o próximo token |
| `peek()` | `peekTokenIs(tipo)` | Olha o próximo token sem avançar |
| `match(tipo)` | `peekTokenIs(tipo)` seguido de `expectPeek(tipo)` | Consome o token só quando ele é do tipo esperado |
| `consume(tipo)` | `expectPeek(tipo)` | Confere o tipo do próximo token, avança e grava o token no XML, ou acusa erro se o tipo não bater |

## Formato da saída

Cada regra da gramática abre e fecha uma tag, cada token ocupa uma linha, e cada nível da árvore recua 2 espaços. Os símbolos `&`, `<`, `>` e `"` saem como `&amp;`, `&lt;`, `&gt;` e `&quot;`, como o XML exige.

Algumas regras da gramática não aparecem como tag nos arquivos de referência, e o parser também não gera tag para elas. É o caso de `statement`. Nos arquivos de referência, cada comando aparece direto com a tag dele, como `<letStatement>` ou `<whileStatement>`, sem uma tag `<statement>` em volta. O mesmo vale para `type`, `op`, `unaryOp`, `keywordConstant` e para as chamadas de subrotina.

Jack não tem precedência entre operadores. Em `a + b * c`, o parser lê os termos na ordem em que aparecem, e só os parênteses mudam o agrupamento, como em `a + (b * c)`.

## Erros sintáticos

Quando encontra um erro, o parser para e mostra o arquivo, a linha, a coluna, o que esperava e o que encontrou. O programa termina com código 1.

```
Main.jack: syntax error at line 3, column 5: expected SEMICOLON, found 'let'
```

## Exemplo de uso

Os comandos abaixo copiam o Square do Project 10 para `output/` e geram as árvores das três classes.

```bash
mkdir -p output/Square
cp src/test/resources/input/project10/Square/*.jack output/Square/
java -jar target/jackcompiler.jar --xml output/Square
```

O programa grava `MainP.xml`, `SquareP.xml` e `SquareGameP.xml` em `output/Square/`. O `SquareP.xml` começa assim.

```xml
<class>
  <keyword> class </keyword>
  <identifier> Square </identifier>
  <symbol> { </symbol>
  <classVarDec>
    <keyword> field </keyword>
    ...
```

## Testes

Os testes ficam em `src/test/java/parser/`. Cada teste unitário chama uma função do parser com um trecho pequeno de código Jack e compara o XML gerado com o esperado. O `ExpressionParserTest` tem 15 testes sobre expressões e chamadas de subrotina, o `StatementParserTest` tem 14 sobre os cinco comandos e o `StructureParserTest` tem 12 sobre declarações, subrotinas e a classe completa.

O `ParserFilesTest` roda o parser nas sete classes dos três programas do Project 10, o ExpressionLessSquare, o Square e o ArrayTest, e compara cada árvore com a referência em `src/test/resources/expected/parser/`, ignorando os espaços do início das linhas. O `validate.sh` faz a comparação completa, com a indentação.

## Diferenças em relação ao tutorial

O tutorial serviu de base para o parser, mas cinco pontos precisaram de ajuste durante a implementação.

O construtor do tutorial lê dois tokens antes de começar a análise, e o parser acabava pulando o primeiro token do arquivo. Na entrada `10;`, por exemplo, ele começava pelo `;` e acusava erro sem nunca ver o `10`. O primeiro teste unitário mostrou o problema, e o construtor passou a ler um token só.

Para saber se um token é um operador, o tutorial compara só o texto dele. Nesse caso, uma string com o texto `"+"` passaria por operador de soma. O parser consulta o tipo do token, que separa os dois casos.

Numa expressão, um nome pode ser uma variável, como `x`, um acesso a array, como `a[1]`, ou uma chamada, como `Square.new()`. Só o símbolo depois do nome mostra qual é o caso. O parser lê o nome primeiro e decide pelo símbolo seguinte.

Os exemplos de XML do tutorial saem sem indentação, e os arquivos de referência usam 2 espaços por nível. O parser guarda o nível da árvore em que está e recua cada linha de acordo, para que a saída fique idêntica à de referência.

A ordem de implementação do tutorial esbarrou em dependências. O `if` e o `while` contêm blocos de comandos, então a função que lê uma sequência de comandos precisou vir antes deles. Pelo mesmo motivo, os parâmetros e as variáveis locais vieram antes das subrotinas.