#!/bin/bash
# Compila o projeto, compara os XML gerados para o Project 10 com os oficiais e os .vm gerados para o Project 11 com os do JackCompiler oficial.
set -e

javac -sourcepath src/main/java -d bin src/main/java/Main.java

for dir in src/test/resources/input/project10/*/; do
  program=$(basename "$dir")
  rm -rf "output/xml/$program"
  mkdir -p "output/xml/$program"
  cp "$dir"*.jack "output/xml/$program/"
  java -cp bin Main --tokens "output/xml/$program" > /dev/null
  java -cp bin Main --xml "output/xml/$program" > /dev/null
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
  if diff --strip-trailing-cr -q "$1" "src/test/resources/expected/$2" > /dev/null; then
    echo "OK   $2"
  else
    echo "FAIL $2"
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
    name=$(basename "${jack%.jack}")
    check "output/xml/$program/${name}T.xml" "lexer/$program/${name}T.xml"
  done
done
for dir in src/test/resources/input/project10/*/; do
  program=$(basename "$dir")
  for jack in "$dir"*.jack; do
    name=$(basename "${jack%.jack}")
    check "output/xml/$program/${name}P.xml" "parser/$program/$name.xml"
  done
done
for dir in src/test/resources/input/project11/*/; do
  check_vm "$(basename "$dir")"
done
exit $fail