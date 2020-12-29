use crate::ast::{
  BinaryOperator, ExpressionStaticType, FullyInlinedProgram, IfElseBlock, LiteralValue,
  SourceLanguageExpression, SourceLanguageProgram,
};
use crate::inliner::program_inline;
use std::collections::HashMap;

fn pretty_print(
  expression: &SourceLanguageExpression,
  string_builder: &mut String,
  variable_replacement_map: &HashMap<String, i32>,
) -> () {
  match &expression {
    SourceLanguageExpression::LiteralExpression {
      line_number: _,
      static_type: _,
      literal,
    } => match literal {
      LiteralValue::BoolLiteral(true) => string_builder.push_str("1 = 1"),
      LiteralValue::BoolLiteral(false) => string_builder.push_str("1 = 0"),
      LiteralValue::IntLiteral(i) => string_builder.push_str(&format!("{:}", i)),
    },
    SourceLanguageExpression::VariableExpression {
      line_number: _,
      static_type: _,
      identifier,
    } => string_builder.push_str(&format!(
      "mem[{:}]",
      *variable_replacement_map.get(identifier).unwrap()
    )),
    SourceLanguageExpression::FunctionCallExpression {
      line_number: _,
      static_type,
      function_name,
      function_arguments,
    } => {
      if *static_type == ExpressionStaticType::VoidType {
        string_builder.push(' ');
      }
      if function_name == "memsize" {
        string_builder.push_str("MEMSIZE");
      } else if function_name == "defense" {
        string_builder.push_str("DEFENSE");
      } else if function_name == "offense" {
        string_builder.push_str("OFFENSE");
      } else if function_name == "size" {
        string_builder.push_str("SIZE");
      } else if function_name == "energy" {
        string_builder.push_str("ENERGY");
      } else if function_name == "pass" {
        string_builder.push_str("PASS");
      } else if function_name == "posture" {
        string_builder.push_str("POSTURE");
      } else if function_name == "wait"
        || function_name == "forward"
        || function_name == "backward"
        || function_name == "left"
        || function_name == "right"
        || function_name == "eat"
        || function_name == "attack"
        || function_name == "grow"
        || function_name == "bud"
        || function_name == "mate"
        || function_name == "smell"
      {
        string_builder.push_str(&function_name);
      } else if function_name == "serve"
        || function_name == "nearby"
        || function_name == "ahead"
        || function_name == "random"
      {
        string_builder.push_str(&format!("{:}[", function_name));
        pretty_print(
          &function_arguments[0],
          string_builder,
          variable_replacement_map,
        );
        string_builder.push(']');
      } else {
        panic!(format!("Unknown function {:}", function_name));
      }
    }
    SourceLanguageExpression::BinaryExpression {
      line_number: _,
      static_type: _,
      operator,
      e1,
      e2,
    } => {
      let is_condition = *operator == BinaryOperator::AND || *operator == BinaryOperator::OR;
      string_builder.push(if is_condition { '{' } else { '(' });
      pretty_print(e1, string_builder, variable_replacement_map);
      string_builder.push(' ');
      match operator {
        BinaryOperator::MUL => string_builder.push('*'),
        BinaryOperator::DIV => string_builder.push('/'),
        BinaryOperator::MOD => string_builder.push_str("mod"),
        BinaryOperator::PLUS => string_builder.push('+'),
        BinaryOperator::MINUS => string_builder.push('-'),

        BinaryOperator::LT => string_builder.push('<'),
        BinaryOperator::GT => string_builder.push('>'),
        BinaryOperator::LE => string_builder.push_str("<="),
        BinaryOperator::GE => string_builder.push_str(">="),
        BinaryOperator::EQ => string_builder.push('='),
        BinaryOperator::NE => string_builder.push_str("!="),

        BinaryOperator::AND => string_builder.push_str("and"),
        BinaryOperator::OR => string_builder.push_str("or"),
      }
      string_builder.push(' ');
      pretty_print(e2, string_builder, variable_replacement_map);
      string_builder.push(if is_condition { '}' } else { ')' });
    }
    SourceLanguageExpression::IfElseExpression {
      line_number: _,
      static_type: _,
      condition: _,
      e1: _,
      e2: _,
    } => panic!("should not be here!"),
    SourceLanguageExpression::AssignmentExpression {
      line_number: _,
      static_type: _,
      identifier,
      assigned_expression,
    } => {
      string_builder.push_str(&format!(
        " mem[{:}] := ",
        *variable_replacement_map.get(identifier).unwrap()
      ));
      pretty_print(
        assigned_expression,
        string_builder,
        variable_replacement_map,
      );
    }
    SourceLanguageExpression::ChainExpression {
      line_number: _,
      static_type: _,
      expressions,
    } => {
      for sub_expression in expressions {
        pretty_print(sub_expression, string_builder, variable_replacement_map);
      }
    }
  }
}

pub fn compile_to_critter_lang(program: &SourceLanguageProgram, inline_depth: usize) -> String {
  let FullyInlinedProgram {
    global_variable_definitions,
    if_else_blocks,
  } = &*program_inline(program, inline_depth);

  let mut string_builder = String::new();
  string_builder.push_str("mem[8] = 0 --> mem[8] := 1");

  let mut variable_replacement_map = HashMap::new();
  let mut variable_counter = 9;
  for global_variable_definition in global_variable_definitions {
    variable_replacement_map.insert(
      global_variable_definition.identifier.clone(),
      variable_counter,
    );
    string_builder.push_str(&format!(
      " mem[{:}] := {:}",
      variable_counter, global_variable_definition.assigned_value
    ));
    variable_counter += 1;
  }
  string_builder.push_str(";\n");

  for if_else_block in if_else_blocks {
    let IfElseBlock { condition, action } = &*if_else_block;
    pretty_print(condition, &mut string_builder, &variable_replacement_map);
    string_builder.push_str(" -->");
    pretty_print(action, &mut string_builder, &variable_replacement_map);
    string_builder.push_str(";\n");
  }

  string_builder
}
