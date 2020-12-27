mod ast;
#[rustfmt::skip]
mod pl;
mod checker;
mod evaluator;
mod inliner;
mod renamer;
mod runtime;
mod transformer;

fn main() {
  let program =
    "var a = 4; fun b(v: int): int = v fun main(): void = if true then serve(b(37) + 1 + a) else ({})"
      .to_string();
  match checker::get_type_checked_program(runtime::get_critter_world_runtime(), program) {
    Ok(p) => {
      println!("Original: {:?}\n\n", p);
      println!("Lowered: {:?}", inliner::program_inline(p, 20));
    }
    Err(e) => {
      println!("Errors: {:?}", e);
    }
  }
}
