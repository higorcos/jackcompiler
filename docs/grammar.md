# Gramática da linguagem Jack

[Voltar ao README](../README.md)

A gramática abaixo é a que o analisador sintático implementa. Ela segue o apêndice A do tutorial, que reproduz a gramática do livro *The Elements of Computing Systems*.

A notação usa a convenção do Nand2Tetris. Um texto entre aspas simples é um símbolo terminal literal, e um nome sem aspas é uma regra. Os parênteses agrupam, a barra separa alternativas, o `?` indica zero ou uma ocorrência e o `*` indica zero ou mais.

## Elementos léxicos

| Token | Definição |
| --- | --- |
| `keyword` | `class`, `constructor`, `function`, `method`, `field`, `static`, `var`, `int`, `char`, `boolean`, `void`, `true`, `false`, `null`, `this`, `let`, `do`, `if`, `else`, `while`, `return` |
| `symbol` | `{ } ( ) [ ] . , ; + - * / & \| < > = ~` |
| `integerConstant` | Um número decimal entre 0 e 32767. O sinal de um número negativo é o operador unário `-` |
| `stringConstant` | Uma sequência de caracteres entre aspas duplas, sem aspas internas e sem quebra de linha |
| `identifier` | Uma sequência de letras, dígitos e `_` que não começa com dígito |

## Estrutura do programa

```
class           'class' className '{' classVarDec* subroutineDec* '}'
classVarDec     ('static' | 'field') type varName (',' varName)* ';'
type            'int' | 'char' | 'boolean' | className
subroutineDec   ('constructor' | 'function' | 'method') ('void' | type) subroutineName
                '(' parameterList ')' subroutineBody
parameterList   ((type varName) (',' type varName)*)?
subroutineBody  '{' varDec* statements '}'
varDec          'var' type varName (',' varName)* ';'
className       identifier
subroutineName  identifier
varName         identifier
```

## Comandos

```
statements      statement*
statement       letStatement | ifStatement | whileStatement | doStatement | returnStatement
letStatement    'let' varName ('[' expression ']')? '=' expression ';'
ifStatement     'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
whileStatement  'while' '(' expression ')' '{' statements '}'
doStatement     'do' subroutineCall ';'
returnStatement 'return' expression? ';'
```

## Expressões

```
expression      term (op term)*
term            integerConstant | stringConstant | keywordConstant | varName
                | varName '[' expression ']' | subroutineCall | '(' expression ')' | unaryOp term
subroutineCall  subroutineName '(' expressionList ')'
                | (className | varName) '.' subroutineName '(' expressionList ')'
expressionList  (expression (',' expression)*)?
op              '+' | '-' | '*' | '/' | '&' | '|' | '<' | '>' | '='
unaryOp         '-' | '~'
keywordConstant 'true' | 'false' | 'null' | 'this'
```

## Observações

Jack não tem precedência entre operadores. Por isso a regra `expression` tem um nível só, e `a + b * c` soma `a` e `b` antes de multiplicar por `c`. Só os parênteses mudam o agrupamento.

Em quase toda a gramática, o primeiro token basta para escolher a regra. As exceções são `term` e `subroutineCall`. Quando um termo começa com um identificador, o parser olha o token seguinte para decidir. Um `[` indica acesso a array, um `(` ou um `.` indica chamada de subrotina, e qualquer outro símbolo indica uma variável simples. O parser lê o identificador primeiro e só depois decide, o que mantém a análise com um token de antecipação.