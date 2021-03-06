use crate::ast::{
  pretty_print_expression_static_type, BinaryOperator, ExpressionStaticType, FunctionType,
  LiteralValue, SourceLanguageExpression, SourceLanguageFunctionDefinition,
  SourceLanguageMutableGlobalVariableDefinition, SourceLanguageProgram,
};
use crate::pl::SourceLanguageProgramParser;
use im::{HashMap, HashSet};

fn check_type(
  line_number: usize,
  type_errors: &mut Vec<String>,
  expected_type: ExpressionStaticType,
  actual_type: ExpressionStaticType,
) -> ExpressionStaticType {
  if expected_type != actual_type {
    (*type_errors).push(format!(
      "Line {:}: Expected type `{:}`, actual type `{:}`.",
      line_number,
      pretty_print_expression_static_type(expected_type),
      pretty_print_expression_static_type(actual_type)
    ));
  }
  actual_type
}

fn type_check_expression(
  functions_environment: &HashMap<String, FunctionType>,
  readable_values_environment: &HashMap<String, ExpressionStaticType>,
  global_values_environment: &HashSet<String>,
  expected_type: ExpressionStaticType,
  type_errors: &mut Vec<String>,
  expression: &SourceLanguageExpression,
) -> Box<SourceLanguageExpression> {
  match &expression {
    &SourceLanguageExpression::LiteralExpression {
      line_number,
      literal: LiteralValue::IntLiteral(i),
    } => {
      check_type(
        *line_number,
        type_errors,
        expected_type,
        ExpressionStaticType::IntType,
      );
      Box::new(SourceLanguageExpression::LiteralExpression {
        line_number: *line_number,
        literal: LiteralValue::IntLiteral(*i),
      })
    }
    &SourceLanguageExpression::LiteralExpression {
      line_number,
      literal: LiteralValue::BoolLiteral(b),
    } => {
      check_type(
        *line_number,
        type_errors,
        expected_type,
        ExpressionStaticType::BoolType,
      );
      Box::new(SourceLanguageExpression::LiteralExpression {
        line_number: *line_number,
        literal: LiteralValue::BoolLiteral(*b),
      })
    }
    &SourceLanguageExpression::VariableExpression {
      line_number,
      identifier,
    } => {
      match (*readable_values_environment).get(identifier) {
        Some(&actual_type) => {
          check_type(*line_number, type_errors, expected_type, actual_type);
        }
        None => {
          type_errors.push(format!(
            "Line {:}: Undefined variable `{:}`.",
            line_number, identifier
          ));
        }
      };
      Box::new(SourceLanguageExpression::VariableExpression {
        line_number: *line_number,
        identifier: (*identifier).clone(),
      })
    }
    &SourceLanguageExpression::FunctionCallExpression {
      line_number,
      static_type: _,
      function_name,
      function_arguments,
    } => {
      match (*functions_environment).get(function_name) {
        None => {
          type_errors.push(format!(
            "Line {:}: Undefined function `{:}`.",
            line_number, function_name
          ));
          Box::new(SourceLanguageExpression::FunctionCallExpression {
            line_number: *line_number,
            static_type: ExpressionStaticType::VoidType,
            function_name: (*function_name).clone(),
            function_arguments: (*function_arguments).clone(),
          })
        }
        Some(function_type) => {
          let FunctionType {
            argument_types,
            return_type,
          } = &*function_type;
          // Check return type
          check_type(*line_number, type_errors, expected_type, *return_type);
          // Check argument types
          let expected_argument_length = argument_types.len();
          let actual_argument_length = function_arguments.len();
          if expected_argument_length != actual_argument_length {
            type_errors.push(format!(
              "Line {:}: Expected argument length `{:}`, actual {:}.",
              line_number, expected_argument_length, actual_argument_length
            ));
          }
          let mut checked_function_arguments = Vec::new();
          for (argument_expression, argument_type) in function_arguments
            .into_iter()
            .zip(argument_types.into_iter())
          {
            checked_function_arguments.push(type_check_expression(
              functions_environment,
              readable_values_environment,
              global_values_environment,
              *argument_type,
              type_errors,
              &*argument_expression,
            ));
          }
          Box::new(SourceLanguageExpression::FunctionCallExpression {
            line_number: *line_number,
            static_type: *return_type,
            function_name: (*function_name).clone(),
            function_arguments: checked_function_arguments,
          })
        }
      }
    }
    &SourceLanguageExpression::BinaryExpression {
      line_number,
      operator,
      e1,
      e2,
    } => match operator {
      BinaryOperator::MUL
      | BinaryOperator::DIV
      | BinaryOperator::MOD
      | BinaryOperator::PLUS
      | BinaryOperator::MINUS => {
        check_type(
          *line_number,
          type_errors,
          expected_type,
          ExpressionStaticType::IntType,
        );
        Box::new(SourceLanguageExpression::BinaryExpression {
          line_number: *line_number,
          operator: *operator,
          e1: type_check_expression(
            functions_environment,
            readable_values_environment,
            global_values_environment,
            ExpressionStaticType::IntType,
            type_errors,
            &*e1,
          ),
          e2: type_check_expression(
            functions_environment,
            readable_values_environment,
            global_values_environment,
            ExpressionStaticType::IntType,
            type_errors,
            &*e2,
          ),
        })
      }
      BinaryOperator::LT | BinaryOperator::LE | BinaryOperator::GT | BinaryOperator::GE => {
        check_type(
          *line_number,
          type_errors,
          expected_type,
          ExpressionStaticType::BoolType,
        );
        Box::new(SourceLanguageExpression::BinaryExpression {
          line_number: *line_number,
          operator: *operator,
          e1: type_check_expression(
            functions_environment,
            readable_values_environment,
            global_values_environment,
            ExpressionStaticType::IntType,
            type_errors,
            &*e1,
          ),
          e2: type_check_expression(
            functions_environment,
            readable_values_environment,
            global_values_environment,
            ExpressionStaticType::IntType,
            type_errors,
            &*e2,
          ),
        })
      }
      BinaryOperator::AND | BinaryOperator::OR => {
        check_type(
          *line_number,
          type_errors,
          expected_type,
          ExpressionStaticType::BoolType,
        );
        Box::new(SourceLanguageExpression::BinaryExpression {
          line_number: *line_number,
          operator: *operator,
          e1: type_check_expression(
            functions_environment,
            readable_values_environment,
            global_values_environment,
            ExpressionStaticType::BoolType,
            type_errors,
            &*e1,
          ),
          e2: type_check_expression(
            functions_environment,
            readable_values_environment,
            global_values_environment,
            ExpressionStaticType::BoolType,
            type_errors,
            &*e2,
          ),
        })
      }
      BinaryOperator::EQ | BinaryOperator::NE => {
        check_type(
          *line_number,
          type_errors,
          expected_type,
          ExpressionStaticType::BoolType,
        );
        Box::new(SourceLanguageExpression::BinaryExpression {
          line_number: *line_number,
          operator: *operator,
          e1: type_check_expression(
            functions_environment,
            readable_values_environment,
            global_values_environment,
            ExpressionStaticType::IntType,
            type_errors,
            &*e1,
          ),
          e2: type_check_expression(
            functions_environment,
            readable_values_environment,
            global_values_environment,
            ExpressionStaticType::IntType,
            type_errors,
            &*e2,
          ),
        })
      }
    },
    &SourceLanguageExpression::IfElseExpression {
      line_number,
      condition,
      e1,
      e2,
    } => Box::new(SourceLanguageExpression::IfElseExpression {
      line_number: *line_number,
      condition: type_check_expression(
        functions_environment,
        readable_values_environment,
        global_values_environment,
        ExpressionStaticType::BoolType,
        type_errors,
        &*condition,
      ),
      e1: type_check_expression(
        functions_environment,
        readable_values_environment,
        global_values_environment,
        expected_type,
        type_errors,
        &*e1,
      ),
      e2: type_check_expression(
        functions_environment,
        readable_values_environment,
        global_values_environment,
        expected_type,
        type_errors,
        &*e2,
      ),
    }),
    &SourceLanguageExpression::AssignmentExpression {
      line_number,
      identifier,
      assigned_expression,
    } => {
      check_type(
        *line_number,
        type_errors,
        expected_type,
        ExpressionStaticType::VoidType,
      );
      Box::new(SourceLanguageExpression::AssignmentExpression {
        line_number: *line_number,
        identifier: {
          if !(*global_values_environment).contains(identifier) {
            type_errors.push(format!(
              "Line {:}: Undefined global variable `{:}`.",
              line_number, identifier
            ));
          }
          (*identifier).clone()
        },
        assigned_expression: type_check_expression(
          functions_environment,
          readable_values_environment,
          global_values_environment,
          ExpressionStaticType::IntType,
          type_errors,
          &*assigned_expression,
        ),
      })
    }
    &SourceLanguageExpression::ChainExpression {
      line_number,
      expressions,
    } => {
      check_type(
        *line_number,
        type_errors,
        expected_type,
        ExpressionStaticType::VoidType,
      );
      Box::new(SourceLanguageExpression::ChainExpression {
        line_number: *line_number,
        expressions: {
          let mut checked_expressions = Vec::new();
          for sub_expression in expressions {
            checked_expressions.push(type_check_expression(
              functions_environment,
              readable_values_environment,
              global_values_environment,
              ExpressionStaticType::VoidType,
              type_errors,
              &*sub_expression,
            ));
          }
          checked_expressions
        },
      })
    }
  }
}

