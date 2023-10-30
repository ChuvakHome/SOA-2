package ru.itmo.se.soa.lab2.parser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ru.itmo.se.soa.lab2.model.FuelType;

public class Lexer {
	public static final String DATE_FORMAT = "$yyyy-MM-dd";
	
	private static Set<String> fieldNames = Set.of(
			"id", "name", 
			"coordinates.x", "coordinates.y", 
			"creationDate", "enginePower", 
			"numberOfWheels", "distanceTravelled", "fuelType"
	);
	private static Set<String> comparisonOperators = Set.of(
			"=", "!=",
			">", ">=",
			"<", "<="
	);
	private static Set<String> unaryLogicOrerators = Set.of("not");
	private static Set<String> binaryLogicOrerators = Set.of(
			"and", "or"
	);
	
	private static boolean isNumber(String s) {
		if (s.isEmpty())
			return false;
		
		if (s.contains(".")) {
			 if (s.endsWith("f"))
				 return false;
			 
			 try {
				 Double.valueOf(s);
				 
				 return true;
			 } catch (NumberFormatException e) {
				return false;
			}
		}
		else if (!s.startsWith("+")) {
			try {
				 Integer.valueOf(s);
				 
				 return true;
			 } catch (NumberFormatException e) {
				return false;
			}
		}
		else
			return false;
	}
	
