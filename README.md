# Primitivize

A programming language that can be easily lowered to a giant block of a single expression with some
variable declarations. The lowered AST is designed to be easily transpiled to other low level
programming languages.

The language is NOT Turing complete, but it can be used to simulate one step of Turing machine
state transition.

## Scope of this Project

This project aims to implement a type-checker and compiler/transpiler for this simple language.

The type-checker will be invoked before compilation to reject all ill-formed code before running.

The compiler will compile the source code of this language to an AST that is already lowered to
a set of variable declarations and a single unit expression. The user of this library can take the
primitive AST and translate it into more primitive languages.

## Getting Started

```java
// Java
public final class GettingStarted {
    public static void main(String... args) {
        String code = "var a = 1\nvar b = 2\nfun main(): void = a = 2; b = 1";
        // replace it with your code.
        RuntimeLibrary lib = null; // Supply with your own library, or keep it null.
        ProcessedProgram p = Primitivizer.primitivize(code, lib);
        // You can further process this program.
        // The visitor pattern is implemented for you in AstToCodeConverter and CodeConvertible.
        // The PrettyPrinter in package codegen is a good example to look at.
    }
}
```

```kotlin
// Kotlin
fun main(args: Array<String>) {
  val code = """
  var a = 1
  var b = 2
  fun main(): void = a = 2; b = 1
  """
  // replace it with your code.
  val lib: RuntimeLibrary? = null // Supply with your own library, or keep it null.
  val p: ProcessedProgram = Primitivizer.primitivize(code, lib)
  // You can further process this program.
  // The visitor pattern is implemented for you in AstToCodeConverter and CodeConvertible.
  // The PrettyPrinter in package codegen is a good example to look at.
}
```

The code mentioned above will be compiled to an AST that is equivalent to:

```
var var0 = 1
var var1 = 2
// Main Expression:
var0 = 2; var1 = 1
```

You can see that variables are renamed by numbers so you can easily process them to some lower-level
stuff.

## Documentations

Read the [language spec](./LANGUAGE_SPEC.md) for the definition of the language features.
*Currently, the language is not very precisely defined, but it mostly conforms to your intuition.*
