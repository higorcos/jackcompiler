# jackcompiler

Compilador da linguagem Jack escrito em Java. Jack é uma linguagem pequena, orientada a objetos, criada para o curso Nand2Tetris. O compilador lê programas Jack e gera código para a máquina virtual do mesmo curso. Esse código roda no VM Emulator, o emulador que o curso fornece.

O projeto segue o tutorial do livro didático da disciplina de Compiladores do curso de Engenharia da Computação da Universidade Federal do Maranhão. A construção passou por três etapas. A primeira fez o analisador léxico, a segunda o analisador sintático e a terceira a tabela de símbolos e o gerador de código. Este README descreve o compilador completo e aponta para a documentação detalhada de cada parte.

## Sumário

- [Autores](#autores)
- [Material de referência](#material-de-referência)
- [Requisitos](#requisitos)
- [Arquivos de build](#arquivos-de-build)
- [Como compilar e executar](#como-compilar-e-executar)
- [Como rodar no VM Emulator](#como-rodar-no-vm-emulator)
- [Testes e validação](#testes-e-validação)
- [Status da validação](#status-da-validação)
- [Estrutura do repositório](#estrutura-do-repositório)
- [Documentação detalhada](#documentação-detalhada)
- [Decisões de projeto](#decisões-de-projeto)
- [Limitações conhecidas](#limitações-conhecidas)
- [Desafios e aprendizados](#desafios-e-aprendizados)
- [Uso de Inteligência Artificial](#uso-de-inteligência-artificial)
- [Vídeo de demonstração](#vídeo-de-demonstração)
- [Licença](#licença)

## Autores

- ANTONIO LUCAS DA SILVA VALE | 20250013480
- HIGOR PINHEIRO COSTA | 20250013695

## Material de referência

Três fontes aparecem ao longo deste repositório.

A primeira é o livro didático da disciplina, *Compiladores — do zero ao Hack*, publicado em lambdageo-edu.github.io. Ele funciona como tutorial e mostra, capítulo a capítulo, como escrever cada parte do compilador em Java. Neste repositório, a palavra tutorial se refere a ele.

A segunda é o livro em que o tutorial se baseia, *The Elements of Computing Systems*, de Noam Nisan e Shimon Schocken, publicado pela MIT Press. O livro é a base do curso Nand2Tetris e divide o conteúdo em projetos numerados. O Project 10 constrói o analisador sintático e o Project 11 constrói o gerador de código.

A terceira é o pacote de software que os autores do livro distribuem no site [nand2tetris.org](https://www.nand2tetris.org/). O pacote traz os programas Jack de teste de cada projeto, os arquivos XML de referência do Project 10 e as ferramentas oficiais, entre elas o VM Emulator e o JackCompiler, o compilador de referência escrito pelos autores.

## Requisitos

O projeto usa Java 17 ou superior. Não é preciso instalar o Maven, porque o repositório traz o Maven Wrapper, que baixa o Maven na primeira execução.

No Linux e no macOS, os comandos usam o script `./mvnw`. No Windows, o equivalente é o `mvnw.cmd`. O script de validação, `scripts/validate.sh`, precisa de um terminal bash, como o Git Bash ou o WSL no Windows.

## Arquivos de build

| Arquivo | Papel |
| --- | --- |
| `pom.xml` | Configura o Maven. Define o Java 17, a dependência do JUnit 5 para os testes e o plugin que gera o `target/jackcompiler.jar` com a classe `Main` como ponto de entrada |
| `mvnw` e `mvnw.cmd` | Os scripts do Maven Wrapper, para Linux e macOS e para Windows |
| `.mvn/wrapper/` | A configuração do Maven Wrapper, com a versão do Maven que ele baixa |
| `.gitignore` | Deixa fora do Git as classes compiladas, as saídas geradas e os arquivos de IDE |

## Como compilar e executar

Há duas formas de usar o compilador. A primeira gera um arquivo jar executável. O `package` roda os testes antes de montar o jar.

```bash
./mvnw package
java -jar target/jackcompiler.jar <arquivo.jack | pasta>
```

A segunda usa as classes compiladas, sem o jar.

```bash
./mvnw compile
java -cp target/classes Main <arquivo.jack | pasta>
```

O compilador grava cada saída na mesma pasta do arquivo `.jack`. Para não misturar arquivos gerados com os programas de teste, os exemplos abaixo copiam o programa para `output/`, uma pasta que o Git ignora.

### Compilar uma pasta

Um programa Jack costuma ter várias classes, uma por arquivo. Com uma pasta, o compilador lê todos os `.jack` dela, sem entrar em subpastas, e grava um `.vm` para cada classe. É dessa pasta que o VM Emulator carrega o programa.

```bash
mkdir -p output/Square
cp src/test/resources/input/project11/Square/*.jack output/Square/
java -jar target/jackcompiler.jar output/Square
```

O programa responde uma linha por classe.

```
wrote output/Square/Main.vm
wrote output/Square/Square.vm
wrote output/Square/SquareGame.vm
```

### Compilar um arquivo

Com um arquivo, o compilador gera só o `.vm` daquela classe.

```bash
mkdir -p output/Seven
cp src/test/resources/input/project11/Seven/Main.jack output/Seven/
java -jar target/jackcompiler.jar output/Seven/Main.jack
```

### Saídas das etapas anteriores

As opções `--tokens` e `--xml` mantêm as saídas em XML das duas primeiras etapas. A primeira grava a lista de tokens do analisador léxico, e a segunda grava a árvore sintática. As duas aceitam arquivo ou pasta.

```bash
mkdir -p output/ArrayTest
cp src/test/resources/input/project10/ArrayTest/Main.jack output/ArrayTest/
java -jar target/jackcompiler.jar --tokens output/ArrayTest
java -jar target/jackcompiler.jar --xml output/ArrayTest
```

Os dois comandos gravam `MainT.xml` e `MainP.xml` ao lado do `Main.jack`. Com um arquivo só, o caminho de saída pode vir como último argumento, como em `--xml Main.jack arvore.xml`.

### Erros

Diante de um erro, o compilador mostra o arquivo, a linha, a coluna e o problema, e termina com código 1.

```
Main.jack: lexical error at line 2, column 5: unexpected character '@'
Main.jack: syntax error at line 3, column 5: expected SEMICOLON, found 'let'
Main.jack: semantic error at line 4, column 9: undeclared variable 'x'
```

Um caminho que não existe gera a mensagem `no such file or directory`, e uma pasta sem arquivos Jack gera `no .jack files in`.

## Como rodar no VM Emulator

O VM Emulator faz parte do pacote de software do Nand2Tetris. No Linux, ele abre com o script `tools/VMEmulator.sh` do pacote.

1. Compile a pasta do programa com o jackcompiler.
2. No VM Emulator, abra a pasta do programa pelo botão de carregar programa.
3. Escolha *No animation* no controle de animação e clique em *Run*.

O emulador já traz o sistema operacional do Jack, com as classes `Math`, `Memory`, `Screen`, `Output`, `Keyboard`, `String`, `Array` e `Sys`. A pasta do programa só precisa dos `.vm` das classes do próprio programa.

O ConvertToBin, um dos programas de teste do Project 11, converte para binário o número guardado na posição 8000 da memória. O valor precisa entrar em `RAM[8000]` depois de carregar o programa e antes de clicar em *Run*. Com *No animation*, o painel da memória só atualiza depois de *Stop*.

## Testes e validação

O Maven roda os testes automatizados com JUnit 5.

```bash
./mvnw test
```

O resumo do Maven mostra os 133 testes passando.

```
[INFO] Tests run: 133, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

O script `validate.sh` compila o projeto e compara as saídas do compilador com arquivos de referência. Ele copia cada programa para `output/`, roda o compilador na cópia e confere arquivo por arquivo com `diff`.

```bash
./scripts/validate.sh
```

Para o léxico e o sintático, a referência são os XML que acompanham os três programas do Project 10 no pacote do Nand2Tetris. Para o gerador de código, o pacote não traz arquivos `.vm` de referência. A dupla rodou o JackCompiler oficial nos seis programas do Project 11 e guardou as saídas em `src/test/resources/expected/vm/`. O [documento do gerador de código](docs/codegen.md#comparação-com-o-compilador-oficial) mostra os comandos usados.

## Status da validação

Os 133 testes passam. São 19 do analisador léxico, 48 do analisador sintático e 66 da tabela de símbolos e do gerador de código. O `validate.sh` mostra uma linha por programa, e todas saem OK.

```
OK   lexer/ArrayTest
OK   lexer/ExpressionLessSquare
OK   lexer/Square

OK   parser/ArrayTest
OK   parser/ExpressionLessSquare
OK   parser/Square

OK   vm/Average
OK   vm/ComplexArrays
OK   vm/ConvertToBin
OK   vm/Pong
OK   vm/Seven
OK   vm/Square
```

Nos três programas do Project 10, os tokens e as árvores saem idênticos às referências, inclusive na indentação.

| Programa | Arquivos | O que exercita | Léxico | Sintático |
| --- | --- | --- | --- | --- |
| ExpressionLessSquare | `Main.jack`, `Square.jack`, `SquareGame.jack` | Classes, subrotinas, declarações e comandos, com expressões reduzidas a termos simples | Idêntico | Idêntico |
| Square | `Main.jack`, `Square.jack`, `SquareGame.jack` | Os mesmos arquivos com expressões completas, operadores e chamadas | Idêntico | Idêntico |
| ArrayTest | `Main.jack` | Acesso a array dos dois lados do `=` e strings como argumento | Idêntico | Idêntico |

Os seis programas do Project 11 rodaram no VM Emulator. A tabela mostra o que a dupla observou em cada um. A última coluna indica se os `.vm` gerados são idênticos, byte a byte, aos do JackCompiler oficial.

| Programa | O que exercita | Resultado no VM Emulator | Igual ao oficial |
| --- | --- | --- | --- |
| Seven | Expressão aritmética, `do` e chamada ao sistema operacional | Imprime 7 no canto superior esquerdo da tela | Sim |
| ConvertToBin | `while`, `if` aninhados, funções e os operadores `&` e `~` | Com 13 em `RAM[8000]`, as posições 8001 a 8004 recebem 1, 0, 1 e 1, e as demais até 8016 ficam com 0 | Sim |
| Square | Construtores, métodos, campos e chamadas entre classes | O quadrado anda com as setas, cresce com `x`, diminui com `z` e o programa termina com `q` | Sim, nas três classes |
| Average | Arrays, strings constantes e leitura do teclado | Com três números, 4, 5 e 9, imprime `The average is 6` | Sim |
| Pong | Programa orientado a objetos completo, com variáveis `static` | A bola quica, a raquete segue as setas, o placar conta os pontos e o jogo mostra `Game Over` ao perder | Sim, nas quatro classes |
| ComplexArrays | Índices aninhados, arrays de arrays e expressões nos índices | Os cinco testes imprimem o valor esperado igual ao obtido, 5, 40, 0, 77 e 110 | Sim |

## Estrutura do repositório

| Pasta | Conteúdo |
| --- | --- |
| `src/main/java/` | O `Main.java` e os pacotes `lexer`, `parser` e `codegen` |
| `src/test/java/` | Os testes JUnit, com um pacote por módulo |
| `src/test/resources/input/project10/` | Os três programas do Project 10, um por pasta |
| `src/test/resources/input/project11/` | Os seis programas do Project 11, um por pasta |
| `src/test/resources/expected/lexer/` | Os tokens de referência do Project 10, numa pasta por programa |
| `src/test/resources/expected/parser/` | As árvores de referência do Project 10, numa pasta por programa |
| `src/test/resources/expected/vm/` | Os `.vm` gerados pelo JackCompiler oficial, numa pasta por programa |
| `scripts/` | O `validate.sh` |
| `docs/` | A documentação detalhada |
| `output/` | As cópias dos programas e as saídas do `validate.sh`, fora do Git |

Dentro de `output/`, a pasta `xml/` recebe os `MainT.xml` e `MainP.xml` do Project 10, e a pasta `vm/` recebe os `.vm` do Project 11, sempre ao lado das cópias dos `.jack`.

O Project 10 e o Project 11 têm versões diferentes do Square. O `Main.jack` do Project 10 traz código extra que os autores incluíram para testar o analisador sintático, por isso cada versão fica na pasta do seu projeto.

## Documentação detalhada

- [Gramática da linguagem Jack](docs/grammar.md)
- [Analisador léxico](docs/lexer.md)
- [Analisador sintático](docs/parser.md)
- [Tabela de símbolos e gerador de código](docs/codegen.md)

Os documentos dos módulos descrevem o funcionamento, os testes e as diferenças em relação ao tutorial.

## Decisões de projeto

O projeto usa Maven com JUnit 5, que o tutorial não usa. Os testes ficam no padrão do Maven, com entradas, referências e saídas em pastas separadas.

O parser usa descida recursiva, técnica em que cada regra da gramática vira uma função e as funções chamam umas às outras na ordem da gramática. O gerador de código não percorre a árvore de novo. Cada função do parser emite o código VM enquanto lê os tokens, na mesma passada que monta o XML.

Os três modos gravam a saída ao lado do `.jack`. O modo padrão gera o `.vm`, o `--tokens` gera o `XxxT.xml` e o `--xml` gera o `XxxP.xml`. As referências ficam em `src/test/resources/expected/`, e a validação só lê dessa pasta.

O código VM sai idêntico ao do JackCompiler oficial. Para isso, os arrays empilham o índice antes do endereço base, como o compilador oficial faz, embora o tutorial e o livro façam o contrário. O [documento do gerador de código](docs/codegen.md#diferenças-em-relação-ao-tutorial) explica essa e as demais diferenças.

## Limitações conhecidas

A única checagem semântica é a de variável não declarada. Por isso, alguns erros passam pela compilação.

- Uma variável declarada duas vezes no mesmo escopo não gera erro. A segunda declaração substitui a primeira.
- O compilador não confere tipos. Atribuir uma string a uma variável `int` compila sem aviso.
- Uma chamada a uma subrotina ou classe que não existe compila sem erro e só falha no VM Emulator.
- O compilador não confere o número de argumentos de uma chamada.
- A compilação para no primeiro erro e não tenta continuar a análise.

## Desafios e aprendizados

O primeiro tropeço da terceira etapa veio do próprio tutorial. O `VMWriter` dele escreve o nome do segmento da VM a partir do nome da constante do enum Java, e com isso gerava `push const 5` e `pop arg 0`. A VM só aceita `constant` e `argument`. Os testes unitários do `VMWriter` pegaram o erro antes de qualquer programa rodar.

Ligar a tabela de símbolos ao parser quebrou parte dos testes da etapa anterior. Eles usavam variáveis soltas, como `x` e `y`, que agora precisam de declaração. A dupla criou um auxiliar de teste que declara essas variáveis antes de rodar o parser. No mesmo passo, o compilador ganhou o erro semântico de variável não declarada, porque o código do tutorial terminava com um `NullPointerException` nesse caso.

O VM Emulator também cobrou tempo. A memória só aceita edição antes de o programa rodar, e com *No animation* os painéis só mostram os valores depois de *Stop*. O botão continua em *Running* mesmo depois de *Program Halted*, porque o `Sys.halt` do sistema operacional entra num laço infinito. Até entender isso, o ConvertToBin parecia não responder.

A descoberta mais interessante veio da comparação com o JackCompiler oficial. Código idêntico não era requisito, mas a dupla quis uma verificação automática além dos testes visuais. Nove das onze classes saíram idênticas na primeira tentativa. As duas restantes, do Average e do ComplexArrays, diferiam só na ordem de empilhar a base e o índice dos arrays. Como a soma dá o mesmo endereço nas duas ordens, a dupla adotou a ordem do compilador oficial e passou a ter as onze classes idênticas, conferidas pelo `validate.sh` a cada mudança.

Quanto aos aprendizados, a etapa mostrou como a pilha da VM combina com a árvore sintática. O parser já visitava os nós na ordem certa, e a geração em notação pós-fixa, com os operandos antes do operador, coube nos mesmos métodos. Os segmentos `this` e `that`, apontados por `pointer 0` e `pointer 1`, mostraram como objetos e arrays viram cálculo de endereços. Em `let a[i] = b[j]`, os dois lados usam o `pointer 1`, e guardar o valor em `temp 0` antes de apontar para `a[i]` evita que um lado apague o endereço do outro.

Os desafios das etapas anteriores estão nos documentos do [analisador léxico](docs/lexer.md#desafios-e-diferenças-em-relação-ao-tutorial) e do [analisador sintático](docs/parser.md#diferenças-em-relação-ao-tutorial).

## Uso de Inteligência Artificial

A dupla usou o Claude, assistente de IA da Anthropic, pelo site claude.ai com o recurso Projetos. O modelo foi o Claude Opus 5.5, que aparece como Opus 5.5 no seletor da ferramenta, com esforço médio. Os dois integrantes usaram o mesmo modelo, desde o analisador léxico até o gerador de código.

Em cada etapa, a dupla criou um projeto no Claude com os capítulos do tutorial, a gramática da linguagem Jack, a especificação da VM, a descrição da etapa e o código do repositório. Com esse contexto, a IA ajudou nas tarefas abaixo.

- Ler o tutorial antes da implementação e apontar trechos que não batiam com a gramática, com a especificação da VM ou com os arquivos oficiais.
- Sugerir a divisão do trabalho entre os integrantes e a sequência de commits, com as mensagens de cada um.
- Tirar dúvidas e aprofundar partes do tutorial, como os segmentos da VM e o protocolo de chamada de métodos.
- Escrever testes unitários com JUnit, parte em que um erro no valor esperado passaria despercebido com facilidade.
- Escrever e revisar a documentação, incluindo este README.

Cada sugestão passou pelos mesmos filtros antes de entrar no repositório. O `./mvnw test` e o `./scripts/validate.sh` rodaram antes de cada commit, e a dupla conferiu os seis programas do Project 11 no VM Emulator. A comparação byte a byte com o JackCompiler oficial deu uma checagem independente da IA. Quando uma sugestão falhava nesses testes, a dupla corrigia ou descartava.

Na terceira etapa, o Higor implementou o `VMWriter`, as expressões, os comandos e a geração dos arquivos `.vm`, até o Seven e o ConvertToBin funcionarem, e marcou esse ponto com a tag `etapa1` do Git. O Lucas implementou a tabela de símbolos, os construtores, os métodos, os arrays, a entrada por pasta, o jar, a comparação com o compilador oficial e a validação do ExpressionLessSquare e do ArrayTest, e escreveu este README. A dupla responde por todo o código do repositório.

## Vídeo de demonstração

[Vídeo de demonstração no Google Drive](https://drive.google.com/file/d/1-Fz0JKrwR_76sDvPlnVJbH7to488YQxS/view?usp=sharing)

## Licença

MIT, conforme o arquivo [LICENSE](LICENSE).