mod ast;
#[rustfmt::skip]
mod pl;
mod parser;
mod type_checker;
use im::HashMap;

fn main() {
  let program = "fun main(): int = 32 + 10";
  match parser::parse_program(program) {
    Ok(p) => {
      let (checked_program, errors) = type_checker::type_check_program(HashMap::new(), p);
      println!("{:?}\nErrors: {:?}", checked_program, errors);
    }
    Err(e) => {
      println!("{:}", e);
    }
  }
}
