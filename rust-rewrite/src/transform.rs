use crate::ast::{FullyInlinedProgram, SourceLanguageProgram};
use crate::inliner::program_inline;
use crate::renamer::prefix_variable_names;

pub fn lower_program(
  program: Box<SourceLanguageProgram>,
  inline_depth: usize,
) -> Box<FullyInlinedProgram> {
  program_inline(prefix_variable_names(program, inline_depth))
}
