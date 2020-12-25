mod ast;
#[rustfmt::skip]
mod pl;
mod checker;
mod lowering;
mod type_checker;
use im::HashMap;

fn main() {
  let program = "fun main(): int = 32 + 10".to_string();
  match checker::get_type_checked_program(HashMap::new(), program) {
    Ok(p) => {
      println!("Original: {:?}", p);
      println!("Lowered: {:?}", lowering::lower_program(p));
    }
    Err(e) => {
      println!("Errors: {:?}", e);
    }
  }
}
