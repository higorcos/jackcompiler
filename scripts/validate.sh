#!/bin/bash
# Compila o projeto e compara as saidas com os XML oficiais: tokens com os T.xml e arvore com os .xml.
set -e

javac -sourcepath src/main/java -d bin src/main/java/Main.java

fail=0
for name in Main Square SquareGame; do
  if java -cp bin Main --tokens src/test/resources/$name.jack | diff --strip-trailing-cr -q - src/test/resources/${name}T.xml > /dev/null; then
    echo "OK   tokens $name"
  else
    echo "FAIL tokens $name"
    fail=1
  fi
done

for name in Main Square SquareGame; do
  if java -cp bin Main src/test/resources/$name.jack | diff --strip-trailing-cr -q - src/test/resources/$name.xml > /dev/null; then
    echo "OK   parser $name"
  else
    echo "FAIL parser $name"
    fail=1
  fi
done
exit $fail