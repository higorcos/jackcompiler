# jackcompiler

Compilador para a linguagem Jack, baseado no projeto [Nand2Tetris](https://www.nand2tetris.org/) e desenvolvido de acordo com o material da disciplina. Cada etapa do compilador entra neste repositório com a sua própria seção. Até o momento, o repositório contém o analisador léxico.

## Autores

- ANTONIO LUCAS DA SILVA VALE, matrícula 20250013480
- HIGOR PINHEIRO COSTA, matrícula 20250013695

## Linguagem

O projeto é escrito em Java 17 ou superior e usa o Maven para compilar e rodar os testes automatizados, escritos com JUnit 5. O repositório traz o Maven Wrapper, o script `mvnw`, então basta ter o Java instalado: na primeira execução, o wrapper baixa o Maven sozinho.

O código-fonte fica em `src/main/java/`, os programas Jack e os XML de referência em `src/test/resources/` e o script de validação em `scripts/`.

## Analisador léxico

O scanner lê um arquivo `.jack` e imprime os tokens em XML, no formato dos arquivos de referência do Nand2Tetris. Ele reconhece palavras reservadas, símbolos, constantes inteiras, constantes de string e identificadores, e pula espaços e comentários. Diante de um caractere inválido, de uma string sem aspas finais ou de um comentário de bloco sem fechamento, o scanner lança uma `LexicalException` com a linha do problema, e o programa termina com uma mensagem como `lexical error at line 2: unexpected character '@'`.

O código fica no pacote `lexer`, em `src/main/java/lexer/`. O `Scanner.java` reconhece os tokens. O `Token.java` guarda o tipo, o lexema e a linha de cada token e gera o seu XML. O `TokenType.java` lista os tipos de token. O `Lexicon.java` guarda as tabelas de palavras reservadas e de símbolos, e o `LexicalException.java` define a exceção dos erros léxicos. O ponto de entrada é o `src/main/java/Main.java`.

Para compilar:

```bash
javac -sourcepath src/main/java -d bin src/main/java/Main.java
```

Os `.class` vão para `bin/`. O `-sourcepath` faz o `javac` encontrar sozinho as classes do pacote `lexer`. Para rodar o scanner, o arquivo Jack é passado como argumento:

```bash
java -cp bin Main src/test/resources/Square.jack
```

A saída começa assim:

```xml
<tokens>
<keyword> class </keyword>
<identifier> Square </identifier>
<symbol> { </symbol>
...
</tokens>
```

### Testes

O XML gerado para cada programa Jack é comparado com o `*T.xml` oficial por meio do `diff`. O script `scripts/validate.sh` faz isso para `Main.jack`, `Square.jack` e `SquareGame.jack`: compila o projeto, roda o scanner e imprime `OK` ou `FAIL` para cada arquivo.

```bash
./scripts/validate.sh
```

Com tudo certo, a saída é:

```
OK   Main
OK   Square
OK   SquareGame
```

O `diff` usa `--strip-trailing-cr` porque os arquivos oficiais terminam as linhas com `\r\n`. Fora isso, a comparação é exata.

## Licença

MIT, conforme o arquivo [LICENSE](LICENSE).