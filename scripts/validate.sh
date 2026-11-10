#!/bin/bash
# Compila o projeto, compara as saidas XML com as oficiais e confere que os programas do Project 11 geram .vm.
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
  for jack in "output/vm/$program"/*.jack; do
    java -cp bin Main "$jack" > /dev/null
  done
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
  local missing=0
  for jack in "output/vm/$1"/*.jack; do
    if [ ! -s "${jack%.jack}.vm" ]; then
      missing=1
    fi
  done
  if [ $missing -eq 0 ]; then
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