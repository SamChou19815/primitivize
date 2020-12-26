mod ast;
#[rustfmt::skip]
mod pl;
mod checker;
mod inliner;
mod renamer;
mod transform;
use im::HashMap;

fn main() {
  let program = "var a = 4; fun b(v: int): int = v fun main(): int = b(38) + a".to_string();
  match checker::get_type_checked_program(HashMap::new(), program) {
    Ok(p) => {
      println!("Original: {:?}", p);
      println!("Lowered: {:?}", transform::lower_program(p));
    }
    Err(e) => {
      println!("Errors: {:?}", e);
    }
  }
}
