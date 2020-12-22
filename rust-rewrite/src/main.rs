mod ast;
#[rustfmt::skip]
mod pl;
mod parser;

fn main() {
  match parser::parse_program("fun main(): void = {}") {
    Ok(p) => {
      println!("{:?}", p);
    }
    Err(e) => {
      println!("{:}", e);
    }
  }
}
