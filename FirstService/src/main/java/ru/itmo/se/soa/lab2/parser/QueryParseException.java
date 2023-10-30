package ru.itmo.se.soa.lab2.parser;


public class QueryParseException extends Exception {
    private static final long serialVersionUID = -9091183722282512377L;
    
    private int errorOffset;
    private String query;

	public QueryParseException(String s, int errorOffset) {
        super(s);
        
        this.query = s;
        this.errorOffset = errorOffset;
    }
	
	public QueryParseException(String message, String query, int errorOffset) {
        super(message);
        
        this.query = query;
        this.errorOffset = errorOffset;
    }

	public String getQuery() {
		return query;
	}
	
    public int getErrorOffset () {
        return errorOffset;
    }
}
