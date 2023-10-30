package ru.itmo.se.soa.lab2.validator;

import lombok.Getter;

@Getter
public class SortValidationException extends Exception {
	private static final long serialVersionUID = -60135109388524476L;
	
	private String fieldName;
	
	public SortValidationException(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public SortValidationException(String fieldName, String message) {
		super(message);
		
		this.fieldName = fieldName;
	}
}
