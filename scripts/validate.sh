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
same() {
  diff --strip-trailing-cr -q "$1" "$2" > /dev/null 2>&1
}

report() {
  if [ "$2" -eq 0 ]; then
    echo "OK   $1"
  else
    echo "FAIL $1"
    fail=1
  fi
}

for dir in src/test/resources/input/project10/*/; do
  program=$(basename "$dir")
  differ=0
  for jack in "$dir"*.jack; do
    name=$(basename "${jack%.jack}")
    same "output/xml/$program/${name}T.xml" "src/test/resources/expected/lexer/$program/${name}T.xml" || differ=1
  done
  report "lexer/$program" $differ
done
echo
for dir in src/test/resources/input/project10/*/; do
  program=$(basename "$dir")
  differ=0
  for jack in "$dir"*.jack; do
    name=$(basename "${jack%.jack}")
    same "output/xml/$program/${name}P.xml" "src/test/resources/expected/parser/$program/$name.xml" || differ=1
  done
  report "parser/$program" $differ
done
echo
for dir in src/test/resources/input/project11/*/; do
  program=$(basename "$dir")
  differ=0
  for jack in "$dir"*.jack; do
    name=$(basename "${jack%.jack}")
    same "output/vm/$program/$name.vm" "src/test/resources/expected/vm/$program/$name.vm" || differ=1
  done
  report "vm/$program" $differ
done
exit $fail