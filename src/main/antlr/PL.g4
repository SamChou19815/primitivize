grammar PL;

import PLLexerPart;

program
    : variableDeclaration*
      functionDeclaration*
      FUN MAIN LPAREN RPAREN COLON VOID ASSIGN expression
      EOF;

variableDeclaration: VAR LowerIdentifier ASSIGN expression;

functionDeclaration :
    FUN LowerIdentifier
    (LPAREN RPAREN | LPAREN LowerIdentifier COLON type (COMMA LowerIdentifier COLON type)* RPAREN)
    COLON type ASSIGN expression;

expression
    : LPAREN expression RPAREN # NestedExpr
    | literal # LiteralExpr
    | LowerIdentifier # IdentifierExpr
    | NOT expression # NotExpr
    | LowerIdentifier (LPAREN RPAREN | LPAREN expression (COMMA expression)* RPAREN) # FunAppExpr
    | expression factorOperator expression # FactorExpr
    | expression termOperator expression # TermExpr
    | expression comparisonOperator expression # ComparisonExpr
    | expression AND expression # ConjunctionExpr
    | expression OR expression # DisjunctionExpr
    | IF expression THEN expression ELSE expression # IfElseExpr
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

type : INT | BOOL | VOID ;
