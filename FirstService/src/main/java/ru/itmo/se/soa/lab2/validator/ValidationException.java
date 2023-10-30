package ru.itmo.se.soa.lab2.validator;

public class ValidationException extends Exception {
	private static final long serialVersionUID = 4642472781543577275L;
	
	private final String validationMessage;
	
	public ValidationException(String validationMessage) {
		super(String.format("Validation failed because %s", validationMessage));
		
		this.validationMessage = validationMessage;
	}
	
	public String getValidationMessage() {
		return validationMessage;
	}
}
