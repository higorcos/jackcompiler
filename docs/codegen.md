# Tabela de símbolos e gerador de código

[Voltar ao README](../README.md)

- [Funcionamento](#funcionamento)
- [Tabela de símbolos](#tabela-de-símbolos)
- [VMWriter](#vmwriter)
- [Expressões e comandos](#expressões-e-comandos)
- [Subrotinas, objetos e arrays](#subrotinas-objetos-e-arrays)
- [Exemplo](#exemplo)
- [Erros semânticos](#erros-semânticos)
- [Testes](#testes)
- [Comparação com o compilador oficial](#comparação-com-o-compilador-oficial)
- [Diferenças em relação ao tutorial](#diferenças-em-relação-ao-tutorial)

## Funcionamento

A terceira etapa traduz cada classe Jack para o código da máquina virtual do Nand2Tetris. A VM leva o nome de Hack, o computador que o curso constrói, e trabalha com uma pilha. As instruções empilham e desempilham valores de segmentos de memória, fazem contas com o topo da pilha e chamam funções. O capítulo 7 e o apêndice B do tutorial descrevem essa especificação.

O gerador não percorre a árvore numa segunda passada. O parser da etapa anterior já visita cada regra da gramática na ordem certa, então cada função do parser passou a emitir o código VM correspondente enquanto lê os tokens. O parser continua gerando o XML na mesma passada, e o `Main` escolhe qual saída gravar. Sem opção, grava o `.vm`. Com `--xml`, grava a árvore.

Duas classes novas, no pacote `codegen`, apoiam o parser. A `SymbolTable` guarda o que se sabe sobre cada variável, e o `VMWriter` escreve as instruções no formato da VM.

## Tabela de símbolos

A `SymbolTable`, em `src/main/java/codegen/SymbolTable.java`, segue o capítulo 8 do tutorial. Ela tem dois escopos. O escopo da classe guarda as variáveis `static` e `field`, e o escopo da subrotina guarda os parâmetros e as variáveis locais. Cada símbolo registra o nome, o tipo, a categoria e um índice, que conta a partir de 0 dentro de cada categoria.

O parser registra cada declaração no momento em que a lê. No início de cada subrotina, o `startSubroutine` limpa o escopo da subrotina e zera os contadores de parâmetros e locais. Num método, o parser registra `this` como primeiro parâmetro antes dos parâmetros declarados, porque a VM entrega o objeto em `argument 0`.

Para usar uma variável, o parser chama `resolve`, que procura o nome primeiro no escopo da subrotina e depois no da classe. A categoria define o segmento da VM.

| Categoria em Jack | Segmento da VM |
| --- | --- |
| `static` | `static` |
| `field` | `this` |
| parâmetro | `argument` |
| `var` | `local` |

## VMWriter

O `VMWriter`, em `src/main/java/codegen/VMWriter.java`, segue o capítulo 9 do tutorial. Ele tem um método para cada tipo de instrução, como `writePush`, `writePop`, `writeArithmetic`, `writeLabel`, `writeIf`, `writeCall`, `writeFunction` e `writeReturn`, e acumula as linhas num `StringBuilder`. Os segmentos e os comandos aritméticos são enums, e cada segmento guarda o nome que a VM espera, como `constant` e `argument`.

## Expressões e comandos

A dupla dividiu a etapa em duas fases, uma por integrante. A primeira cobriu o capítulo 9 do tutorial e terminou com o Seven e o ConvertToBin funcionando. A tag `etapa1` do Git marca esse ponto.

As expressões saem em notação pós-fixa. O parser gera o código do primeiro termo, depois o do segundo e só então o operador. Multiplicação e divisão viram chamadas ao sistema operacional, `call Math.multiply 2` e `call Math.divide 2`. O `-` unário gera `neg`, e o `~` gera `not`.

| Jack | Código VM |
| --- | --- |
| número `n` | `push constant n` |
| `false` e `null` | `push constant 0` |
| `true` | `push constant 0` e `not` |
| `this` | `push pointer 0` |
| string `"ab"` | `push constant 2`, `call String.new 1` e um `call String.appendChar 2` por caractere |
| variável | `push` do segmento e do índice que a tabela informa |

Nos comandos, o `let` calcula o lado direito e desempilha o resultado na variável. O `do` descarta o valor de retorno com `pop temp 0`. Uma subrotina `void` empilha 0 antes do `return`, porque toda função da VM devolve um valor.

O `if` usa os rótulos `IF_TRUEn`, `IF_FALSEn` e `IF_ENDn`, e o `while` usa `WHILE_EXPn` e `WHILE_ENDn`. Cada tipo de comando tem um contador próprio, zerado no início de cada subrotina. O parser monta os nomes dos rótulos e avança o contador antes de compilar o bloco interno, então um `if` dentro de outro recebe um número diferente.

## Subrotinas, objetos e arrays

A segunda fase cobriu o capítulo 10 do tutorial e terminou com o Square, o Average, o Pong e o ComplexArrays funcionando.

Cada subrotina vira uma linha `function Classe.nome nLocais`, em que `nLocais` é o número de variáveis locais. Um construtor aloca o objeto antes do corpo. Ele empilha o número de campos da classe, chama `Memory.alloc` e guarda o endereço recebido em `pointer 0`. No fim, o `return this` gera `push pointer 0`. Um método ancora o objeto recebido com `push argument 0` e `pop pointer 0`. Depois disso, os campos do objeto ficam acessíveis pelo segmento `this`.

As chamadas têm três formas, e o parser decide qual é pela tabela de símbolos.

| Chamada | Como o parser reconhece | Código VM |
| --- | --- | --- |
| `Classe.f(x)` | O nome antes do ponto não está na tabela | Empilha os argumentos e chama `Classe.f` com n argumentos |
| `obj.m(x)` | O nome antes do ponto é uma variável | Empilha `obj`, empilha os argumentos e chama `Tipo.m` com n + 1 argumentos, em que `Tipo` é o tipo de `obj` |
| `m(x)` | Não há ponto | Empilha `pointer 0`, empilha os argumentos e chama `ClasseAtual.m` com n + 1 argumentos |

Um array em Jack guarda o endereço do primeiro elemento. Para ler `a[i]`, o parser soma o índice ao endereço, grava o resultado em `pointer 1` e lê `that 0`. Na escrita `let a[i] = expr`, o endereço calculado fica na pilha enquanto o parser compila `expr`. O valor vai para `temp 0`, o endereço vai para `pointer 1`, e o valor volta de `temp 0` para `that 0`. Essa ordem protege o endereço do lado esquerdo quando `expr` também acessa um array, como em `let a[i] = b[j]`.

## Exemplo

A classe abaixo cobre um `while`, uma escrita em array e uma chamada de método.

```
class Main {
  function void main() {
    var Array a;
    var int i;
    var Square s;
    let a = Array.new(3);
    let i = 0;
    while (i < 3) {
      let a[i] = i * 2;
      let i = i + 1;
    }
    let s = Square.new(0, 0, 30);
    do s.moveRight();
    return;
  }
}
```

O compilador gera o código abaixo. As três variáveis locais aparecem no `function Main.main 3`. O laço usa os rótulos `WHILE_EXP0` e `WHILE_END0`, e o `not` inverte a condição para sair do laço quando ela fica falsa. Na chamada `s.moveRight()`, o objeto `s` entra como único argumento.

```
function Main.main 3
push constant 3
call Array.new 1
pop local 0
push constant 0
pop local 1
label WHILE_EXP0
push local 1
push constant 3
lt
not
if-goto WHILE_END0
push local 1
push local 0
add
push local 1
push constant 2
call Math.multiply 2
pop temp 0
pop pointer 1
push temp 0
pop that 0
push local 1
push constant 1
add
pop local 1
goto WHILE_EXP0
label WHILE_END0
push constant 0
push constant 0
push constant 30
call Square.new 3
pop local 2
push local 2
call Square.moveRight 1
pop temp 0
push constant 0
return
```

## Erros semânticos

Quando o código usa uma variável que não foi declarada, a tabela de símbolos não encontra o nome e o parser lança uma `SemanticException`, em `src/main/java/parser/SemanticException.java`. O programa termina com código 1 e uma mensagem no mesmo formato dos erros léxicos e sintáticos.

```
Main.jack: semantic error at line 4, column 9: undeclared variable 'x'
```

## Testes

Os testes do gerador seguem a mesma ideia dos testes do parser. Cada teste compila um trecho pequeno de Jack e compara o código VM gerado com o esperado. Vários casos vêm direto dos exemplos dos capítulos do tutorial, como a classe `Point` do capítulo 8, o Seven e a função `raiz`, que os capítulos 9 e 10 usam como exemplo.

| Classe de teste | Testes | O que cobre |
| --- | --- | --- |
| `codegen/SymbolTableTest` | 6 | Escopos, índices por categoria, `startSubroutine` e `resolve` |
| `codegen/VMWriterTest` | 6 | O texto de cada tipo de instrução |
| `parser/SymbolTableParserTest` | 6 | O registro das declarações da classe `Point` durante a análise |
| `parser/ExpressionCodeTest` | 15 | Constantes, strings, operadores e expressões aninhadas |
| `parser/StatementCodeTest` | 12 | `let`, `do`, `return`, `if` e `while`, com rótulos aninhados |
| `parser/VariableCodeTest` | 5 | Os quatro segmentos e o erro de variável não declarada |
| `parser/SubroutineCodeTest` | 11 | Funções, construtores, métodos e as três formas de chamada |
| `parser/ArrayCodeTest` | 5 | Leitura e escrita de elementos de array, inclusive com índices aninhados |

O `validate.sh` completa os testes unitários. Ele compila os seis programas do Project 11 e compara cada `.vm` com a saída do compilador oficial, como descreve a próxima seção.

## Comparação com o compilador oficial

O pacote de software do Nand2Tetris traz o JackCompiler, o compilador de referência escrito pelos autores do livro. Código idêntico ao dele não é requisito, porque nomes de rótulos e outras escolhas podem variar sem mudar o comportamento. Mesmo assim, a comparação com o compilador oficial dá uma verificação automática que os testes no VM Emulator não oferecem.

A dupla rodou o JackCompiler oficial sobre cópias dos seis programas e guardou as saídas em `src/test/resources/expected/vm/`, numa pasta por programa. Os comandos abaixo reproduzem esse passo, com o pacote do Nand2Tetris instalado em `~/nand2tetris`.

```bash
for p in Seven ConvertToBin Square Average Pong ComplexArrays; do
  mkdir -p /tmp/oficial/$p
  cp src/test/resources/input/project11/$p/*.jack /tmp/oficial/$p/
  ~/nand2tetris/tools/JackCompiler.sh /tmp/oficial/$p
  mkdir -p src/test/resources/expected/vm/$p
  cp /tmp/oficial/$p/*.vm src/test/resources/expected/vm/$p/
done
```

O `.gitignore` ignora os arquivos `.vm` gerados, com exceção dos que ficam em `expected/vm/`. O `validate.sh` copia os programas para `output/vm/`, compila cada pasta e compara os `.vm` com os de referência usando `diff`. As onze classes dos seis programas saem idênticas.

## Diferenças em relação ao tutorial

O `VMWriter` do tutorial monta o nome do segmento a partir do nome da constante do enum, convertido para minúsculas. Como o enum usa `CONST` e `ARG`, o código saía com `push const 5` e `pop arg 0`, que a VM não aceita. No projeto, cada constante do enum guarda o nome que a VM espera, `constant` e `argument`. Os testes do `VMWriter` acusaram o problema no mesmo commit que criou a classe.

O capítulo 8 sugere testar a tabela de símbolos acrescentando atributos aos identificadores do XML, com a categoria e o índice de cada um, para conferência visual. A dupla preferiu testar a tabela com JUnit. Os atributos extras mudariam o XML e quebrariam a comparação com os arquivos de referência do Project 10, que continua valendo.

No tutorial, o `resolve` devolve `null` para um nome desconhecido, e o código seguinte usa esse resultado sem conferir. Uma variável não declarada terminava num `NullPointerException`, sem indicar onde estava o erro. O parser passou a lançar uma `SemanticException` com a linha, a coluna e o nome da variável.

Ligar a tabela de símbolos ao parser quebrou os testes de XML da etapa anterior que usavam variáveis sem declaração, como `let x = y;`. Para manter esses testes, a dupla criou o auxiliar `parserWithVars`, que declara as variáveis do trecho antes de rodar o parser.

O `switch` de operadores do tutorial não tem caso padrão. O projeto acrescentou um que lança erro, para que um operador não tratado não passe sem gerar código.

A diferença mais visível está nos arrays. O tutorial e o livro empilham o endereço base do array e depois o índice. O JackCompiler oficial empilha o índice primeiro. As duas ordens dão o mesmo resultado, porque a soma não depende da ordem. A diferença apareceu quando a dupla comparou as saídas com as do compilador oficial. Nove das onze classes saíram idênticas, e as outras duas, do Average e do ComplexArrays, diferiam só nessas linhas. A dupla adotou a ordem do compilador oficial, e as onze classes passaram a coincidir byte a byte.

O `true` segue o tutorial, o livro e o compilador oficial, com `push constant 0` e `not`. Outra sequência comum, `push constant 1` e `neg`, deixa o mesmo -1 na pilha.