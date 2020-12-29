mod ast;
#[rustfmt::skip]
mod pl;
mod checker;
mod compiler;
mod evaluator;
mod inliner;
mod renamer;
mod runtime;
mod transformer;

use std::io::{self, Read};

fn main() {
  let mut program_buffer = String::new();
  match io::stdin().read_to_string(&mut program_buffer) {
    Ok(_) => (),
    Err(e) => panic!(e),
  }
  match checker::get_type_checked_program(runtime::get_critter_world_runtime(), program_buffer) {
    Ok(program) => {
      println!("{:}", compiler::compile_to_critter_lang(&program, 20));
    }
    Err(errors) => {
      println!("Errors:");
      for e in errors {
        println!("{:}", e);
      }
    }
  }
}
