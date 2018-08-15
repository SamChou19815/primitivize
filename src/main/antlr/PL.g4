grammar PL;

import PLLexerPart;

program
    : variableDeclaration*
      functionDeclaration*
      FUN MAIN LPAREN RPAREN COLON UNIT ASSIGN expression
      EOF;

variableDeclaration: VAR LowerIdentifier ASSIGN expression;

functionDeclaration : FUN LowerIdentifier LPAREN RPAREN typeAnnotation ASSIGN expression;

// Some parser type fragment
typeAnnotation : COLON type;

expression
    : LPAREN expression RPAREN # NestedExpr
    | literal # LiteralExpr
    | LowerIdentifier # IdentifierExpr
    | NOT expression # NotExpr
    | expression factorOperator expression # FactorExpr
    | expression termOperator expression # TermExpr
    | expression comparisonOperator expression # ComparisonExpr
    | expression AND expression # ConjunctionExpr
    | expression OR expression # DisjunctionExpr
    | IF expression THEN expression ELSE expression # IfElseExpr
    | LowerIdentifier LPAREN RPAREN # FunAppExpr
    | LowerIdentifier ASSIGN expression # AssignExpr
    | expression SEMICOLON expression # ChainExpr
    ;

// Operator collections

factorOperator : MUL | DIV | MOD;

termOperator : PLUS | MINUS;

comparisonOperator : LT | LE | GT | GE | EQ | NE;

// Literals

literal : IntegerLiteral | BooleanLiteral ;

// Types

type : INT | BOOL | ENUM | UNIT ;
