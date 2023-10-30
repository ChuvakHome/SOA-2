package ru.itmo.se.soa.lab2.parser;

import java.util.Map;

import ru.itmo.se.soa.lab2.parser.Lexer.Lexeme;
import ru.itmo.se.soa.lab2.parser.Lexer.LexemeType;

public class ExpressionASTNode extends ASTNode {
	private static Map<LexemeType, ASTNodeSubtype> nodeSubtypeByLexeme = Map.of(
		LexemeType.LEXEME_FIELD, ASTNodeSubtype.NODE_FIELD,
		LexemeType.LEXEME_NUMBER, ASTNodeSubtype.NODE_NUMBER,
		LexemeType.LEXEME_ENUM, ASTNodeSubtype.NODE_ENUM,
		LexemeType.LEXEME_DATE, ASTNodeSubtype.NODE_DATE,
		LexemeType.LEXEME_STRING, ASTNodeSubtype.NODE_STRING
	);
	
	ExpressionASTNode(Lexeme lexeme) {
		super(lexeme.getLexemeString(), null, ASTNodeType.EXPRESSION, nodeSubtypeByLexeme.get(lexeme.getLexemeType()));
	}
}
