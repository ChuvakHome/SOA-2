package ru.itmo.se.soa.lab2.util;

import java.util.HashMap;
import java.util.Map;

import ru.itmo.se.soa.lab2.service.Sort;
import ru.itmo.se.soa.lab2.service.Sort.SortOrder;
import ru.itmo.se.soa.lab2.validator.AscDescSortOrderException;
import ru.itmo.se.soa.lab2.validator.SortFieldNotExistException;


public final class SortUtils {
	private SortUtils() {}
	
	public static Sort[] toSorts(String[] sortArray) throws AscDescSortOrderException, SortFieldNotExistException {
		if (sortArray == null)
			throw new NullPointerException("Array of sort string cannot be null");
		
		Sort[] sorts = new Sort[sortArray.length];
		
		int i = 0;
		
		Map<String, SortOrder> fieldSortOrder = new HashMap<>();
		
		for (String sortString: sortArray) {
			if (sortString == null)
				throw new NullPointerException(String.format("Sort string (index: %d) is null", i)); 
			
			SortOrder sortOrder = SortOrder.ASCENDING;
			String sortField;
			
			if (sortString.startsWith("-")) {
				sortOrder = SortOrder.DESCENDING;
				sortField = sortString.substring(1); 
			}
			else
				sortField = sortString;
			
			if (EntityVehicleUtils.VEHICLE_FIELDS.contains(sortField)) {
				SortOrder prevOrder = fieldSortOrder.put(sortField, sortOrder);
				
				if (prevOrder != null && sortOrder != prevOrder)
					throw new AscDescSortOrderException(sortField);
				else
					sorts[i++] = new Sort(sortField, sortOrder);
			}
			else
				throw new SortFieldNotExistException(sortField);
		}
		
		return sorts;
	}
}
