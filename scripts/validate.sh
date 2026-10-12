#!/bin/bash
# Compila o scanner e compara a saida com os T.xml oficiais.
set -e

javac -d bin src/*.java

fail=0
for name in Main Square SquareGame; do
  if java -cp bin Main tests/$name.jack | diff --strip-trailing-cr -q - tests/${name}T.xml > /dev/null; then
    echo "OK   $name"
  else
    echo "FAIL $name"
    fail=1
  fi
done
exit $fail
