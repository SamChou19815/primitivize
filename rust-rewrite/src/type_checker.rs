use crate::ast::{ExpressionStaticType, LiteralValue, SourceLanguageExpression};

fn type_check_expression(
  expression: Box<SourceLanguageExpression>,
) -> Box<SourceLanguageExpression> {
  match *expression {
    SourceLanguageExpression::LiteralExpression {
      line_number,
      static_type: _,
      literal: LiteralValue::IntLiteral(i),
    } => Box::new(SourceLanguageExpression::LiteralExpression {
      line_number,
      static_type: ExpressionStaticType::IntType,
      literal: LiteralValue::IntLiteral(i),
    }),
    SourceLanguageExpression::LiteralExpression {
      line_number,
      static_type: _,
      literal: LiteralValue::BoolLiteral(b),
    } => Box::new(SourceLanguageExpression::LiteralExpression {
      line_number,
      static_type: ExpressionStaticType::BoolType,
      literal: LiteralValue::BoolLiteral(b),
    }),
    _ => expression,
  }
}
