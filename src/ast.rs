use serde::Serialize;

#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash, Serialize)]
pub enum LiteralValue {
  IntLiteral(i32),
  BoolLiteral(bool),
}

#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash, Serialize)]
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

#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash, Serialize)]
pub enum ExpressionStaticType {
  VoidType,
  IntType,
  BoolType,
}

pub fn pretty_print_expression_static_type(t: ExpressionStaticType) -> String {
  match t {
    ExpressionStaticType::VoidType => "void".to_string(),
    ExpressionStaticType::IntType => "int".to_string(),
    ExpressionStaticType::BoolType => "bool".to_string(),
  }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
pub struct FunctionType {
  pub argument_types: Vec<ExpressionStaticType>,
  pub return_type: ExpressionStaticType,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
pub enum SourceLanguageExpression {
  LiteralExpression {
    line_number: usize,
    literal: LiteralValue,
  },
  VariableExpression {
    line_number: usize,
    identifier: String,
  },
  FunctionCallExpression {
    line_number: usize,
    static_type: ExpressionStaticType,
    function_name: String,
    function_arguments: Vec<Box<SourceLanguageExpression>>,
  },
  BinaryExpression {
    line_number: usize,
    operator: BinaryOperator,
    e1: Box<SourceLanguageExpression>,
    e2: Box<SourceLanguageExpression>,
  },
  IfElseExpression {
    line_number: usize,
    condition: Box<SourceLanguageExpression>,
    e1: Box<SourceLanguageExpression>,
    e2: Box<SourceLanguageExpression>,
  },
  AssignmentExpression {
    line_number: usize,
    identifier: String,
    assigned_expression: Box<SourceLanguageExpression>,
  },
  ChainExpression {
    line_number: usize,
    expressions: Vec<Box<SourceLanguageExpression>>,
  },
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
pub struct SourceLanguageMutableGlobalVariableDefinition {
  pub line_number: usize,
  pub identifier: String,
  pub assigned_value: i32,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
pub struct SourceLanguageFunctionDefinition {
  pub line_number: usize,
  pub identifier: String,
  pub function_arguments: Vec<(String, ExpressionStaticType)>,
  pub return_type: ExpressionStaticType,
  pub body: Box<SourceLanguageExpression>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
pub struct SourceLanguageProgram {
  pub global_variable_definitions: Vec<SourceLanguageMutableGlobalVariableDefinition>,
  pub function_definitions: Vec<SourceLanguageFunctionDefinition>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
pub struct IfElseBlock {
  pub condition: SourceLanguageExpression,
  pub action: SourceLanguageExpression,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize)]
pub struct FullyInlinedProgram {
  pub global_variable_definitions: Vec<SourceLanguageMutableGlobalVariableDefinition>,
  pub if_else_blocks: Vec<IfElseBlock>,
}
