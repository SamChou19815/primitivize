use crate::ast::{
  pretty_print_expression_static_type, BinaryOperator, ExpressionStaticType, FunctionType,
  LiteralValue, SourceLanguageExpression,
};
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
  functions_environment: &HashMap<String, &FunctionType>,
  local_values_environment: &HashSet<String>,
  expected_type: ExpressionStaticType,
  type_errors: &mut Vec<String>,
  expression: Box<SourceLanguageExpression>,
) -> Box<SourceLanguageExpression> {
  match *expression {
    SourceLanguageExpression::LiteralExpression {
      line_number,
      static_type: _,
      literal: LiteralValue::IntLiteral(i),
    } => Box::new(SourceLanguageExpression::LiteralExpression {
      line_number,
      static_type: check_type(
        line_number,
        type_errors,
        expected_type,
        ExpressionStaticType::IntType,
      ),
      literal: LiteralValue::IntLiteral(i),
    }),
    SourceLanguageExpression::LiteralExpression {
      line_number,
      static_type: _,
      literal: LiteralValue::BoolLiteral(b),
    } => Box::new(SourceLanguageExpression::LiteralExpression {
      line_number,
      static_type: check_type(
        line_number,
        type_errors,
        expected_type,
        ExpressionStaticType::BoolType,
      ),
      literal: LiteralValue::BoolLiteral(b),
    }),
    SourceLanguageExpression::VariableExpression {
      line_number,
      static_type: _,
      identifier,
    } => Box::new(SourceLanguageExpression::VariableExpression {
      line_number,
      static_type: {
        check_type(
          line_number,
          type_errors,
          expected_type,
          ExpressionStaticType::IntType,
        );
        if !(*local_values_environment).contains(&identifier) {
          type_errors.push(format!(
            "Line {:}: Undefined variable `{:}`.",
            line_number, identifier
          ));
        }
        ExpressionStaticType::IntType
      },
      identifier,
    }),
    SourceLanguageExpression::NotExpression {
      line_number,
      static_type: _,
      sub_expression,
    } => Box::new(SourceLanguageExpression::NotExpression {
      line_number,
      static_type: check_type(
        line_number,
        type_errors,
        expected_type,
        ExpressionStaticType::BoolType,
      ),
      sub_expression: type_check_expression(
        functions_environment,
        local_values_environment,
        ExpressionStaticType::BoolType,
        type_errors,
        sub_expression,
      ),
    }),
    SourceLanguageExpression::FunctionCallExpression {
      line_number,
      static_type,
      function_name,
      function_arguments,
    } => {
      match (*functions_environment).get(&function_name) {
        None => {
          type_errors.push(format!(
            "Line {:}: Undefined function `{:}`.",
            line_number, function_name
          ));
          Box::new(SourceLanguageExpression::FunctionCallExpression {
            line_number,
            static_type,
            function_name,
            function_arguments,
          })
        }
        Some(&function_type) => {
          let FunctionType {
            argument_types,
            return_type,
          } = &*function_type;
          // Check return type
          check_type(line_number, type_errors, expected_type, *return_type);
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
              local_values_environment,
              *argument_type,
              type_errors,
              argument_expression,
            ));
          }
          Box::new(SourceLanguageExpression::FunctionCallExpression {
            line_number,
            static_type: *return_type,
            function_name,
            function_arguments: checked_function_arguments,
          })
        }
      }
    }
    SourceLanguageExpression::BinaryExpression {
      line_number,
      static_type: _,
      operator,
      e1,
      e2,
    } => match operator {
      BinaryOperator::MUL
      | BinaryOperator::DIV
      | BinaryOperator::MOD
      | BinaryOperator::PLUS
      | BinaryOperator::MINUS => Box::new(SourceLanguageExpression::BinaryExpression {
        line_number,
        static_type: check_type(
          line_number,
          type_errors,
          expected_type,
          ExpressionStaticType::IntType,
        ),
        operator,
        e1: type_check_expression(
          functions_environment,
          local_values_environment,
          ExpressionStaticType::IntType,
          type_errors,
          e1,
        ),
        e2: type_check_expression(
          functions_environment,
          local_values_environment,
          ExpressionStaticType::IntType,
          type_errors,
          e2,
        ),
      }),
      BinaryOperator::LT | BinaryOperator::LE | BinaryOperator::GT | BinaryOperator::GE => {
        Box::new(SourceLanguageExpression::BinaryExpression {
          line_number,
          static_type: check_type(
            line_number,
            type_errors,
            expected_type,
            ExpressionStaticType::BoolType,
          ),
          operator,
          e1: type_check_expression(
            functions_environment,
            local_values_environment,
            ExpressionStaticType::IntType,
            type_errors,
            e1,
          ),
          e2: type_check_expression(
            functions_environment,
            local_values_environment,
            ExpressionStaticType::IntType,
            type_errors,
            e2,
          ),
        })
      }
      BinaryOperator::AND | BinaryOperator::OR => {
        Box::new(SourceLanguageExpression::BinaryExpression {
          line_number,
          static_type: check_type(
            line_number,
            type_errors,
            expected_type,
            ExpressionStaticType::BoolType,
          ),
          operator,
          e1: type_check_expression(
            functions_environment,
            local_values_environment,
            ExpressionStaticType::BoolType,
            type_errors,
            e1,
          ),
          e2: type_check_expression(
            functions_environment,
            local_values_environment,
            ExpressionStaticType::BoolType,
            type_errors,
            e2,
          ),
        })
      }
      BinaryOperator::EQ | BinaryOperator::NE => {
        Box::new(SourceLanguageExpression::BinaryExpression {
          line_number,
          static_type: check_type(
            line_number,
            type_errors,
            expected_type,
            ExpressionStaticType::BoolType,
          ),
          operator,
          e1: type_check_expression(
            functions_environment,
            local_values_environment,
            ExpressionStaticType::IntType,
            type_errors,
            e1,
          ),
          e2: type_check_expression(
            functions_environment,
            local_values_environment,
            ExpressionStaticType::IntType,
            type_errors,
            e2,
          ),
        })
      }
    },
    SourceLanguageExpression::IfElseExpression {
      line_number,
      static_type: _,
      condition,
      e1,
      e2,
    } => Box::new(SourceLanguageExpression::IfElseExpression {
      line_number,
      static_type: expected_type,
      condition: type_check_expression(
        functions_environment,
        local_values_environment,
        ExpressionStaticType::BoolType,
        type_errors,
        condition,
      ),
      e1: type_check_expression(
        functions_environment,
        local_values_environment,
        expected_type,
        type_errors,
        e1,
      ),
      e2: type_check_expression(
        functions_environment,
        local_values_environment,
        expected_type,
        type_errors,
        e2,
      ),
    }),
    SourceLanguageExpression::AssignmentExpression {
      line_number,
      static_type: _,
      identifier,
      assigned_expression,
    } => Box::new(SourceLanguageExpression::AssignmentExpression {
      line_number,
      static_type: check_type(
        line_number,
        type_errors,
        expected_type,
        ExpressionStaticType::VoidType,
      ),
      identifier,
      assigned_expression: type_check_expression(
        functions_environment,
        local_values_environment,
        ExpressionStaticType::IntType,
        type_errors,
        assigned_expression,
      ),
    }),
    SourceLanguageExpression::ChainExpression {
      line_number,
      static_type: _,
      expressions,
    } => Box::new(SourceLanguageExpression::ChainExpression {
      line_number,
      static_type: check_type(
        line_number,
        type_errors,
        expected_type,
        ExpressionStaticType::VoidType,
      ),
      expressions: {
        let mut checked_expressions = Vec::new();
        for sub_expression in expressions {
          checked_expressions.push(type_check_expression(
            functions_environment,
            local_values_environment,
            ExpressionStaticType::VoidType,
            type_errors,
            sub_expression,
          ));
        }
        checked_expressions
      },
    }),
  }
}
