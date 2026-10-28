# jackcompiler

Compilador para a linguagem Jack, baseado no projeto [Nand2Tetris](https://www.nand2tetris.org/) e desenvolvido de acordo com o material da disciplina. Cada etapa do compilador ganha uma seção própria neste repositório. Hoje ele contém o analisador léxico e o analisador sintático, integrados.

## Autores

- ANTONIO LUCAS DA SILVA VALE | 20250013480
- HIGOR PINHEIRO COSTA | 20250013695

## Linguagem

O projeto usa Java 17 ou superior e o Maven para compilar e rodar os testes, escritos com JUnit 5. Não é preciso instalar o Maven. O repositório traz o Maven Wrapper, o script `mvnw`, que baixa o Maven sozinho na primeira execução.

## Estrutura

| Pasta | Conteúdo |
| --- | --- |
| `src/main/java/` | O `Main.java` e os pacotes `lexer` e `parser` |
| `src/test/java/` | Os testes JUnit, com um pacote por módulo |
| `src/test/resources/input/` | Os programas Jack de entrada |
| `src/test/resources/expected/lexer/` | Os tokens oficiais `MainT.xml`, `SquareT.xml` e `SquareGameT.xml` |
| `src/test/resources/expected/parser/` | As árvores oficiais `Main.xml`, `Square.xml` e `SquareGame.xml` |
| `scripts/` | O `validate.sh`, que compara as saídas com os arquivos oficiais |
| `output/` | As saídas geradas pelo programa, fora do Git |

Os arquivos de `input/` e `expected/` vêm do pacote do Projeto 10 do Nand2Tetris.

## Como compilar e executar

O comando abaixo compila o projeto e grava as classes em `target/classes/`.

```bash
./mvnw compile
```

O programa recebe um arquivo Jack e aceita o caminho de saída como segundo argumento.

```bash
java -cp target/classes Main [--tokens] <arquivo.jack> [saida.xml]
```

Sem a opção `--tokens`, o programa gera a árvore sintática. Com ela, gera a lista de tokens do analisador léxico.

### Arquivo de saída

Quando o segundo argumento falta, o programa grava o XML em `output/` com o mesmo nome do arquivo oficial correspondente.

| Comando | Arquivo gerado |
| --- | --- |
| `java -cp target/classes Main --tokens src/test/resources/input/Square.jack` | `output/lexer/SquareT.xml` |
| `java -cp target/classes Main src/test/resources/input/Square.jack` | `output/parser/Square.xml` |

Os tokens vão para `output/lexer/<Nome>T.xml` e a árvore vai para `output/parser/<Nome>.xml`, em que `<Nome>` é o nome do arquivo Jack sem a extensão. O programa cria as pastas que faltarem e informa o caminho gravado. Com o segundo argumento, o XML vai para o caminho indicado, como em `java -cp target/classes Main entrada.jack saida.xml`.

### Testes e validação

O Maven roda os testes automatizados com o comando abaixo.

```bash
./mvnw test
```

O script abaixo roda o programa nos três programas oficiais e confere se as saídas batem com os arquivos de `expected/`.

```bash
./scripts/validate.sh
```

O script compila o projeto, grava os seis arquivos em `output/` e compara cada um com o oficial usando `diff`. A opção `--strip-trailing-cr` faz o `diff` ignorar a diferença entre as quebras de linha do Windows e do Linux.

## Analisador léxico

O scanner lê um arquivo `.jack` e produz os tokens em XML, no formato dos arquivos de referência do Nand2Tetris. Ele reconhece palavras reservadas, símbolos, constantes inteiras, constantes de string e identificadores, e pula espaços e comentários. Diante de um caractere inválido, de uma string sem aspas finais ou de um comentário de bloco sem fechamento, o scanner lança uma `LexicalException` com a linha e a coluna do problema. O programa então termina com uma mensagem como `lexical error at line 2, column 5: unexpected character '@'`.

O código fica no pacote `lexer`, em `src/main/java/lexer/`. O `Scanner.java` reconhece os tokens. O `Token.java` guarda o tipo, o lexema, a linha e a coluna de cada token e gera o seu XML. O `TokenType.java` lista os tipos de token, e o `Lexicon.java` guarda as tabelas de palavras reservadas e de símbolos. O `LexicalException.java` define a exceção dos erros léxicos, e o `TokenXml.java` monta o XML com todos os tokens de um arquivo.

O comando abaixo gera os tokens do `Square.jack`.

```bash
java -cp target/classes Main --tokens src/test/resources/input/Square.jack
```

O arquivo `output/lexer/SquareT.xml` começa assim.

```xml
<tokens>
<keyword> class </keyword>
<identifier> Square </identifier>
<symbol> { </symbol>
...
</tokens>
```

### Testes do analisador léxico

Os testes ficam em `src/test/java/lexer/`. O `ScannerTest` tem 12 testes unitários. Eles cobrem palavras reservadas e identificadores, constantes, os 19 símbolos, comentários, contagem de linhas e colunas, escape no XML, o `TokenXml` e os três erros léxicos. O `ScannerFilesTest` compara os tokens de `Main.jack`, `Square.jack` e `SquareGame.jack` com os arquivos de `expected/lexer/`.

## Analisador sintático

O parser lê um arquivo `.jack` e monta a árvore sintática em XML, no mesmo formato dos arquivos oficiais do Nand2Tetris. Ele pede os tokens ao scanner um de cada vez, então os dois módulos trabalham juntos desde a leitura do arquivo.

A técnica é a descida recursiva. Cada regra da gramática de Jack tem uma função própria no parser, como `parseClass`, `parseLet` ou `parseExpression`, e uma função chama as outras do mesmo jeito que a gramática descreve.

O código fica no pacote `parser`, em `src/main/java/parser/`. O `Parser.java` contém o analisador, e o `SyntaxException.java` define a exceção dos erros sintáticos.

### Funções auxiliares

A atividade pede as funções `advance`, `peek`, `match` e `consume` para lidar com os tokens. O parser usa os nomes do tutorial da disciplina, e a tabela mostra qual função corresponde a cada uma.

| Atividade | Parser | O que faz |
| --- | --- | --- |
| `advance()` | `nextToken()` | Passa para o próximo token |
| `peek()` | `peekTokenIs(tipo)` | Olha o próximo token sem avançar |
| `match(tipo)` e `consume(tipo)` | `expectPeek(tipo)` | Confere se o próximo token é do tipo esperado, avança e grava o token no XML. Se o tipo não bater, acusa erro |

O parser sempre olha um token à frente antes de decidir o que fazer. Na maior parte da gramática, esse token basta. Um `let` indica uma atribuição, e um `while` indica um laço.

### Formato da saída

A saída segue o formato dos arquivos oficiais. Cada regra da gramática abre e fecha uma tag, cada token ocupa uma linha, e cada nível da árvore recua 2 espaços. Os símbolos `&`, `<`, `>` e `"` saem como `&amp;`, `&lt;`, `&gt;` e `&quot;`, como o XML exige.

Algumas regras da gramática não aparecem como tag nos arquivos oficiais, e o parser também não gera tag para elas. É o caso de `statement`. Nos arquivos oficiais, cada comando aparece direto com a tag dele, como `<letStatement>` ou `<whileStatement>`, sem uma tag `<statement>` em volta. O parser segue os arquivos oficiais, embora a atividade cite `<statement>` como tag. O mesmo vale para `type`, `op`, `unaryOp`, `keywordConstant` e para as chamadas de subroutine.

Jack não tem precedência entre operadores. Em `a + b * c`, o parser lê os termos na ordem em que aparecem, e só os parênteses mudam o agrupamento, como em `a + (b * c)`.

### Erros sintáticos

Quando encontra um erro, o parser para e mostra a linha, a coluna, o que esperava e o que encontrou. O programa termina com código 1.

```
syntax error at line 3, column 5: expected SEMICOLON, found 'let'
```

### Exemplo de uso

O comando abaixo gera a árvore do `Square.jack`.

```bash
java -cp target/classes Main src/test/resources/input/Square.jack
```

O programa responde `wrote output/parser/Square.xml`, e o arquivo começa assim.

```xml
<class>
  <keyword> class </keyword>
  <identifier> Square </identifier>
  <symbol> { </symbol>
  <classVarDec>
    <keyword> field </keyword>
    ...
```

### Testes do analisador sintático

Os testes ficam em `src/test/java/parser/`. Cada teste unitário chama uma função do parser com um trecho pequeno de código Jack e compara o XML gerado com o esperado. O `ExpressionParserTest` tem 15 testes sobre expressões e chamadas de subroutine. O `StatementParserTest` tem 14 testes sobre os cinco comandos. O `StructureParserTest` tem 12 testes sobre declarações, subroutines e a classe completa.

O `ParserFilesTest` roda o parser nos três programas oficiais e compara a árvore com os arquivos de `expected/parser/`, ignorando os espaços do início das linhas.

## Status da validação

Os 59 testes automatizados passam, 15 do analisador léxico e 44 do analisador sintático. O `validate.sh` confirma que as seis saídas são idênticas aos arquivos oficiais, inclusive na indentação.

```
OK   lexer/MainT.xml
OK   lexer/SquareT.xml
OK   lexer/SquareGameT.xml
OK   parser/Main.xml
OK   parser/Square.xml
OK   parser/SquareGame.xml
```

## Desafios da unidade

No analisador léxico, a comparação com os arquivos oficiais falhava mesmo com a saída certa. Os arquivos oficiais terminam as linhas com `\r\n`, no padrão do Windows, e o programa gerava `\n`. A opção `--strip-trailing-cr` do `diff` resolveu.

No analisador sintático, o tutorial da disciplina serviu de base, mas alguns trechos precisaram de ajuste.

O construtor do tutorial lê dois tokens antes de começar a análise, e com isso o parser pula o primeiro token do arquivo. Na entrada `10;`, por exemplo, o parser começava pelo `;` e acusava erro, sem nunca ver o `10`. O primeiro teste unitário mostrou o problema, e o construtor passou a ler um token só.

Para saber se um token é um operador, o tutorial olha só o texto dele. Assim, uma string com o texto `"+"` passaria por operador de soma. O parser olha o tipo do token, que diferencia os dois casos.

Numa expressão, um nome pode ser uma variável, como `x`, um acesso a array, como `a[1]`, ou uma chamada, como `Square.new()`. Só o símbolo depois do nome mostra qual é o caso. Por isso o parser lê o nome primeiro e decide pelo símbolo seguinte.

Os exemplos de XML do tutorial saem sem indentação, e os arquivos oficiais usam 2 espaços por nível. O parser passou a contar em que nível da árvore está e a indentar cada linha de acordo.

A ordem de implementação do tutorial esbarrou em dependências. O `if` e o `while` contêm blocos de comandos, então a parte que lê uma sequência de comandos precisou vir antes deles. Pelo mesmo motivo, os parâmetros e as variáveis locais vieram antes das subroutines.

Para os testes automatizados, o projeto passou a usar o Maven com o JUnit 5. Com isso, as pastas seguem o padrão do Maven, e os arquivos de teste ficaram separados em entrada, esperados e saída, do mesmo jeito para o léxico e para o parser.

## Licença

MIT, conforme o arquivo [LICENSE](LICENSE).