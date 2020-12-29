use crate::ast::{BinaryOperator, IfElseBlock, LiteralValue, SourceLanguageExpression};

fn hoist_if_else(expression: &SourceLanguageExpression) -> Box<SourceLanguageExpression> {
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
      line_number: _,
      static_type: _,
      function_name: _,
      function_arguments: _,
    } => Box::new(expression.clone()), // Assume no more if-else inside leaf function calls.
    SourceLanguageExpression::BinaryExpression {
      line_number,
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
            condition: e1c,
            e1: e1e1,
            e2: e1e2,
          },
          SourceLanguageExpression::IfElseExpression {
            line_number: _,
            condition: e2c,
            e1: e2e1,
            e2: e2e2,
          },
        ) => Box::new(SourceLanguageExpression::IfElseExpression {
          line_number: *line_number,
          condition: Box::new(SourceLanguageExpression::BinaryExpression {
            line_number: *line_number,
            operator: BinaryOperator::AND,
            e1: e1c.clone(),
            e2: e2c.clone(),
          }),
          e1: hoist_if_else(&SourceLanguageExpression::BinaryExpression {
            line_number: *line_number,
            operator: *operator,
            e1: e1e1.clone(),
            e2: e2e1.clone(),
          }),
          e2: Box::new(SourceLanguageExpression::IfElseExpression {
            line_number: *line_number,
            condition: e1c.clone(),
            e1: hoist_if_else(&SourceLanguageExpression::BinaryExpression {
              line_number: *line_number,
              operator: *operator,
              e1: e1e1.clone(),
              e2: e2e2.clone(),
            }),
            e2: Box::new(SourceLanguageExpression::IfElseExpression {
              line_number: *line_number,
              condition: e2c.clone(),
              e1: hoist_if_else(&SourceLanguageExpression::BinaryExpression {
                line_number: *line_number,
                operator: *operator,
                e1: e1e2.clone(),
                e2: e2e1.clone(),
              }),
              e2: hoist_if_else(&SourceLanguageExpression::BinaryExpression {
                line_number: *line_number,
                operator: *operator,
                e1: e1e2.clone(),
                e2: e2e2.clone(),
              }),
            }),
          }),
        }),
        (
          SourceLanguageExpression::IfElseExpression {
            line_number: _,
            condition: e1c,
            e1: e1e1,
            e2: e1e2,
          },
          hoisted_e2_unboxed,
        ) => Box::new(SourceLanguageExpression::IfElseExpression {
          line_number: *line_number,
          condition: e1c,
          e1: hoist_if_else(&SourceLanguageExpression::BinaryExpression {
            line_number: *line_number,
            operator: *operator,
            e1: e1e1,
            e2: Box::new(hoisted_e2_unboxed.clone()),
          }),
          e2: hoist_if_else(&SourceLanguageExpression::BinaryExpression {
            line_number: *line_number,
            operator: *operator,
            e1: e1e2,
            e2: Box::new(hoisted_e2_unboxed.clone()),
          }),
        }),
        (
          hoisted_e1_unboxed,
          SourceLanguageExpression::IfElseExpression {
            line_number: _,
            condition: e2c,
            e1: e2e1,
            e2: e2e2,
          },
        ) => Box::new(SourceLanguageExpression::IfElseExpression {
          line_number: *line_number,
          condition: e2c,
          e1: hoist_if_else(&SourceLanguageExpression::BinaryExpression {
            line_number: *line_number,
            operator: *operator,
            e1: Box::new(hoisted_e1_unboxed.clone()),
            e2: e2e1,
          }),
          e2: hoist_if_else(&SourceLanguageExpression::BinaryExpression {
            line_number: *line_number,
            operator: *operator,
            e1: Box::new(hoisted_e1_unboxed.clone()),
            e2: e2e2,
          }),
        }),
        (hoisted_e1_unboxed, hoisted_e2_unboxed) => {
          Box::new(SourceLanguageExpression::BinaryExpression {
            line_number: *line_number,
            operator: *operator,
            e1: Box::new(hoisted_e1_unboxed),
            e2: Box::new(hoisted_e2_unboxed),
          })
        }
      }
    }
    SourceLanguageExpression::IfElseExpression {
      line_number,
      condition,
      e1,
      e2,
    } => Box::new(SourceLanguageExpression::IfElseExpression {
      line_number: *line_number,
      condition: hoist_if_else(condition), // TODO ???
      e1: hoist_if_else(e1),
      e2: hoist_if_else(e2),
    }),
    SourceLanguageExpression::AssignmentExpression {
      line_number,
      identifier,
      assigned_expression,
    } => match *hoist_if_else(assigned_expression) {
      SourceLanguageExpression::IfElseExpression {
        line_number: _,
        condition,
        e1,
        e2,
      } => Box::new(SourceLanguageExpression::IfElseExpression {
        line_number: *line_number,
        condition,
        e1: hoist_if_else(&SourceLanguageExpression::AssignmentExpression {
          line_number: *line_number,
          identifier: identifier.clone(),
          assigned_expression: e1,
        }),
        e2: hoist_if_else(&SourceLanguageExpression::AssignmentExpression {
          line_number: *line_number,
          identifier: identifier.clone(),
          assigned_expression: e2,
        }),
      }),
      _ => Box::new((*expression).clone()),
    },
    SourceLanguageExpression::ChainExpression {
      line_number,
      expressions,
    } => {
      if expressions.len() == 0 {
        Box::new((*expression).clone())
      } else if expressions.len() == 1 {
        expressions[0].clone()
      } else {
        let mut mutable_expressions = expressions.clone();
        let hoisted_e2 = hoist_if_else(&mutable_expressions.pop().unwrap());
        let hoisted_e1 = hoist_if_else(&SourceLanguageExpression::ChainExpression {
          line_number: *line_number,
          expressions: mutable_expressions,
        });
        match (*hoisted_e1, *hoisted_e2) {
          (
            SourceLanguageExpression::IfElseExpression {
              line_number: _,
              condition: e1c,
              e1: e1e1,
              e2: e1e2,
            },
            SourceLanguageExpression::IfElseExpression {
              line_number: _,
              condition: e2c,
              e1: e2e1,
              e2: e2e2,
            },
          ) => Box::new(SourceLanguageExpression::IfElseExpression {
            line_number: *line_number,
            condition: Box::new(SourceLanguageExpression::BinaryExpression {
              line_number: *line_number,
              operator: BinaryOperator::AND,
              e1: e1c.clone(),
              e2: e2c.clone(),
            }),
            e1: hoist_if_else(&SourceLanguageExpression::ChainExpression {
              line_number: *line_number,
              expressions: vec![e1e1.clone(), e2e1.clone()],
            }),
            e2: Box::new(SourceLanguageExpression::IfElseExpression {
              line_number: *line_number,
              condition: e1c.clone(),
              e1: hoist_if_else(&SourceLanguageExpression::ChainExpression {
                line_number: *line_number,
                expressions: vec![e1e1.clone(), e2e2.clone()],
              }),
              e2: Box::new(SourceLanguageExpression::IfElseExpression {
                line_number: *line_number,
                condition: e2c.clone(),
                e1: hoist_if_else(&SourceLanguageExpression::ChainExpression {
                  line_number: *line_number,
                  expressions: vec![e1e2.clone(), e2e1.clone()],
                }),
                e2: hoist_if_else(&SourceLanguageExpression::ChainExpression {
                  line_number: *line_number,
                  expressions: vec![e1e2.clone(), e2e2.clone()],
                }),
              }),
            }),
          }),
          (
            SourceLanguageExpression::IfElseExpression {
              line_number: _,
              condition: e1c,
              e1: e1e1,
              e2: e1e2,
            },
            hoisted_e2_unboxed,
          ) => Box::new(SourceLanguageExpression::IfElseExpression {
            line_number: *line_number,
            condition: e1c,
            e1: hoist_if_else(&SourceLanguageExpression::ChainExpression {
              line_number: *line_number,
              expressions: vec![e1e1, Box::new(hoisted_e2_unboxed.clone())],
            }),
            e2: hoist_if_else(&SourceLanguageExpression::ChainExpression {
              line_number: *line_number,
              expressions: vec![e1e2, Box::new(hoisted_e2_unboxed.clone())],
            }),
          }),
          (
            hoisted_e1_unboxed,
            SourceLanguageExpression::IfElseExpression {
              line_number: _,
              condition: e2c,
              e1: e2e1,
              e2: e2e2,
            },
          ) => Box::new(SourceLanguageExpression::IfElseExpression {
            line_number: *line_number,
            condition: e2c,
            e1: hoist_if_else(&SourceLanguageExpression::ChainExpression {
              line_number: *line_number,
              expressions: vec![Box::new(hoisted_e1_unboxed.clone()), e2e1],
            }),
            e2: hoist_if_else(&SourceLanguageExpression::ChainExpression {
              line_number: *line_number,
              expressions: vec![Box::new(hoisted_e1_unboxed.clone()), e2e2],
            }),
          }),
          (hoisted_e1_unboxed, hoisted_e2_unboxed) => {
            Box::new(SourceLanguageExpression::ChainExpression {
              line_number: *line_number,
              expressions: vec![Box::new(hoisted_e1_unboxed), Box::new(hoisted_e2_unboxed)],
            })
          }
        }
      }
    }
  }
}

pub fn transform_to_if_else_blocks(expression: &SourceLanguageExpression) -> Vec<IfElseBlock> {
  match &*hoist_if_else(&expression) {
    SourceLanguageExpression::IfElseExpression {
      line_number: _,
      condition,
      e1,
      e2,
    } => {
      let e1_list = transform_to_if_else_blocks(e1);
      let mut e2_list = transform_to_if_else_blocks(e2).clone();
      let mut list = Vec::new();
      for IfElseBlock {
        condition: c,
        action,
      } in e1_list
      {
        list.push(IfElseBlock {
          condition: SourceLanguageExpression::BinaryExpression {
            line_number: 1,
            operator: BinaryOperator::AND,
            e1: condition.clone(),
            e2: Box::new(c),
          },
          action,
        });
      }
      list.append(&mut e2_list);
      list
    }
    hoisted => vec![IfElseBlock {
      condition: SourceLanguageExpression::LiteralExpression {
        line_number: 1,
        literal: LiteralValue::BoolLiteral(true),
      },
      action: (*hoisted).clone(),
    }],
  }
}
