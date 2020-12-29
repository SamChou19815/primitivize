use crate::ast::{BinaryOperator, LiteralValue, SourceLanguageExpression};

pub fn compile_time_evaluation(
  expression: &SourceLanguageExpression,
) -> Box<SourceLanguageExpression> {
  match &expression {
    SourceLanguageExpression::LiteralExpression {
      line_number,
      static_type,
      literal,
    } => Box::new(SourceLanguageExpression::LiteralExpression {
      line_number: *line_number,
      static_type: *static_type,
      literal: *literal,
    }),
    SourceLanguageExpression::VariableExpression {
      line_number,
      static_type,
      identifier,
    } => Box::new(SourceLanguageExpression::VariableExpression {
      line_number: *line_number,
      static_type: *static_type,
      identifier: (*identifier).clone(),
    }),
    SourceLanguageExpression::FunctionCallExpression {
      line_number,
      static_type,
      function_name,
      function_arguments,
    } => Box::new(SourceLanguageExpression::FunctionCallExpression {
      line_number: *line_number,
      static_type: *static_type,
      function_name: (*function_name).clone(),
      function_arguments: function_arguments
        .iter()
        .map(|e| compile_time_evaluation(e))
        .collect(),
    }),
    SourceLanguageExpression::BinaryExpression {
      line_number,
      static_type,
      operator,
      e1,
      e2,
    } => {
      let evaluated_e1 = compile_time_evaluation(e1);
      let evaluated_e2 = compile_time_evaluation(e2);
      let copied_evaluated_e1 = evaluated_e1.clone();
      let copied_evaluated_e2 = evaluated_e2.clone();
      let generic = Box::new(SourceLanguageExpression::BinaryExpression {
        line_number: *line_number,
        static_type: *static_type,
        operator: *operator,
        e1: evaluated_e1,
        e2: evaluated_e2,
      });
      match (*copied_evaluated_e1, *copied_evaluated_e2) {
        (
          SourceLanguageExpression::LiteralExpression {
            line_number: _,
            static_type: _,
            literal: l1,
          },
          SourceLanguageExpression::LiteralExpression {
            line_number: _,
            static_type: _,
            literal: l2,
          },
        ) => match operator {
          BinaryOperator::MUL => match (l1, l2) {
            (LiteralValue::IntLiteral(i1), LiteralValue::IntLiteral(i2)) => {
              Box::new(SourceLanguageExpression::LiteralExpression {
                line_number: *line_number,
                static_type: *static_type,
                literal: LiteralValue::IntLiteral(i1 * i2),
              })
            }
            _ => generic,
          },
          BinaryOperator::DIV => match (l1, l2) {
            (LiteralValue::IntLiteral(i1), LiteralValue::IntLiteral(i2)) => {
              Box::new(SourceLanguageExpression::LiteralExpression {
                line_number: *line_number,
                static_type: *static_type,
                literal: LiteralValue::IntLiteral(i1 / i2),
              })
            }
            _ => generic,
          },
          BinaryOperator::MOD => match (l1, l2) {
            (LiteralValue::IntLiteral(i1), LiteralValue::IntLiteral(i2)) => {
              Box::new(SourceLanguageExpression::LiteralExpression {
                line_number: *line_number,
                static_type: *static_type,
                literal: LiteralValue::IntLiteral(i1 % i2),
              })
            }
            _ => generic,
          },
          BinaryOperator::PLUS => match (l1, l2) {
            (LiteralValue::IntLiteral(i1), LiteralValue::IntLiteral(i2)) => {
              Box::new(SourceLanguageExpression::LiteralExpression {
                line_number: *line_number,
                static_type: *static_type,
                literal: LiteralValue::IntLiteral(i1 + i2),
              })
            }
            _ => generic,
          },
          BinaryOperator::MINUS => match (l1, l2) {
            (LiteralValue::IntLiteral(i1), LiteralValue::IntLiteral(i2)) => {
              Box::new(SourceLanguageExpression::LiteralExpression {
                line_number: *line_number,
                static_type: *static_type,
                literal: LiteralValue::IntLiteral(i1 - i2),
              })
            }
            _ => generic,
          },
          BinaryOperator::LT => match (l1, l2) {
            (LiteralValue::IntLiteral(i1), LiteralValue::IntLiteral(i2)) => {
              Box::new(SourceLanguageExpression::LiteralExpression {
                line_number: *line_number,
                static_type: *static_type,
                literal: LiteralValue::BoolLiteral(i1 < i2),
              })
            }
            _ => generic,
          },
          BinaryOperator::LE => match (l1, l2) {
            (LiteralValue::IntLiteral(i1), LiteralValue::IntLiteral(i2)) => {
              Box::new(SourceLanguageExpression::LiteralExpression {
                line_number: *line_number,
                static_type: *static_type,
                literal: LiteralValue::BoolLiteral(i1 <= i2),
              })
            }
            _ => generic,
          },
          BinaryOperator::GT => match (l1, l2) {
            (LiteralValue::IntLiteral(i1), LiteralValue::IntLiteral(i2)) => {
              Box::new(SourceLanguageExpression::LiteralExpression {
                line_number: *line_number,
                static_type: *static_type,
                literal: LiteralValue::BoolLiteral(i1 > i2),
              })
            }
            _ => generic,
          },
          BinaryOperator::GE => match (l1, l2) {
            (LiteralValue::IntLiteral(i1), LiteralValue::IntLiteral(i2)) => {
              Box::new(SourceLanguageExpression::LiteralExpression {
                line_number: *line_number,
                static_type: *static_type,
                literal: LiteralValue::BoolLiteral(i1 >= i2),
              })
            }
            _ => generic,
          },
          BinaryOperator::EQ => match (l1, l2) {
            (LiteralValue::IntLiteral(i1), LiteralValue::IntLiteral(i2)) => {
              Box::new(SourceLanguageExpression::LiteralExpression {
                line_number: *line_number,
                static_type: *static_type,
                literal: LiteralValue::BoolLiteral(i1 == i2),
              })
            }
            _ => generic,
          },
          BinaryOperator::NE => match (l1, l2) {
            (LiteralValue::IntLiteral(i1), LiteralValue::IntLiteral(i2)) => {
              Box::new(SourceLanguageExpression::LiteralExpression {
                line_number: *line_number,
                static_type: *static_type,
                literal: LiteralValue::BoolLiteral(i1 != i2),
              })
            }
            _ => generic,
          },
          BinaryOperator::AND => match (l1, l2) {
            (LiteralValue::BoolLiteral(i1), LiteralValue::BoolLiteral(i2)) => {
              Box::new(SourceLanguageExpression::LiteralExpression {
                line_number: *line_number,
                static_type: *static_type,
                literal: LiteralValue::BoolLiteral(i1 && i2),
              })
            }
            _ => generic,
          },
          BinaryOperator::OR => match (l1, l2) {
            (LiteralValue::BoolLiteral(i1), LiteralValue::BoolLiteral(i2)) => {
              Box::new(SourceLanguageExpression::LiteralExpression {
                line_number: *line_number,
                static_type: *static_type,
                literal: LiteralValue::BoolLiteral(i1 || i2),
              })
            }
            _ => generic,
          },
        },
        _ => generic,
      }
    }
    SourceLanguageExpression::IfElseExpression {
      line_number,
      static_type,
      condition,
      e1,
      e2,
    } => {
      let evaluated_condition = compile_time_evaluation(&condition);
      let evaluated_e1 = compile_time_evaluation(&e1);
      let evaluated_e2 = compile_time_evaluation(&e2);
      match *evaluated_condition {
        SourceLanguageExpression::LiteralExpression {
          line_number: _,
          static_type: _,
          literal: LiteralValue::BoolLiteral(true),
        } => evaluated_e1,
        SourceLanguageExpression::LiteralExpression {
          line_number: _,
          static_type: _,
          literal: LiteralValue::BoolLiteral(false),
        } => evaluated_e2,
        _ => Box::new(SourceLanguageExpression::IfElseExpression {
          line_number: *line_number,
          static_type: *static_type,
          condition: evaluated_condition,
          e1: evaluated_e1,
          e2: evaluated_e2,
        }),
      }
    }
    SourceLanguageExpression::AssignmentExpression {
      line_number,
      static_type,
      identifier,
      assigned_expression,
    } => Box::new(SourceLanguageExpression::AssignmentExpression {
      line_number: *line_number,
      static_type: *static_type,
      identifier: (*identifier).clone(),
      assigned_expression: compile_time_evaluation(&assigned_expression),
    }),
    SourceLanguageExpression::ChainExpression {
      line_number,
      static_type,
      expressions,
    } => {
      let mut replaced_expressions = Vec::new();
      for sub_expression in expressions {
        replaced_expressions.push(compile_time_evaluation(&sub_expression));
      }
      Box::new(SourceLanguageExpression::ChainExpression {
        line_number: *line_number,
        static_type: *static_type,
        expressions: replaced_expressions,
      })
    }
  }
}
