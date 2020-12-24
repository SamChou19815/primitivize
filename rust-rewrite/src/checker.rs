use crate::ast::{FunctionType, SourceLanguageProgram};
use crate::pl::SourceLanguageProgramParser;
use crate::type_checker::type_check_program;
use im::HashMap;

pub fn get_type_checked_program(
  functions_environment: HashMap<String, FunctionType>,
  source_string: String,
) -> Result<Box<SourceLanguageProgram>, Vec<String>> {
  let generated_parser = SourceLanguageProgramParser::new();
  match generated_parser.parse(source_string.as_str()) {
    Ok(program) => {
      let (checked_program, errors) = type_check_program(functions_environment, program);
      if errors.len() > 0 {
        Err(errors)
      } else {
        Ok(checked_program)
      }
    }
    Err(e) => Err(vec![format!("{:}", e)]),
  }
}