	public static boolean isDateTime(String s) {
		try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            df.parse(s);
            
            return true;
        } catch (ParseException e) {
            return false;
        }
	}
	
	private static boolean isEnum(String s) {
		try {
			return s.charAt(0) == '@' && FuelType.valueOf(s.substring(1)) != null;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	static LexemeType calcLexemeType(String t) {
		if (t.equals("("))
			return LexemeType.LEXEME_LEFT_PARENTHESIS;
		else if (t.equals(")"))
			return LexemeType.LEXEME_RIGHT_PARENTHESIS;
		else if (unaryLogicOrerators.contains(t))
			return LexemeType.LEXEME_UNARY_LOGICAL_OPERATOR;
		else if (binaryLogicOrerators.contains(t))
			return LexemeType.LEXEME_BINARY_LOGICAL_OPERATOR;
		else if (comparisonOperators.contains(t))
			return LexemeType.LEXEME_COMPARISON_OPERATOR;
		else if (fieldNames.contains(t))
			return LexemeType.LEXEME_FIELD;
		else if (isNumber(t))
			return LexemeType.LEXEME_NUMBER;
		else if (isEnum(t))
			return LexemeType.LEXEME_ENUM;
		else if (isDateTime(t))
			return LexemeType.LEXEME_DATE;
		else if (t.chars().allMatch(c -> Character.isAlphabetic(c) || Character.isDigit(c) || c == '_'))
			return LexemeType.LEXEME_STRING;
		else
			return LexemeType.LEXEME_UNDEFINED;
			
	}
	
	private static void throwIf(boolean cond, QueryLexException e) throws QueryLexException {
		if (cond)
			throw e;
	}
	
	private static enum LexerState {
		INIT,
		NUMBER,
		DATE_VALUE,
		ENUM_VALUE,
		STRING_CONSTANT,
		COMPARISON_OPERATOR
	}
	
	public static Lexeme[] lex(String query) throws QueryLexException {
		
		
		LexerState currentState = LexerState.INIT;
		
		String s = "";
		
		int i = 0;
		int j = i;
		
		List<Lexeme> lexemeList = new ArrayList<>();
		
		while (i < query.length()) {
			LexerState previousState = currentState; 
			
			char c = query.charAt(i);
			
			switch (currentState) {
				case INIT:
					switch (c) {
						case '>':
						case '<':
						case '!':
						case '=':
							s = "" + c;
							currentState = LexerState.COMPARISON_OPERATOR;
							break;
						case '-':
							s = "" + c;
							currentState = LexerState.NUMBER;
							break;
						case '@':
							s += c;
							currentState = LexerState.ENUM_VALUE;
							break;
						case '$':
							s += c;
							currentState = LexerState.DATE_VALUE;
							break;
						case '(':
						case ')':
							lexemeList.add(new Lexeme(s + c, i));
							j++;
							break;
						default:
							if (Character.isDigit(c)) {
								s = "" + c;
								currentState = LexerState.NUMBER;
							}
							else if (Character.isAlphabetic(c)) {
								s += c;
								currentState = LexerState.STRING_CONSTANT;
							}
							else
								throw new QueryLexException(query, j);
								
							break;
					}
					break;
				case DATE_VALUE:
					if (Character.isDigit(c) || c == '-')
						s += c;
					else
						currentState = LexerState.INIT;
					break;
				case ENUM_VALUE:
					if (Character.isAlphabetic(c) && Character.isUpperCase(c))
						s += c;
					else
						currentState = LexerState.INIT;
					break;
				case NUMBER:
					if (Character.isDigit(c) || c == '.')
						s += c;
					else
						currentState = LexerState.INIT;
					break;
				case STRING_CONSTANT:
					if (Character.isAlphabetic(c) || Character.isDigit(c) || c == '_' || c == '.')
						s += c;
					else {
						if (calcLexemeType(s) == LexemeType.LEXEME_UNDEFINED)
							throw new QueryLexException(query, j);
						
						currentState = LexerState.INIT; 
					}
					break;
				case COMPARISON_OPERATOR:
					if (c == '=')
						s += c;
					else {
						if (!comparisonOperators.contains(s))
							throw new QueryLexException(query, j);
						
						currentState = LexerState.INIT;
					}
					break;
			}
			
			if (currentState != previousState && previousState != LexerState.INIT) {
				lexemeList.add(new Lexeme(s, j));
				s = "";
				j = i;
			}
			else
				++i;
		}
				
		if (!s.isEmpty()) {
			LexemeType lexemType = calcLexemeType(s); 
			
			switch (currentState) {
				case NUMBER:
					throwIf(lexemType != LexemeType.LEXEME_NUMBER, new QueryLexException(query, j));
					break;
				case STRING_CONSTANT:
					switch (lexemType) {
						case LEXEME_UNARY_LOGICAL_OPERATOR:
						case LEXEME_BINARY_LOGICAL_OPERATOR:
						case LEXEME_FIELD:
						case LEXEME_STRING:
							break;
						default:
							throw new QueryLexException(query, j);
					}
					
					break;
				case DATE_VALUE:
					break;
				case ENUM_VALUE:
					throwIf(lexemType != LexemeType.LEXEME_ENUM, new QueryLexException(query, j));
					break;
				case COMPARISON_OPERATOR:
					throwIf(lexemType != LexemeType.LEXEME_COMPARISON_OPERATOR, new QueryLexException(query, j));
					break;
				default:
					throw new QueryLexException(query, j);
			}
			
			lexemeList.add(new Lexeme(s, j));
		}
		
		return lexemeList.toArray(Lexeme[]::new);
	}
	
	public static enum LexemeType {
		LEXEME_FIELD,
		LEXEME_NUMBER,
		LEXEME_DATE,
		LEXEME_ENUM,
		LEXEME_STRING,
		LEXEME_UNARY_LOGICAL_OPERATOR,
		LEXEME_BINARY_LOGICAL_OPERATOR,
		LEXEME_COMPARISON_OPERATOR,
		LEXEME_LEFT_PARENTHESIS,
		LEXEME_RIGHT_PARENTHESIS,
		LEXEME_UNDEFINED
	}
	
	public static class Lexeme {
		private final String lexemeString;

		private final int lexemePosition;
		private final LexemeType lexemeType;
		
		private Lexeme(String lexemeString, int lexemePosition, LexemeType lexemeType) {
			this.lexemeString = lexemeString;
			this.lexemePosition = lexemePosition;
			this.lexemeType = lexemeType;
		}
		
		private Lexeme(String lexemString, int lexemPosition) {
			this(lexemString, lexemPosition, calcLexemeType(lexemString));
		}
		
		public String getLexemeString() {
			return lexemeString;
		}

		public int getLexemePosition() {
			return lexemePosition;
		}

		public LexemeType getLexemeType() {
			return lexemeType;
		}
		
		public String toString() {
			return String.format("Lexeme['%s', %d, %s]", lexemeString, lexemePosition, lexemeType.name());
		}
	}
}
