use crate::ast::{
  ExpressionStaticType, IRExpression, IRFunctionDefinition, IRMutableGlobalVariableDefinition,
  IRProgram, IRStatement, LiteralValue, SourceLanguageExpression, SourceLanguageFunctionDefinition,
  SourceLanguageMutableGlobalVariableDefinition, SourceLanguageProgram,
};
use crate::renamer::normalize_variable_names;

const DUMMY_ZERO: IRExpression = IRExpression::LiteralExpression(LiteralValue::IntLiteral(0));

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
      condition,
      e1,
      e2,
    } => {
      let (lowered_condition, lowered_condition_statements) = lower_expression(condition);
      let (_, s1) = lower_expression(e1);
      let (_, s2) = lower_expression(e2);

      let mut statements = lowered_condition_statements.clone();
      statements.push(Box::new(IRStatement::IfElseStatement {
        condition: lowered_condition,
        s1,
        s2,
      }));
      (
        Box::new(IRExpression::LiteralExpression(LiteralValue::IntLiteral(0))),
        statements,
      )
    }
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
      (Box::new(DUMMY_ZERO), mutable_statements)
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
      (Box::new(DUMMY_ZERO), lowered_statements)
    }
  }
}

pub fn lower_program(program: Box<SourceLanguageProgram>) -> Box<IRProgram> {
  let SourceLanguageProgram {
    global_variable_definitions,
    function_definitions,
  } = &*normalize_variable_names(program);

  let mut lowered_global_variable_definitions = Vec::new();
  for global_variable in global_variable_definitions {
    let SourceLanguageMutableGlobalVariableDefinition {
      line_number: _,
      identifier,
      assigned_value,
    } = &*global_variable;

    lowered_global_variable_definitions.push(IRMutableGlobalVariableDefinition {
      identifier: (*identifier).clone(),
      assigned_value: *assigned_value,
    })
  }

  let mut lowered_function_definitions = Vec::new();
  for function_definition in function_definitions {
    let SourceLanguageFunctionDefinition {
      line_number: _,
      identifier,
      function_arguments,
      return_type,
      body,
    } = &*function_definition;

    let function_argument_names: Vec<String> = (*function_arguments)
      .iter()
      .map(|(n, _)| (*n).clone())
      .collect();
    let (lowered_body, body_statements) = lower_expression((*body).clone());
    let body_final_expression = if *return_type == ExpressionStaticType::VoidType {
      None
    } else {
      Some(lowered_body)
    };

    lowered_function_definitions.push(IRFunctionDefinition {
      identifier: (*identifier).clone(),
      function_arguments: function_argument_names,
      body_statements,
      body_final_expression,
    });
  }

  Box::new(IRProgram {
    global_variable_definitions: lowered_global_variable_definitions,
    function_definitions: lowered_function_definitions,
  })
}
