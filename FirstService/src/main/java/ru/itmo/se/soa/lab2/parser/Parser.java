package ru.itmo.se.soa.lab2.parser;

import java.util.List;
import java.util.Map;

import ru.itmo.se.soa.lab2.parser.ASTNode.ASTNodeType;
import ru.itmo.se.soa.lab2.parser.Lexer.Lexeme;
import ru.itmo.se.soa.lab2.parser.Lexer.LexemeType;

public class Parser {
	private static Map<String, Integer> binlogopPriorityMap = Map.of(
			"or", 1,
			"and", 2
	);
	
	private static boolean isRightValue(Lexeme lexeme) {
		switch (lexeme.getLexemeType()) {
			case LEXEME_NUMBER:
			case LEXEME_ENUM:
			case LEXEME_DATE:
			case LEXEME_STRING:
				return true;
			default:
				return false;
		}
	}
	
	private static void attachChild(ASTNode parentNode, ASTNode childNode) {
		if (parentNode instanceof UnaryASTNode)
			((UnaryASTNode) parentNode).setChild(childNode);
		else if (parentNode instanceof BinaryASTNode) {
			BinaryASTNode binASTNode = (BinaryASTNode) parentNode;
			
			if (binASTNode.getLeftChild() == null)
				binASTNode.setLeftChild(childNode);
			else if (binASTNode.getRightChild() == null)
				binASTNode.setRightChild(childNode);
		}
	}
	
	private static Lexeme[] splitLexemes(String query) throws QueryParseException {
		if (query == null)
			throw new NullPointerException();
		
		Lexeme[] lexemes;
		
		try {
			lexemes = Lexer.lex(query);
		} catch (QueryLexException e) {
			throw new QueryParseException("Error parsing query", query, e.getErrorOffset());
		}
		
		final Lexeme lastLexeme = lexemes[lexemes.length - 1];
		
		if (!isRightValue(lastLexeme) && lastLexeme.getLexemeType() != LexemeType.LEXEME_RIGHT_PARENTHESIS)
			throw new QueryParseException("Wrong terminating lexeme", query, lastLexeme.getLexemePosition());
			
		int nestingLevel = 0;
		
		List<LexemeType> expectingLexemes = List.of(
				LexemeType.LEXEME_LEFT_PARENTHESIS,
				LexemeType.LEXEME_UNARY_LOGICAL_OPERATOR,
				LexemeType.LEXEME_FIELD
		);
		
		for (Lexeme lexeme: lexemes) {
			if (!expectingLexemes.contains(lexeme.getLexemeType()))
				throw new QueryParseException("Unexpected lexeme", query, lexeme.getLexemePosition());
			
			if (lexeme.getLexemeType() == LexemeType.LEXEME_LEFT_PARENTHESIS)
				++nestingLevel;
			else if (lexeme.getLexemeType() == LexemeType.LEXEME_RIGHT_PARENTHESIS)
				--nestingLevel;
			
			if (nestingLevel > 255)
				throw new NestingLevelQueryParseException("Too many left parenthesis", query, lexeme.getLexemePosition());
			else if (nestingLevel < 0)
				throw new NestingLevelQueryParseException("Too many right parenthesis", query, lexeme.getLexemePosition());
			
			if (lexeme.getLexemeType() == LexemeType.LEXEME_UNDEFINED)
				throw new QueryParseException("Undefined lexeme", query, lexeme.getLexemePosition());
			
			switch (lexeme.getLexemeType()) {
				case LEXEME_LEFT_PARENTHESIS:
					expectingLexemes = List.of(
						LexemeType.LEXEME_LEFT_PARENTHESIS,
						LexemeType.LEXEME_FIELD,
						LexemeType.LEXEME_UNARY_LOGICAL_OPERATOR
					);
					break;
				case LEXEME_RIGHT_PARENTHESIS:
					expectingLexemes = List.of(
							LexemeType.LEXEME_RIGHT_PARENTHESIS,
							LexemeType.LEXEME_BINARY_LOGICAL_OPERATOR
						);
						break;
				case LEXEME_UNARY_LOGICAL_OPERATOR:
				case LEXEME_BINARY_LOGICAL_OPERATOR:
					expectingLexemes = List.of(LexemeType.LEXEME_LEFT_PARENTHESIS);
					break;
				case LEXEME_NUMBER:
				case LEXEME_ENUM:
				case LEXEME_DATE:
				case LEXEME_STRING:
					expectingLexemes = List.of(LexemeType.LEXEME_RIGHT_PARENTHESIS);
					break;
				case LEXEME_FIELD:
					expectingLexemes = List.of(
							LexemeType.LEXEME_COMPARISON_OPERATOR,
							LexemeType.LEXEME_RIGHT_PARENTHESIS
					);
					break;
				case LEXEME_COMPARISON_OPERATOR:
					expectingLexemes = List.of(
							LexemeType.LEXEME_NUMBER,
							LexemeType.LEXEME_ENUM,
							LexemeType.LEXEME_DATE,
							LexemeType.LEXEME_STRING,
							LexemeType.LEXEME_FIELD
					);
					break;
				default:
					break;
			}
		}
		
		if (nestingLevel != 0)
			throw new NestingLevelQueryParseException("Not enough right parenthesis", query, query.length());
		
		return lexemes;
	}
	
