mod ast;
#[rustfmt::skip]
mod pl;
mod checker;
mod evaluator;
mod inliner;
mod renamer;
mod transformer;
use im::HashMap;

fn main() {
  let program =
    "var a = 4; fun b(v: int): int = v fun main(): void = if true then print(b(37) + 1 + a) else ({})"
      .to_string();
  match checker::get_type_checked_program(
    HashMap::new().update(
      "print".to_string(),
      ast::FunctionType {
        argument_types: vec![ast::ExpressionStaticType::IntType],
        return_type: ast::ExpressionStaticType::VoidType,
      },
    ),
    program,
  ) {
    Ok(p) => {
      println!("Original: {:?}\n\n", p);
      println!("Lowered: {:?}", inliner::program_inline(p, 20));
    }
    Err(e) => {
      println!("Errors: {:?}", e);
    }
  }
}
