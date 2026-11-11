# Analisador léxico

[Voltar ao README](../README.md)

- [Funcionamento](#funcionamento)
- [Código](#código)
- [Exemplo de uso](#exemplo-de-uso)
- [Testes](#testes)
- [Desafios e diferenças em relação ao tutorial](#desafios-e-diferenças-em-relação-ao-tutorial)

## Funcionamento

O analisador léxico, ou scanner, lê um arquivo `.jack` e quebra o texto em tokens. Ele reconhece palavras reservadas, símbolos, constantes inteiras, constantes de string e identificadores, e pula espaços e comentários. O parser pede os tokens ao scanner um de cada vez, então os dois trabalham juntos desde a leitura do arquivo.

Com a opção `--tokens`, o compilador grava a lista de tokens em XML, no formato dos arquivos de referência do Project 10 do Nand2Tetris. O arquivo sai ao lado do `.jack`, com o sufixo T, e o `Square.jack` gera o `SquareT.xml`. Essa saída serviu para validar o scanner na primeira etapa e continua disponível.

Diante de um caractere inválido, de uma string sem aspas finais ou de um comentário de bloco sem fechamento, o scanner lança uma `LexicalException` com a linha e a coluna do problema. O programa termina com uma mensagem como `Main.jack: lexical error at line 2, column 5: unexpected character '@'`.

## Código

O código fica no pacote `lexer`, em `src/main/java/lexer/`.

| Arquivo | Papel |
| --- | --- |
| `Scanner.java` | Reconhece os tokens |
| `Token.java` | Guarda o tipo, o lexema, a linha e a coluna de cada token e gera o seu XML |
| `TokenType.java` | Lista os tipos de token |
| `Lexicon.java` | Guarda as tabelas de palavras reservadas e de símbolos |
| `LexicalException.java` | Define a exceção dos erros léxicos |
| `TokenXml.java` | Monta o XML com todos os tokens de um arquivo |

## Exemplo de uso

Os comandos abaixo copiam o Square do Project 10 para `output/` e geram os tokens das três classes.

```bash
mkdir -p output/Square
cp src/test/resources/input/project10/Square/*.jack output/Square/
java -jar target/jackcompiler.jar --tokens output/Square
```

O programa grava `MainT.xml`, `SquareT.xml` e `SquareGameT.xml` em `output/Square/`. O `SquareT.xml` começa assim.

```xml
<tokens>
<keyword> class </keyword>
<identifier> Square </identifier>
<symbol> { </symbol>
...
</tokens>
```

## Testes

Os testes ficam em `src/test/java/lexer/`. O `ScannerTest` tem 12 testes unitários. Eles cobrem palavras reservadas e identificadores, constantes, os 19 símbolos, comentários, contagem de linhas e colunas, escape no XML, o `TokenXml` e os três erros léxicos.

O `ScannerFilesTest` gera os tokens das sete classes dos três programas do Project 10, o ExpressionLessSquare, o Square e o ArrayTest, e compara cada saída com a referência em `src/test/resources/expected/lexer/`.

## Desafios e diferenças em relação ao tutorial

O principal desafio desta etapa apareceu na validação. A comparação com os arquivos de referência falhava mesmo com a saída certa, porque esses arquivos terminam as linhas com `\r\n`, no padrão do Windows, e o programa gerava `\n`. A opção `--strip-trailing-cr` do `diff` passou a ignorar essa diferença no `validate.sh`. Os testes JUnit comparam linha a linha, o que também descarta o `\r`.

Para rodar testes automatizados, a dupla adotou o Maven com o JUnit 5, que o tutorial não usa. Com isso, as pastas seguem o padrão do Maven, e os arquivos de teste ficam separados em entradas, saídas esperadas e saídas geradas, do mesmo jeito para o scanner, para o parser e para o gerador de código.