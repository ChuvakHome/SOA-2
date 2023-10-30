package ru.itmo.se.soa.lab2.service;

public class Pagination {
	private final int pageNumber;
	private final int pageSize;
	
	public Pagination(int pageNumber, int pageSize) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}
}
