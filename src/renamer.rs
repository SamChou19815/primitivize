use crate::ast::SourceLanguageExpression;
use std::collections::HashMap;

pub fn replace_variable_in_expression(
  expression: &SourceLanguageExpression,
  expression_replacement_map: &HashMap<String, Box<SourceLanguageExpression>>,
) -> Box<SourceLanguageExpression> {
  match &expression {
    SourceLanguageExpression::LiteralExpression {
      line_number,
      literal,
    } => Box::new(SourceLanguageExpression::LiteralExpression {
      line_number: *line_number,
      literal: *literal,
    }),
    SourceLanguageExpression::VariableExpression {
      line_number,
      identifier,
    } => match (*expression_replacement_map).get(identifier) {
      None => Box::new(SourceLanguageExpression::VariableExpression {
        line_number: *line_number,
        identifier: (*identifier).clone(),
      }),
      Some(replacement) => (*replacement).clone(),
    },
    SourceLanguageExpression::FunctionCallExpression {
      line_number,
      static_type,
      function_name,
      function_arguments,
    } => {
      let mut checked_function_arguments = Vec::new();
      for argument_expression in function_arguments {
        checked_function_arguments.push(replace_variable_in_expression(
          &argument_expression,
          expression_replacement_map,
        ));
      }
      Box::new(SourceLanguageExpression::FunctionCallExpression {
        line_number: *line_number,
        static_type: *static_type,
        function_name: (*function_name).clone(),
        function_arguments: checked_function_arguments,
      })
    }
    SourceLanguageExpression::BinaryExpression {
      line_number,
      operator,
      e1,
      e2,
    } => Box::new(SourceLanguageExpression::BinaryExpression {
      line_number: *line_number,
      operator: *operator,
      e1: replace_variable_in_expression(&e1, expression_replacement_map),
      e2: replace_variable_in_expression(&e2, expression_replacement_map),
    }),
    SourceLanguageExpression::IfElseExpression {
      line_number,
      condition,
      e1,
      e2,
    } => Box::new(SourceLanguageExpression::IfElseExpression {
      line_number: *line_number,
      condition: replace_variable_in_expression(&condition, expression_replacement_map),
      e1: replace_variable_in_expression(&e1, expression_replacement_map),
      e2: replace_variable_in_expression(&e2, expression_replacement_map),
    }),
    SourceLanguageExpression::AssignmentExpression {
      line_number,
      identifier,
      assigned_expression,
    } => Box::new(SourceLanguageExpression::AssignmentExpression {
      line_number: *line_number,
      identifier: (*identifier).clone(),
      assigned_expression: replace_variable_in_expression(
        &assigned_expression,
        expression_replacement_map,
      ),
    }),
    SourceLanguageExpression::ChainExpression {
      line_number,
      expressions,
    } => {
      let mut replaced_expressions = Vec::new();
      for sub_expression in expressions {
        replaced_expressions.push(replace_variable_in_expression(
          &sub_expression,
          expression_replacement_map,
        ));
      }
      Box::new(SourceLanguageExpression::ChainExpression {
        line_number: *line_number,
        expressions: replaced_expressions,
      })
    }
  }
}
