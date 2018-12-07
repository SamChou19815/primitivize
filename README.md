# Primitivize

[![Build Status](https://travis-ci.com/SamChou19815/primitivize.svg?branch=master)](https://travis-ci.com/SamChou19815/primitivize)
[![Release](https://jitpack.io/v/SamChou19815/primitivize.svg)](https://jitpack.io/#SamChou19815/primitivize)
![GitHub](https://img.shields.io/github/license/SamChou19815/primitivize.svg)

A programming language that can be easily lowered to a giant block of a single expression with some
variable declarations. The lowered AST is designed to be easily transpiled to other low level
programming languages.

The language is NOT Turing complete, but it can be used to simulate one step of Turing machine
state transition.

# Simple Usage

If you don't want to do deep into the details, I also prepared some examples and CLI support.

Currently, the CLI can compile this programming language into its lower level primitive and also
compile this language into critterlang. For the second option, here is an example

```bash
./gradlew build # you first compile it with gradle
java -jar build/libs/primitivize-0.1-all.jar -critter-compile < src/test/resources/com/developersam/primitivize/integration/critter-program.txt
# need more help? the following line will just print the help message.
java -jar build/libs/primitivize-0.1-all.jar
```

You can see the grammar definition [here](./src/main/antlr/PL.g4).

## Gradle Config

Add this to your `build.gradle` to use the artifact.

```groovy
repositories {
    jcenter()
    maven { url "https://jitpack.io" }
}
dependencies {
    implementation 'com.github.SamChou19815:primitivize:+'
}
```

## Getting Started

```kotlin
// Kotlin
fun main(args: Array<String>) {
  val code = """
  var veryLongVar = 23 * 3
  var isGoodRecorder = veryLongVar // 1 ==> good, else ==> bad
  var a = 1
  var b = 2
  
  fun reassign(d: int): void = veryLongVar = d
  fun constant(): int = 4 * 3 + 2
  fun foo(): void = veryLongVar = constant()
  fun isGood(): bool = isGoodRecorder == 1
  
  recursive(3, 0) fun testRecursion(): int = 1 + testRecursion()
  
  fun main(): void =
    if isGood() then (
      reassign(3 + 2);
      isGoodRecorder = testRecursion()
    ) else if smell() < energy() + nearby(3 + 2) then (
      foo();
      waitFor()
    ) else forward()
  """
  // replace it with your code.
  val lib: RuntimeLibrary? = CritterLangRuntime // Supply with your own library, or keep it null.
  val p: ProcessedProgram = Primitivizer.primitivize(code, lib)
  // You can further process this program.
  // The visitor pattern is implemented for you in AstToCodeConverter and CodeConvertible.
  // The PrettyPrinter in package codegen is a good example to look at.
}
```

The code mentioned above will be compiled to an AST that is equivalent to:

```
var var0 =
  23 * 3
var var1 =
  var0
var var2 =
  1
var var3 =
  2
// Main Expression:
if var1 == 1 && true then (
  var0 = 5
  ;
  var1 = 4
) else if smell() < energy() + nearby(3 + 2) && true then (
  var0 = 14
  ;
  waitFor()
) else (
  forward()
)
```

You can see that variables are renamed by numbers so you can easily process them to some lower-level
stuff. Some constants value computation are determined at compile time to optimize the programs.
Functions, except those defined in the runtime library, are all aggressively inlined. It also
has some primitive support for recursion.

The function header `recursive(3, 0)` means that the compiler will expand the recursive call with
depth at most 3, after that it will use the default value 0. Note that full recursion support is
not possible without change the scope of the project.

## Scope of this Project

This project aims to implement a type-checker and compiler/transpiler for this simple language. The
language is intentionally designed not to be Turing complete but to mimic as much features of Turing
complete languages as possible. The reason is that we want the compilation target to be a non-Turing
complete language.

The type-checker will be invoked before compilation to reject all ill-formed code before running.

The compiler will compile the source code of this language to an AST that is already lowered to
a set of variable declarations and a single unit expression. The user of this library can take the
primitive AST and translate it into more primitive languages.

## Documentations

Read the [language spec](./LANGUAGE_SPEC.md) for the definition of the language features.
*Currently, the language is not very precisely defined, but it mostly conforms to your intuition.*
