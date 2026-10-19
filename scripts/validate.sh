#!/bin/bash
# Compila o scanner e compara a saida com os T.xml oficiais.
set -e

javac -sourcepath src/main/java -d bin src/main/java/Main.java

fail=0
for name in Main Square SquareGame; do
  if java -cp bin Main src/test/resources/$name.jack | diff --strip-trailing-cr -q - src/test/resources/${name}T.xml > /dev/null; then
    echo "OK   $name"
  else
    echo "FAIL $name"
    fail=1
  fi
done
exit $fail