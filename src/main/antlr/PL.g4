grammar PL;

import PLLexerPart;

program
    : variableDeclaration*
      functionDeclaration*
      FUN MAIN LPAREN RPAREN COLON VOID ASSIGN expression
      EOF;

variableDeclaration: VAR LowerIdentifier ASSIGN expression;

functionDeclaration :
    recursiveFunctionHeader? FUN LowerIdentifier
    (LPAREN RPAREN | LPAREN LowerIdentifier COLON exprType (COMMA LowerIdentifier COLON exprType)* RPAREN)
    COLON exprType ASSIGN expression;

recursiveFunctionHeader : RECURSIVE LPAREN IntegerLiteral COMMA expression RPAREN;

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

exprType : INT | BOOL | VOID ;
