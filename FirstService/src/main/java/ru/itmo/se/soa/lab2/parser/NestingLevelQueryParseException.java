package ru.itmo.se.soa.lab2.parser;

public class NestingLevelQueryParseException extends QueryParseException {
	private static final long serialVersionUID = 7000234489272904816L;

	public NestingLevelQueryParseException(String s) {
		this(s, s.length());
	}
	
	public NestingLevelQueryParseException(String s, int errorOffset) {
		super(s, errorOffset);
	}
	
	public NestingLevelQueryParseException(String message, String query, int errorOffset) {
        super(message, query, errorOffset);
    }
}
