#!/bin/bash
# Compila o projeto, compara as saidas XML com as oficiais e os .vm do Project 11 com os gerados pelo JackCompiler oficial.
set -e

javac -sourcepath src/main/java -d bin src/main/java/Main.java

for name in Main Square SquareGame; do
  java -cp bin Main --tokens src/test/resources/input/$name.jack > /dev/null
  java -cp bin Main --xml src/test/resources/input/$name.jack > /dev/null
done

for dir in src/test/resources/input/project11/*/; do
  program=$(basename "$dir")
  rm -rf "output/vm/$program"
  mkdir -p "output/vm/$program"
  cp "$dir"*.jack "output/vm/$program/"
  java -cp bin Main "output/vm/$program" > /dev/null
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

check_vm() {
  local differ=0
  for jack in "output/vm/$1"/*.jack; do
    local name
    name=$(basename "${jack%.jack}")
    if ! diff --strip-trailing-cr -q "output/vm/$1/$name.vm" "src/test/resources/expected/vm/$1/$name.vm" > /dev/null 2>&1; then
      differ=1
    fi
  done
  if [ $differ -eq 0 ]; then
    echo "OK   vm/$1"
  else
    echo "FAIL vm/$1"
    fail=1
  fi
}

for name in Main Square SquareGame; do
  check "lexer/${name}T.xml"
done
for name in Main Square SquareGame; do
  check "parser/$name.xml"
done
for dir in src/test/resources/input/project11/*/; do
  check_vm "$(basename "$dir")"
done
exit $fail