	private static class ParseResult {
		private ASTNode node;
		private int offset;
		
		private ParseResult(ASTNode resultNode, int offset) {
			this.node = resultNode;
			this.offset = offset;
		}
	}
	
	private static ParseResult parse0(final Lexeme[] lexemes, int start) {
		ASTNode rootASTNode = new UnaryASTNode(null, null, ASTNodeType.EMPTY_NODE, null);
		ASTNode currentNode = rootASTNode;
		
		int i = start;
		
		while (i < lexemes.length) {
			Lexeme lexeme = lexemes[i++];
			
			if (lexeme.getLexemeType() == LexemeType.LEXEME_LEFT_PARENTHESIS) {
				ParseResult res = parse0(lexemes, i);
				i += res.offset;
				
				if (currentNode.getNodeType() == ASTNodeType.EMPTY_NODE) {
					if (i < lexemes.length) {
						Lexeme nextLexeme = lexemes[i++];
						
						if (nextLexeme.getLexemeType() == LexemeType.LEXEME_BINARY_LOGICAL_OPERATOR)
							currentNode = new BinaryLogicalOperatorASTNode(nextLexeme.getLexemeString(), res.node, null);
						else if (nextLexeme.getLexemeType() == LexemeType.LEXEME_UNARY_LOGICAL_OPERATOR)
							currentNode = new UnaryLogicalOperatorASTNode(nextLexeme.getLexemeString(), res.node);
					}
					else
						rootASTNode = currentNode = res.node;
				}
				else {
					attachChild(currentNode, res.node);
					
					if (rootASTNode.getNodeType() == ASTNodeType.EMPTY_NODE)
						rootASTNode = currentNode;
				}
			}
			else if (lexeme.getLexemeType() == LexemeType.LEXEME_RIGHT_PARENTHESIS)
				return new ParseResult(rootASTNode, i - start);
			else if (currentNode instanceof BinaryLogicalOperatorASTNode) {
				BinaryLogicalOperatorASTNode binopNode = (BinaryLogicalOperatorASTNode) currentNode;
				
				if (lexeme.getLexemeType() == LexemeType.LEXEME_BINARY_LOGICAL_OPERATOR) {
					if (binopNode.getPriority() < binlogopPriorityMap.get(lexeme.getLexemeString())) {
						BinaryLogicalOperatorASTNode newBinLogOpASTNode = new BinaryLogicalOperatorASTNode(lexeme.getLexemeString(), binopNode.getRightChild(), null);
						binopNode.setRightChild(newBinLogOpASTNode);
						currentNode = newBinLogOpASTNode;
					}
					else {
						BinaryLogicalOperatorASTNode newBinLogOpASTNode = new BinaryLogicalOperatorASTNode(lexeme.getLexemeString(), binopNode, null);
						
						if (rootASTNode == currentNode)
							rootASTNode = currentNode = newBinLogOpASTNode;
						else
							currentNode = newBinLogOpASTNode;
					}
				}
			}
			else if (currentNode instanceof BinaryComparisonOperatorASTNode) {
				if (lexeme.getLexemeType() == LexemeType.LEXEME_BINARY_LOGICAL_OPERATOR) {					
					BinaryLogicalOperatorASTNode binopNode = new BinaryLogicalOperatorASTNode(lexeme.getLexemeString(), currentNode, null);
					currentNode = binopNode;
				}
			}
			else if (lexeme.getLexemeType() == LexemeType.LEXEME_UNARY_LOGICAL_OPERATOR) {
				ParseResult parseResult = parse0(lexemes, i + 1);
				
				i += parseResult.offset + 1;
				
				ASTNode newNode = new UnaryLogicalOperatorASTNode(lexeme.getLexemeString(), parseResult.node); // we're sure next lexeme is left parenthesis, otherwise we won't be here
				
				if (currentNode.getNodeType() == ASTNodeType.EMPTY_NODE)
					rootASTNode = currentNode = newNode;
				else
					attachChild(currentNode, newNode);
			}
			else if (lexeme.getLexemeType() == LexemeType.LEXEME_FIELD) {
				Lexeme fieldLexeme = lexeme;
				Lexeme comparisonOperatorLexeme = lexemes[i];
				Lexeme rightValueLexeme = lexemes[i + 1];
				
				i += 1;
				
				ASTNode exprNode = new BinaryComparisonOperatorASTNode(comparisonOperatorLexeme.getLexemeString(), 
						new ExpressionASTNode(fieldLexeme), 
						new ExpressionASTNode(rightValueLexeme));
				
				if (currentNode.getNodeType() == ASTNodeType.EMPTY_NODE)
					rootASTNode = currentNode = exprNode;
				else
					attachChild(currentNode, exprNode);
			}
		}
		
		return new ParseResult(rootASTNode, i - start);
	}
	
	
	public static void validateQuery(String query) throws QueryParseException, NestingLevelQueryParseException {
		splitLexemes(query);
	}
	
	public static ASTNode parse(String query) throws QueryParseException, NestingLevelQueryParseException {
		return parse0(splitLexemes(query), 0).node;
	}
}
