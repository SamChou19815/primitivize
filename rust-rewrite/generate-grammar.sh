#/bin/bash

cp ../src/main/antlr/PL.g4 ../src/main/antlr/PLLexerPart.g4 .
java -jar antlr4-rust-generator.jar -Dlanguage=Rust -o src/parser -visitor -no-listener PL.g4
rm *.g4
