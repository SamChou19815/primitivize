use crate::ast::{BinaryOperator, ExpressionStaticType, SourceLanguageExpression};

pub fn hoist_if_else(expression: Box<SourceLanguageExpression>) -> Box<SourceLanguageExpression> {
  let cloned = (*expression).clone();
  match cloned {
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
    } => match *hoist_if_else(sub_expression) {
      SourceLanguageExpression::IfElseExpression {
        line_number: _,
        static_type: _,
        condition,
        e1,
        e2,
      } => Box::new(SourceLanguageExpression::IfElseExpression {
        line_number,
        static_type,
        condition,
        e1: hoist_if_else(Box::new(SourceLanguageExpression::NotExpression {
          line_number,
          static_type,
          sub_expression: e1,
        })),
        e2: hoist_if_else(Box::new(SourceLanguageExpression::NotExpression {
          line_number,
          static_type,
          sub_expression: e2,
        })),
      }),
      _ => expression,
    },
    SourceLanguageExpression::FunctionCallExpression {
      line_number: _,
      static_type: _,
      function_name: _,
      function_arguments: _,
    } => expression, // Assume no more if-else inside leaf function calls.
    SourceLanguageExpression::BinaryExpression {
      line_number,
      static_type,
      operator,
      e1,
      e2,
    } => {
      let hoisted_e1 = hoist_if_else(e1);
      let hoisted_e2 = hoist_if_else(e2);
      match (*hoisted_e1, *hoisted_e2) {
        (
          SourceLanguageExpression::IfElseExpression {
            line_number: _,
            static_type: _,
            condition: e1c,
            e1: e1e1,
            e2: e1e2,
          },
          SourceLanguageExpression::IfElseExpression {
            line_number: _,
            static_type: _,
            condition: e2c,
            e1: e2e1,
            e2: e2e2,
          },
        ) => Box::new(SourceLanguageExpression::IfElseExpression {
          line_number,
          static_type,
          condition: Box::new(SourceLanguageExpression::BinaryExpression {
            line_number,
            static_type: ExpressionStaticType::BoolType,
            operator: BinaryOperator::AND,
            e1: e1c.clone(),
            e2: e2c.clone(),
          }),
          e1: hoist_if_else(Box::new(SourceLanguageExpression::BinaryExpression {
            line_number,
            static_type,
            operator,
            e1: e1e1.clone(),
            e2: e2e1.clone(),
          })),
          e2: Box::new(SourceLanguageExpression::IfElseExpression {
            line_number,
            static_type,
            condition: e1c.clone(),
            e1: hoist_if_else(Box::new(SourceLanguageExpression::BinaryExpression {
              line_number,
              static_type,
              operator,
              e1: e1e1.clone(),
              e2: e2e2.clone(),
            })),
            e2: Box::new(SourceLanguageExpression::IfElseExpression {
              line_number,
              static_type,
              condition: e2c.clone(),
              e1: hoist_if_else(Box::new(SourceLanguageExpression::BinaryExpression {
                line_number,
                static_type,
                operator,
                e1: e1e2.clone(),
                e2: e2e1.clone(),
              })),
              e2: hoist_if_else(Box::new(SourceLanguageExpression::BinaryExpression {
                line_number,
                static_type,
                operator,
                e1: e1e2.clone(),
                e2: e2e2.clone(),
              })),
            }),
          }),
        }),
        (
          SourceLanguageExpression::IfElseExpression {
            line_number: _,
            static_type: _,
            condition: e1c,
            e1: e1e1,
            e2: e1e2,
          },
          hoisted_e2_unboxed,
        ) => Box::new(SourceLanguageExpression::IfElseExpression {
          line_number,
          static_type,
          condition: e1c,
          e1: hoist_if_else(Box::new(SourceLanguageExpression::BinaryExpression {
            line_number,
            static_type,
            operator,
            e1: e1e1,
            e2: Box::new(hoisted_e2_unboxed.clone()),
          })),
          e2: hoist_if_else(Box::new(SourceLanguageExpression::BinaryExpression {
            line_number,
            static_type,
            operator,
            e1: e1e2,
            e2: Box::new(hoisted_e2_unboxed.clone()),
          })),
        }),
        (
          hoisted_e1_unboxed,
          SourceLanguageExpression::IfElseExpression {
            line_number: _,
            static_type: _,
            condition: e2c,
            e1: e2e1,
            e2: e2e2,
          },
        ) => Box::new(SourceLanguageExpression::IfElseExpression {
          line_number,
          static_type,
          condition: e2c,
          e1: hoist_if_else(Box::new(SourceLanguageExpression::BinaryExpression {
            line_number,
            static_type,
            operator,
            e1: Box::new(hoisted_e1_unboxed.clone()),
            e2: e2e1,
          })),
          e2: hoist_if_else(Box::new(SourceLanguageExpression::BinaryExpression {
            line_number,
            static_type,
            operator,
            e1: Box::new(hoisted_e1_unboxed.clone()),
            e2: e2e2,
          })),
        }),
        (hoisted_e1_unboxed, hoisted_e2_unboxed) => {
          Box::new(SourceLanguageExpression::BinaryExpression {
            line_number,
            static_type,
            operator,
            e1: Box::new(hoisted_e1_unboxed),
            e2: Box::new(hoisted_e2_unboxed),
          })
        }
      }
    }
    SourceLanguageExpression::IfElseExpression {
      line_number,
      static_type,
      condition,
      e1,
      e2,
    } => Box::new(SourceLanguageExpression::IfElseExpression {
      line_number,
      static_type,
      condition: hoist_if_else(condition), // TODO ???
      e1: hoist_if_else(e1),
      e2: hoist_if_else(e2),
    }),
    SourceLanguageExpression::AssignmentExpression {
      line_number,
      static_type,
      identifier,
      assigned_expression,
    } => match *hoist_if_else(assigned_expression) {
      SourceLanguageExpression::IfElseExpression {
        line_number: _,
        static_type: _,
        condition,
        e1,
        e2,
      } => Box::new(SourceLanguageExpression::IfElseExpression {
        line_number,
        static_type,
        condition,
        e1: hoist_if_else(Box::new(SourceLanguageExpression::AssignmentExpression {
          line_number,
          static_type,
          identifier: identifier.clone(),
          assigned_expression: e1,
        })),
        e2: hoist_if_else(Box::new(SourceLanguageExpression::AssignmentExpression {
          line_number,
          static_type,
          identifier: identifier.clone(),
          assigned_expression: e2,
        })),
      }),
      _ => expression,
    },
    SourceLanguageExpression::ChainExpression {
      line_number,
      static_type,
      expressions,
    } => {
      if expressions.len() == 0 {
        expression
      } else if expressions.len() == 1 {
        expressions[0].clone()
      } else {
        let mut mutable_expressions = expressions.clone();
        let hoisted_e2 = hoist_if_else(mutable_expressions.pop().unwrap());
        let hoisted_e1 = hoist_if_else(Box::new(SourceLanguageExpression::ChainExpression {
          line_number,
          static_type,
          expressions: mutable_expressions,
        }));
        match (*hoisted_e1, *hoisted_e2) {
          (
            SourceLanguageExpression::IfElseExpression {
              line_number: _,
              static_type: _,
              condition: e1c,
              e1: e1e1,
              e2: e1e2,
            },
            SourceLanguageExpression::IfElseExpression {
              line_number: _,
              static_type: _,
              condition: e2c,
              e1: e2e1,
              e2: e2e2,
            },
          ) => Box::new(SourceLanguageExpression::IfElseExpression {
            line_number,
            static_type,
            condition: Box::new(SourceLanguageExpression::BinaryExpression {
              line_number,
              static_type: ExpressionStaticType::BoolType,
              operator: BinaryOperator::AND,
              e1: e1c.clone(),
              e2: e2c.clone(),
            }),
            e1: hoist_if_else(Box::new(SourceLanguageExpression::ChainExpression {
              line_number,
              static_type,
              expressions: vec![e1e1.clone(), e2e1.clone()],
            })),
            e2: Box::new(SourceLanguageExpression::IfElseExpression {
              line_number,
              static_type,
              condition: e1c.clone(),
              e1: hoist_if_else(Box::new(SourceLanguageExpression::ChainExpression {
                line_number,
                static_type,
                expressions: vec![e1e1.clone(), e2e2.clone()],
              })),
              e2: Box::new(SourceLanguageExpression::IfElseExpression {
                line_number,
                static_type,
                condition: e2c.clone(),
                e1: hoist_if_else(Box::new(SourceLanguageExpression::ChainExpression {
                  line_number,
                  static_type,
                  expressions: vec![e1e2.clone(), e2e1.clone()],
                })),
                e2: hoist_if_else(Box::new(SourceLanguageExpression::ChainExpression {
                  line_number,
                  static_type,
                  expressions: vec![e1e2.clone(), e2e2.clone()],
                })),
              }),
            }),
          }),
          (
            SourceLanguageExpression::IfElseExpression {
              line_number: _,
              static_type: _,
              condition: e1c,
              e1: e1e1,
              e2: e1e2,
            },
            hoisted_e2_unboxed,
          ) => Box::new(SourceLanguageExpression::IfElseExpression {
            line_number,
            static_type,
            condition: e1c,
            e1: hoist_if_else(Box::new(SourceLanguageExpression::ChainExpression {
              line_number,
              static_type,
              expressions: vec![e1e1, Box::new(hoisted_e2_unboxed.clone())],
            })),
            e2: hoist_if_else(Box::new(SourceLanguageExpression::ChainExpression {
              line_number,
              static_type,
              expressions: vec![e1e2, Box::new(hoisted_e2_unboxed.clone())],
            })),
          }),
          (
            hoisted_e1_unboxed,
            SourceLanguageExpression::IfElseExpression {
              line_number: _,
              static_type: _,
              condition: e2c,
              e1: e2e1,
              e2: e2e2,
            },
          ) => Box::new(SourceLanguageExpression::IfElseExpression {
            line_number,
            static_type,
            condition: e2c,
            e1: hoist_if_else(Box::new(SourceLanguageExpression::ChainExpression {
              line_number,
              static_type,
              expressions: vec![Box::new(hoisted_e1_unboxed.clone()), e2e1],
            })),
            e2: hoist_if_else(Box::new(SourceLanguageExpression::ChainExpression {
              line_number,
              static_type,
              expressions: vec![Box::new(hoisted_e1_unboxed.clone()), e2e2],
            })),
          }),
          (hoisted_e1_unboxed, hoisted_e2_unboxed) => {
            Box::new(SourceLanguageExpression::ChainExpression {
              line_number,
              static_type,
              expressions: vec![Box::new(hoisted_e1_unboxed), Box::new(hoisted_e2_unboxed)],
            })
          }
        }
      }
    }
  }
}
