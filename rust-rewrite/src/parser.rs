use crate::ast::SourceLanguageProgram;
use crate::pl::SourceLanguageProgramParser;

pub fn parse_program(source_string: &'static str) -> Result<Box<SourceLanguageProgram>, String> {
  let generated_parser = SourceLanguageProgramParser::new();
  match generated_parser.parse(source_string) {
    Ok(program) => Ok(program),
    Err(e) => Err(format!("{:?}", e)),
  }
}
