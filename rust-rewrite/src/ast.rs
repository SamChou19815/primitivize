use serde::Serialize;

#[derive(Debug, Clone, PartialEq, Serialize)]
pub enum LiteralValues {
  IntLiteral(i64),
  BoolLiteral(bool),
}

/** A classification of function's category based on their level of "predefined-ness". */
#[derive(Debug, Clone, PartialEq, Serialize)]
pub enum FunctionCategory {
  /** The functions provided by the user of this compiler. Like `Pervasive` in OCaml */
  Provided,
  /** Fhe functions defined by the user in the actual program. */
  UserDefined,
}

#[derive(Debug, Clone, PartialEq, Serialize)]
pub enum BinaryOperator {
  MUL,
  DIV,
  MOD,
  PLUS,
  MINUS,
  LT,
  LE,
  GT,
  GE,
  EQ,
  NE,
  AND,
  OR,
}

#[derive(Debug, Clone, PartialEq, Serialize)]
struct FunctionTypeInformation {
  argument_type: Vec<ExpressionType>,
  return_type: ExpressionType,
}

#[derive(Debug, Clone, PartialEq, Serialize)]
pub enum ExpressionType {
  VoidType,
  IntType,
  BoolType,
  FunctionType(Box<FunctionTypeInformation>),
}
