# Primitivize

![GitHub](https://img.shields.io/github/license/SamChou19815/primitivize.svg)

A programming language that can be easily lowered to a giant block of if-else expressions with some
variable declarations. It also includes a compiler that compiles the source code into Cornell CS2112
critter language.

## Simple Usage

```bash
cat path/to/program.txt | cargo run
```

You can see the grammar definition [here](./src/pl.lalrpop).

## Scope of this Project

This project aims to implement a type-checker and compiler/transpiler for this simple language. The
language is intentionally designed not to be Turing complete but to mimic as much features of Turing
complete languages as possible. The reason is that we want the compilation target to be a non-Turing
complete language.

The type-checker will be invoked before compilation to reject all ill-formed code before running.

The compiler will compile the source code of this language to an AST that is already lowered to
a set of variable declarations and a single unit expression. It will emit CS 2112 critter language
code that has this form.
