use crate::ast::{
  SourceLanguageExpression, SourceLanguageFunctionDefinition,
  SourceLanguageMutableGlobalVariableDefinition, SourceLanguageProgram,
};
use crate::renamer::replace_variable_in_expression;
use std::collections::HashMap;

fn inline_function(
  expression: Box<SourceLanguageExpression>,
  function_to_inline: &SourceLanguageFunctionDefinition,
) -> Box<SourceLanguageExpression> {
  match *expression {
    SourceLanguageExpression::LiteralExpression {
      line_number,
      static_type,
      literal,
    } => Box::new(SourceLanguageExpression::LiteralExpression {
      line_number,
      static_type,
      literal,
    }),
    SourceLanguageExpression::VariableExpression {
      line_number,
      static_type,
      identifier,
    } => Box::new(SourceLanguageExpression::VariableExpression {
      line_number,
      static_type,
      identifier,
    }),
    SourceLanguageExpression::NotExpression {
      line_number,
      static_type,
      sub_expression,
    } => Box::new(SourceLanguageExpression::NotExpression {
      line_number,
      static_type,
      sub_expression: inline_function(sub_expression, function_to_inline),
    }),
    SourceLanguageExpression::FunctionCallExpression {
      line_number,
      static_type,
      function_name,
      function_arguments,
    } => {
      if function_name == function_to_inline.identifier {
        Box::new(SourceLanguageExpression::FunctionCallExpression {
          line_number,
          static_type,
          function_name,
          function_arguments,
        })
      } else {
        let mut replacement_map = HashMap::new();
        for (parameter, argument_expression) in function_to_inline
          .function_arguments
          .clone()
          .into_iter()
          .zip(function_arguments.into_iter())
        {
          let (name, _) = parameter;
          replacement_map.insert(name, argument_expression);
        }
        replace_variable_in_expression(function_to_inline.body.clone(), &replacement_map)
      }
    }
    SourceLanguageExpression::BinaryExpression {
      line_number,
      static_type,
      operator,
      e1,
      e2,
    } => Box::new(SourceLanguageExpression::BinaryExpression {
      line_number,
      static_type,
      operator,
      e1: inline_function(e1, function_to_inline),
      e2: inline_function(e2, function_to_inline),
    }),
    SourceLanguageExpression::IfElseExpression {
      line_number,
      static_type,
      condition,
      e1,
      e2,
    } => Box::new(SourceLanguageExpression::IfElseExpression {
      line_number,
      static_type,
      condition: inline_function(condition, function_to_inline),
      e1: inline_function(e1, function_to_inline),
      e2: inline_function(e2, function_to_inline),
    }),
    SourceLanguageExpression::AssignmentExpression {
      line_number,
      static_type,
      identifier,
      assigned_expression,
    } => Box::new(SourceLanguageExpression::AssignmentExpression {
      line_number,
      static_type,
      identifier,
      assigned_expression: inline_function(assigned_expression, function_to_inline),
    }),
    SourceLanguageExpression::ChainExpression {
      line_number,
      static_type,
      expressions,
    } => {
      let mut replaced_expressions = Vec::new();
      for sub_expression in expressions {
        replaced_expressions.push(inline_function(sub_expression, function_to_inline));
      }
      Box::new(SourceLanguageExpression::ChainExpression {
        line_number,
        static_type,
        expressions: replaced_expressions,
      })
    }
  }
}
