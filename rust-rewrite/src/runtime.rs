use crate::ast::{ExpressionStaticType, FunctionType};
use im::{hashmap, HashMap};

pub fn get_critter_world_runtime() -> HashMap<String, FunctionType> {
  hashmap! {
    String::from("memsize") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::IntType },
    String::from("defense") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::IntType },
    String::from("offense") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::IntType },
    String::from("size") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::IntType },
    String::from("energy") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::IntType },
    String::from("pass") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::IntType },
    String::from("posture") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::IntType },

    String::from("waitFor") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::VoidType },
    String::from("forward") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::VoidType },
    String::from("backward") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::VoidType },
    String::from("left") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::VoidType },
    String::from("right") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::VoidType },
    String::from("eat") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::VoidType },
    String::from("attack") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::VoidType },
    String::from("eat") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::VoidType },
    String::from("attack") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::VoidType },
    String::from("grow") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::VoidType },
    String::from("bud") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::VoidType },
    String::from("mate") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::VoidType },
    String::from("serve") => FunctionType { argument_types: vec![ExpressionStaticType::IntType], return_type: ExpressionStaticType::VoidType },
    String::from("bud") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::VoidType },

    String::from("nearby") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::IntType },
    String::from("ahead") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::IntType },
    String::from("random") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::IntType },
    String::from("smell") => FunctionType { argument_types: vec![], return_type: ExpressionStaticType::IntType },
  }
}
