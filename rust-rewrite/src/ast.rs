use serde::Serialize;

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
pub enum LiteralValue {
  IntLiteral(i32),
  BoolLiteral(bool),
}

/** A classification of function's category based on their level of "predefined-ness". */
#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
pub enum FunctionCategory {
  /** The functions provided by the user of this compiler. Like `Pervasive` in OCaml */
  Provided,
  /** Fhe functions defined by the user in the actual program. */
  UserDefined,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
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

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
pub enum ExpressionStaticType {
  VoidType,
  IntType,
  BoolType,
  FunctionType {
    argument_types: Vec<ExpressionStaticType>,
    return_type: Box<ExpressionStaticType>,
  },
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
pub struct Position {
  line_number: i32,
  column_number: i32,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
pub struct Range {
  start: Position,
  end: Position,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
pub enum SourceLanguageExpression {
  LiteralExpression {
    range: Range,
    static_type: ExpressionStaticType,
    literal: LiteralValue,
  },
  VariableExpression {
    range: Range,
    static_type: ExpressionStaticType,
    identifier: String,
  },
  NotExpression {
    range: Range,
    static_type: ExpressionStaticType,
    sub_expression: Box<SourceLanguageExpression>,
  },
  FunctionCallExpression {
    range: Range,
    static_type: ExpressionStaticType,
    function_name: String,
    function_arguments: Vec<SourceLanguageExpression>,
  },
  BinaryExpression {
    range: Range,
    static_type: ExpressionStaticType,
    operator: BinaryOperator,
    e1: Box<SourceLanguageExpression>,
    e2: Box<SourceLanguageExpression>,
  },
  IfElseExpression {
    range: Range,
    static_type: ExpressionStaticType,
    condition: Box<SourceLanguageExpression>,
    e1: Box<SourceLanguageExpression>,
    e2: Box<SourceLanguageExpression>,
  },
  AssignmentExpression {
    range: Range,
    static_type: ExpressionStaticType,
    identifier: String,
    assigned_expression: Box<SourceLanguageExpression>,
  },
  ChainExpression {
    range: Range,
    static_type: ExpressionStaticType,
    e1: Box<SourceLanguageExpression>,
    e2: Box<SourceLanguageExpression>,
  },
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
pub struct SourceLanguageMutableGlobalVariableDefinition {
  range: Range,
  static_type: ExpressionStaticType,
  identifier: String,
  assigned_expression: SourceLanguageExpression,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
pub struct SourceLanguageFunctionDefinition {
  range: Range,
  identifier: String,
  identifier_range: Range,
  function_arguments: Vec<(String, ExpressionStaticType)>,
  return_type: ExpressionStaticType,
  body: SourceLanguageExpression,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
pub struct SourceLanguageProgram {
  global_variable_definitions: Vec<SourceLanguageMutableGlobalVariableDefinition>,
  function_definitions: Vec<SourceLanguageFunctionDefinition>,
}
