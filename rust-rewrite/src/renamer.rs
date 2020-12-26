use crate::ast::{
  ExpressionStaticType, SourceLanguageExpression, SourceLanguageFunctionDefinition,
  SourceLanguageProgram,
};
use std::collections::HashMap;

pub fn replace_variable_in_expression(
  expression: Box<SourceLanguageExpression>,
  expression_replacement_map: &HashMap<String, Box<SourceLanguageExpression>>,
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
    } => match (*expression_replacement_map).get(&identifier) {
      None => Box::new(SourceLanguageExpression::VariableExpression {
        line_number,
        static_type,
        identifier,
      }),
      Some(replacement) => (*replacement).clone(),
    },
    SourceLanguageExpression::NotExpression {
      line_number,
      static_type,
      sub_expression,
    } => Box::new(SourceLanguageExpression::NotExpression {
      line_number,
      static_type,
      sub_expression: replace_variable_in_expression(sub_expression, expression_replacement_map),
    }),
    SourceLanguageExpression::FunctionCallExpression {
      line_number,
      static_type,
      function_name,
      function_arguments,
    } => {
      let mut checked_function_arguments = Vec::new();
      for argument_expression in function_arguments {
        checked_function_arguments.push(replace_variable_in_expression(
          argument_expression,
          expression_replacement_map,
        ));
      }
      Box::new(SourceLanguageExpression::FunctionCallExpression {
        line_number,
        static_type,
        function_name,
        function_arguments: checked_function_arguments,
      })
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
      e1: replace_variable_in_expression(e1, expression_replacement_map),
      e2: replace_variable_in_expression(e2, expression_replacement_map),
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
      condition: replace_variable_in_expression(condition, expression_replacement_map),
      e1: replace_variable_in_expression(e1, expression_replacement_map),
      e2: replace_variable_in_expression(e2, expression_replacement_map),
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
      assigned_expression: replace_variable_in_expression(
        assigned_expression,
        expression_replacement_map,
      ),
    }),
    SourceLanguageExpression::ChainExpression {
      line_number,
      static_type,
      expressions,
    } => {
      let mut replaced_expressions = Vec::new();
      for sub_expression in expressions {
        replaced_expressions.push(replace_variable_in_expression(
          sub_expression,
          expression_replacement_map,
        ));
      }
      Box::new(SourceLanguageExpression::ChainExpression {
        line_number,
        static_type,
        expressions: replaced_expressions,
      })
    }
  }
}

pub fn prefix_variable_names(program: Box<SourceLanguageProgram>) -> Box<SourceLanguageProgram> {
  let SourceLanguageProgram {
    global_variable_definitions,
    function_definitions,
  } = &*program;

  let mut normalized_functions = Vec::new();

  for function_definition in function_definitions {
    let SourceLanguageFunctionDefinition {
      line_number,
      identifier,
      function_arguments,
      return_type,
      body,
    } = &*function_definition;

    let mut expression_replacement_map = HashMap::new();
    for (name, _) in function_arguments {
      expression_replacement_map.insert(
        (*name).clone(),
        Box::new(SourceLanguageExpression::VariableExpression {
          line_number: *line_number,
          static_type: ExpressionStaticType::VoidType,
          identifier: format!("{:}__{:}", identifier, name),
        }),
      );
    }

    normalized_functions.push(SourceLanguageFunctionDefinition {
      line_number: *line_number,
      identifier: (*identifier).clone(),
      function_arguments: (*function_arguments).clone(),
      return_type: *return_type,
      body: replace_variable_in_expression((*body).clone(), &expression_replacement_map),
    })
  }

  Box::new(SourceLanguageProgram {
    global_variable_definitions: (*global_variable_definitions).clone(),
    function_definitions: normalized_functions,
  })
}
