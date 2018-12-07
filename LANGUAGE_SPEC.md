# Language Specification

## Grammar Specification

You can read the spec for grammar by reading the code at

- [PL.g4](./src/main/antlr/PL.g4)
- [PLLexerPart.g4](./src/main/antlr/PLLexerPart.g4)

## Runtime Specification

The language has 3 predefined types: `int`, `bool` and `void`. Integers and booleans have their
corresponding literals, like `42`, `true`, while basic `void` expression can be only obtained by 
assigning variables or calling `void` runtime functions.

The language also has function types, although you cannot use them effectively in a functional way.
They exist simply for type checking purposes. It's simply not well supported right now.

You can dynamically inject a runtime by implementing the marker interface `RuntimeLibrary`, and
add static methods annotated with `RuntimeFunction` to be included in the runtime. Note that the
only allowed types are `int`, `boolean`, and `void` in those functions.

## Namespace Specification

Lexical scope is used for type checking. Functions and variables defined below has those members
defined above in scope.

Name shadowing is bad and is not allowed.

Only constant depth recursion is supported, so that we can always inline.

## Code Structure Specification

The program is a simple file that contains a list of `int` global variable declaration, a list of 
normal function declaration and a final main void function.

The main function is the expression that will be called.

Expressions and definitions have slightly different type checking and evaluation rules.

## Type Checking Specification

All parameters in a function must be annotated by their types. 

### Variable 

Syntax: `var identifier = expr`

If `expr` has type `int`, then the environment `e` will gain an additional mapping from `identifier`
to `int`.

### Function

Syntax: `[recursive(num, defaultExpr)]? fun identifier(a1: T1, a2: T2, ...): RT = expr`.

The function has type `(T1, T2, ...) -> RT`. The `<expr>` must also have type `RT`. The mapping 
`a1 -> T1, a2 -> T2, ...` will be added to the environment when type checking `expr`. If the
header `[recursive(num, defaultExpr)]?` is specified, then `defaultExpr` must have type `RT`.

### Expressions

#### Literal

e.g. `42`, `true`, `false`

Type is simply the type of the literal, which can be one of `int`, `bool`.

#### Variable

Type is the type of the variable in the environment.

#### Not Expression

Syntax: `!expr`.

`expr` must have type `bool` and this expression has type `bool`.

#### Function Application

Syntax: `funIdentifier (arg1, arg2, ...)`.

`funExpr` must be a identifier for a function, whose number of available arguments must be equal to 
the given arguments. The type of the expression is the return type of the function.

#### Binary Expression

Syntax: `a op b`.

`a` and `b` must have the same type and `op` is a legal binary operator.

- If `op` is `*`, `/`, `%`, `+`, or `-`, `a` and `b` must both have type `int`, and the expression 
- If `op` is `<`, `<=`, `>`, `>=`, `==`, or `!=`, `a` and `b` must both be `int`, and the expression
has type `bool`.
- If `op` is `&&` or `||`, `a` and `b` must have type `bool` and the expression has type `bool`.

#### If Expression

Syntax: `if c then e1 else e2`.

`c` must have type `bool`. `e1` and `e2` must have the same type, and the expression's type is the
type of `e1`/`e2`.

#### Assign Expression

Syntax: `a = expr`.

If the variable `a` in the environment has the same type with `expr`, this expression has type 
`void`.

#### Chain Expression

Syntax: `e1; e2`

If the expressions `e1` and `e2` both have type `void`, this expression has type `void`.
