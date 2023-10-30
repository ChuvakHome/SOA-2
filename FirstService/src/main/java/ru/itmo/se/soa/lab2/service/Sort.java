package ru.itmo.se.soa.lab2.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Sort {
	private String fieldName;
	private SortOrder sortOrder;
	
	public static enum SortOrder {
		ASCENDING,
		DESCENDING
	}
}
