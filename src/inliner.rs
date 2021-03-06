use crate::ast::{
  ExpressionStaticType, FullyInlinedProgram, LiteralValue, SourceLanguageExpression,
  SourceLanguageFunctionDefinition, SourceLanguageProgram,
};
use crate::evaluator::compile_time_evaluation;
use crate::renamer::replace_variable_in_expression;
use crate::transformer::transform_to_if_else_blocks;
use std::collections::HashMap;

fn inline_function(
  expression: &SourceLanguageExpression,
  function_to_inline: &SourceLanguageFunctionDefinition,
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
    } => Box::new(SourceLanguageExpression::VariableExpression {
      line_number: *line_number,
      identifier: identifier.clone(),
    }),
    SourceLanguageExpression::FunctionCallExpression {
      line_number,
      static_type,
      function_name,
      function_arguments,
    } => {
      if *function_name != function_to_inline.identifier {
        Box::new(SourceLanguageExpression::FunctionCallExpression {
          line_number: *line_number,
          static_type: *static_type,
          function_name: (*function_name).clone(),
          function_arguments: function_arguments
            .iter()
            .map(|e| inline_function(e, function_to_inline))
            .collect(),
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
          replacement_map.insert(name, (*argument_expression).clone());
        }
        replace_variable_in_expression(&function_to_inline.body, &replacement_map)
      }
    }
    SourceLanguageExpression::BinaryExpression {
      line_number,
      operator,
      e1,
      e2,
    } => Box::new(SourceLanguageExpression::BinaryExpression {
      line_number: *line_number,
      operator: *operator,
      e1: inline_function(e1, function_to_inline),
      e2: inline_function(e2, function_to_inline),
    }),
    SourceLanguageExpression::IfElseExpression {
      line_number,
      condition,
      e1,
      e2,
    } => Box::new(SourceLanguageExpression::IfElseExpression {
      line_number: *line_number,
      condition: inline_function(condition, function_to_inline),
      e1: inline_function(e1, function_to_inline),
      e2: inline_function(e2, function_to_inline),
    }),
    SourceLanguageExpression::AssignmentExpression {
      line_number,
      identifier,
      assigned_expression,
    } => Box::new(SourceLanguageExpression::AssignmentExpression {
      line_number: *line_number,
      identifier: (*identifier).clone(),
      assigned_expression: inline_function(assigned_expression, function_to_inline),
    }),
    SourceLanguageExpression::ChainExpression {
      line_number,
      expressions,
    } => {
      let mut replaced_expressions = Vec::new();
      for sub_expression in expressions {
        replaced_expressions.push(inline_function(sub_expression, function_to_inline));
      }
      Box::new(SourceLanguageExpression::ChainExpression {
        line_number: *line_number,
        expressions: replaced_expressions,
      })
    }
  }
}

fn stub_function_call(
  expression: &SourceLanguageExpression,
  function_name_to_stub: &String,
  default_expression: &SourceLanguageExpression,
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
    } => Box::new(SourceLanguageExpression::VariableExpression {
      line_number: *line_number,
      identifier: identifier.clone(),
    }),
    SourceLanguageExpression::FunctionCallExpression {
      line_number,
      static_type,
      function_name,
      function_arguments,
    } => {
      if *function_name != *function_name_to_stub {
        Box::new(SourceLanguageExpression::FunctionCallExpression {
          line_number: *line_number,
          static_type: *static_type,
          function_name: function_name.clone(),
          function_arguments: function_arguments.clone(),
        })
      } else {
        Box::new((*default_expression).clone())
      }
    }
    SourceLanguageExpression::BinaryExpression {
      line_number,
      operator,
      e1,
      e2,
    } => Box::new(SourceLanguageExpression::BinaryExpression {
      line_number: *line_number,
      operator: *operator,
      e1: stub_function_call(e1, function_name_to_stub, default_expression),
      e2: stub_function_call(e2, function_name_to_stub, default_expression),
    }),
    SourceLanguageExpression::IfElseExpression {
      line_number,
      condition,
      e1,
      e2,
    } => Box::new(SourceLanguageExpression::IfElseExpression {
      line_number: *line_number,
      condition: stub_function_call(condition, function_name_to_stub, default_expression),
      e1: stub_function_call(e1, function_name_to_stub, default_expression),
      e2: stub_function_call(e2, function_name_to_stub, default_expression),
    }),
    SourceLanguageExpression::AssignmentExpression {
      line_number,
      identifier,
      assigned_expression,
    } => Box::new(SourceLanguageExpression::AssignmentExpression {
      line_number: *line_number,
      identifier: (*identifier).clone(),
      assigned_expression: stub_function_call(
        assigned_expression,
        function_name_to_stub,
        default_expression,
      ),
    }),
    SourceLanguageExpression::ChainExpression {
      line_number,
      expressions,
    } => {
      let mut replaced_expressions = Vec::new();
      for sub_expression in expressions {
        replaced_expressions.push(stub_function_call(
          sub_expression,
          function_name_to_stub,
          default_expression,
        ));
      }
      Box::new(SourceLanguageExpression::ChainExpression {
        line_number: *line_number,
        expressions: replaced_expressions,
      })
    }
  }
}

fn function_self_inline(
  function: &SourceLanguageFunctionDefinition,
  depth: usize,
) -> SourceLanguageFunctionDefinition {
  let mut body = function.body.clone();
  for _ in 0..depth {
    body = inline_function(&body, function);
  }
  let default_expression = match function.return_type {
    ExpressionStaticType::BoolType => SourceLanguageExpression::LiteralExpression {
      line_number: 0,
      literal: LiteralValue::BoolLiteral(false),
    },
    ExpressionStaticType::IntType => SourceLanguageExpression::LiteralExpression {
      line_number: 0,
      literal: LiteralValue::IntLiteral(0),
    },
    ExpressionStaticType::VoidType => SourceLanguageExpression::ChainExpression {
      line_number: 0,
      expressions: Vec::new(),
    },
  };
  body = stub_function_call(&body, &function.identifier, &default_expression);

  SourceLanguageFunctionDefinition {
    line_number: function.line_number,
    identifier: function.identifier.clone(),
    function_arguments: function.function_arguments.clone(),
    return_type: function.return_type,
    body: compile_time_evaluation(&body),
  }
}

pub fn program_inline(
  program: &SourceLanguageProgram,
  inline_depth: usize,
) -> Box<FullyInlinedProgram> {
  let functions = &program.function_definitions;
  let mut main_expression = functions[functions.len() - 1].body.clone();

  for i in (0..(functions.len() - 1)).rev() {
    main_expression = compile_time_evaluation(&inline_function(
      &main_expression,
      &function_self_inline(&functions[i], inline_depth),
    ));
  }

  Box::new(FullyInlinedProgram {
    global_variable_definitions: program.global_variable_definitions.clone(),
    if_else_blocks: transform_to_if_else_blocks(&main_expression),
  })
}
