package ru.itmo.se.soa.lab2.validator;

public class AscDescSortOrderException extends SortValidationException {
	private static final long serialVersionUID = 7838376879777669999L;

	public AscDescSortOrderException(String fieldName) {
		super(fieldName, String.format("Field \"%s\" has ascending and descending order", fieldName));
	}
}
