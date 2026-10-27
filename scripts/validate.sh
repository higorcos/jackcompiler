#!/bin/bash
# Compila o projeto, gera as saidas em output e compara com os XML oficiais em src/test/resources/expected.
set -e

javac -sourcepath src/main/java -d bin src/main/java/Main.java

for name in Main Square SquareGame; do
  java -cp bin Main --tokens src/test/resources/input/$name.jack > /dev/null
  java -cp bin Main src/test/resources/input/$name.jack > /dev/null
done

fail=0
check() {
  if diff --strip-trailing-cr -q "output/$1" "src/test/resources/expected/$1" > /dev/null; then
    echo "OK   $1"
  else
    echo "FAIL $1"
    fail=1
  fi
}

for name in Main Square SquareGame; do
  check "lexer/${name}T.xml"
done
for name in Main Square SquareGame; do
  check "parser/$name.xml"
done
exit $fail