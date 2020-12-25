use crate::ast::{
  IRExpression, IRFunctionDefinition, IRMutableGlobalVariableDefinition, IRProgram, IRStatement,
  LiteralValue, SourceLanguageExpression, SourceLanguageFunctionDefinition,
  SourceLanguageMutableGlobalVariableDefinition, SourceLanguageProgram,
};

fn lower_expression(
  expression: Box<SourceLanguageExpression>,
) -> (Box<IRExpression>, Vec<Box<IRStatement>>) {
  match *expression {
    SourceLanguageExpression::LiteralExpression {
      line_number: _,
      static_type: _,
      literal,
    } => (Box::new(IRExpression::LiteralExpression(literal)), vec![]),
    SourceLanguageExpression::VariableExpression {
      line_number: _,
      static_type: _,
      identifier,
    } => (
      Box::new(IRExpression::VariableExpression(identifier)),
      vec![],
    ),
    SourceLanguageExpression::NotExpression {
      line_number: _,
      static_type: _,
      sub_expression,
    } => {
      let (lowered_sub_expression, statements) = lower_expression(sub_expression);
      (
        Box::new(IRExpression::NotExpression(lowered_sub_expression)),
        statements,
      )
    }
    SourceLanguageExpression::FunctionCallExpression {
      line_number: _,
      static_type: _,
      function_name,
      function_arguments,
    } => {
      let mut lowered_function_arguments = Vec::new();
      let mut lowered_statements = Vec::new();
      for function_argument in function_arguments {
        let (lowered_argument_expression, lowered_argument_statements) =
          lower_expression(function_argument);
        lowered_function_arguments.push(lowered_argument_expression);
        lowered_statements.append(&mut lowered_argument_statements.clone());
      }
      (
        Box::new(IRExpression::FunctionCallExpression {
          function_name,
          function_arguments: lowered_function_arguments,
        }),
        lowered_statements,
      )
    }
    SourceLanguageExpression::BinaryExpression {
      line_number: _,
      static_type: _,
      operator,
      e1,
      e2,
    } => {
      let (lowered_e1, lowered_e1_statements) = lower_expression(e1);
      let (lowered_e2, lowered_e2_statements) = lower_expression(e2);
      let mut lowered_statements = Vec::new();
      lowered_statements.append(&mut lowered_e1_statements.clone());
      lowered_statements.append(&mut lowered_e2_statements.clone());
      (
        Box::new(IRExpression::BinaryExpression {
          operator,
          e1: lowered_e1,
          e2: lowered_e2,
        }),
        lowered_statements,
      )
    }
    SourceLanguageExpression::IfElseExpression {
      line_number: _,
      static_type: _,
      condition: _,
      e1: _,
      e2: _,
    } => (
      // TODO if else
      Box::new(IRExpression::LiteralExpression(LiteralValue::BoolLiteral(
        false,
      ))),
      vec![],
    ),
    SourceLanguageExpression::AssignmentExpression {
      line_number: _,
      static_type: _,
      identifier,
      assigned_expression,
    } => {
      let (lowered_assigned_expression, statements) = lower_expression(assigned_expression);
      let mut mutable_statements = statements.clone();
      mutable_statements.push(Box::new(IRStatement::AssignmentStatement {
        identifier,
        assigned_expression: lowered_assigned_expression,
      }));
      (
        Box::new(IRExpression::LiteralExpression(LiteralValue::IntLiteral(0))),
        mutable_statements,
      )
    }
    SourceLanguageExpression::ChainExpression {
      line_number: _,
      static_type: _,
      expressions,
    } => {
      let mut lowered_statements = Vec::new();
      for sub_expression in expressions {
        let (_, lowered_sub_expression_statements) = lower_expression(sub_expression);
        lowered_statements.append(&mut lowered_sub_expression_statements.clone());
      }
      (
        Box::new(IRExpression::LiteralExpression(LiteralValue::IntLiteral(0))),
        lowered_statements,
      )
    }
  }
}

/*
fn lower_program(program: Box<SourceLanguageProgram>) -> Box<IRProgram> {
  let SourceLanguageProgram {
    global_variable_definitions,
    function_definitions,
  } = &*program;

  for global_variable in global_variable_definitions {
    let SourceLanguageMutableGlobalVariableDefinition {
      line_number: _,
      identifier,
      assigned_expression,
    } = &*global_variable;


  }
}
*/