fn type_check_program(
  functions_environment: HashMap<String, FunctionType>,
  program: &SourceLanguageProgram,
) -> (SourceLanguageProgram, Vec<String>) {
  let SourceLanguageProgram {
    global_variable_definitions,
    function_definitions,
  } = &*program;

  let mut type_errors = Vec::new();
  let mut mutable_global_values_environment = HashSet::new();
  let mut mutable_patched_functions_environment = functions_environment;

  for global_variable in global_variable_definitions {
    let name = global_variable.identifier.clone();
    if mutable_global_values_environment.contains(&name) {
      type_errors.push(format!(
        "Line {:}: Duplicate identifier: `{:}`",
        global_variable.line_number, name
      ))
    }
    mutable_global_values_environment = mutable_global_values_environment.update(name);
  }

  let global_values_environment = mutable_global_values_environment;

  let mut checked_global_variables = Vec::new();
  let mut checked_functions = Vec::new();

  for global_variable in global_variable_definitions {
    let SourceLanguageMutableGlobalVariableDefinition {
      line_number,
      identifier,
      assigned_value,
    } = &*global_variable;

    checked_global_variables.push(SourceLanguageMutableGlobalVariableDefinition {
      line_number: *line_number,
      identifier: (*identifier).clone(),
      assigned_value: *assigned_value,
    });
  }

  for function_definition in function_definitions {
    let name = function_definition.identifier.clone();
    if mutable_patched_functions_environment.contains_key(&name) {
      type_errors.push(format!(
        "Line {:}: Duplicate function: `{:}`",
        function_definition.line_number, name
      ))
    }

    let function_type = FunctionType {
      argument_types: function_definition
        .function_arguments
        .iter()
        .map(|(_, t)| *t)
        .collect(),
      return_type: function_definition.return_type,
    };
    mutable_patched_functions_environment =
      mutable_patched_functions_environment.update(name, function_type);

    let mut readable_values_environment = HashMap::new();
    for v in global_values_environment.iter() {
      readable_values_environment =
        readable_values_environment.update((*v).clone(), ExpressionStaticType::IntType);
    }
    for (parameter_name, parameter_type) in &*function_definition.function_arguments {
      let name = parameter_name.clone();
      if readable_values_environment.contains_key(&name) {
        type_errors.push(format!(
          "Line {:}: Duplicate function: `{:}`",
          function_definition.line_number, name
        ))
      }
      readable_values_environment = readable_values_environment.update(name, *parameter_type);
    }
    checked_functions.push(SourceLanguageFunctionDefinition {
      line_number: function_definition.line_number,
      identifier: function_definition.identifier.clone(),
      function_arguments: (function_definition.function_arguments).clone(),
      return_type: function_definition.return_type,
      body: type_check_expression(
        &mutable_patched_functions_environment,
        &readable_values_environment,
        &global_values_environment,
        function_definition.return_type,
        &mut type_errors,
        &*function_definition.body,
      ),
    })
  }

  let last_function = &checked_functions[checked_functions.len() - 1];
  if last_function.identifier != "main" || last_function.function_arguments.len() > 0 {
    type_errors.push(format!(
      "Line {:}: Missing main function with void return type at the end. We only have {:}.",
      last_function.line_number, last_function.identifier,
    ));
  }

  let checked_program = SourceLanguageProgram {
    global_variable_definitions: checked_global_variables,
    function_definitions: checked_functions,
  };
  (checked_program, type_errors)
}

pub fn get_type_checked_program(
  functions_environment: HashMap<String, FunctionType>,
  source_string: String,
) -> Result<SourceLanguageProgram, Vec<String>> {
  let generated_parser = SourceLanguageProgramParser::new();
  match generated_parser.parse(source_string.as_str()) {
    Ok(program) => {
      let (checked_program, errors) = type_check_program(functions_environment, &*program);
      if errors.len() > 0 {
        Err(errors)
      } else {
        Ok(checked_program)
      }
    }
    Err(e) => Err(vec![format!("{:}", e)]),
  }
}
