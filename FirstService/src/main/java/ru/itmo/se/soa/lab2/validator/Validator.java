package ru.itmo.se.soa.lab2.validator;

public interface Validator<T> {
	void validate(T t) throws ValidationException;
}
