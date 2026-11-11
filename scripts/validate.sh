#!/bin/bash
# Compila o projeto, compara os XML gerados para o Project 10 com os oficiais e os .vm gerados para o Project 11 com os do JackCompiler oficial.
set -e

javac -sourcepath src/main/java -d bin src/main/java/Main.java

for dir in src/test/resources/input/project10/*/; do
  program=$(basename "$dir")
  for jack in "$dir"*.jack; do
    name=$(basename "${jack%.jack}")
    java -cp bin Main --tokens "$jack" "output/lexer/$program/${name}T.xml" > /dev/null
    java -cp bin Main --xml "$jack" "output/parser/$program/$name.xml" > /dev/null
  done
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

for dir in src/test/resources/input/project10/*/; do
  program=$(basename "$dir")
  for jack in "$dir"*.jack; do
    check "lexer/$program/$(basename "${jack%.jack}")T.xml"
  done
done
for dir in src/test/resources/input/project10/*/; do
  program=$(basename "$dir")
  for jack in "$dir"*.jack; do
    check "parser/$program/$(basename "${jack%.jack}").xml"
  done
done
for dir in src/test/resources/input/project11/*/; do
  check_vm "$(basename "$dir")"
done
exit $fail