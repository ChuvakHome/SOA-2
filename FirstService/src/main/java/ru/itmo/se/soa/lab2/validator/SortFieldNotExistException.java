package ru.itmo.se.soa.lab2.validator;

public class SortFieldNotExistException extends SortValidationException {
	private static final long serialVersionUID = -3131181737246780124L;

	public SortFieldNotExistException(String fieldName) {
		super(fieldName, String.format("Field \"%s\" does not exist", fieldName));
	}
}
