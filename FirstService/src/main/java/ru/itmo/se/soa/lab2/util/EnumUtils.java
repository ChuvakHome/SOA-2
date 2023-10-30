package ru.itmo.se.soa.lab2.util;

public final class EnumUtils {
	private EnumUtils() {}
	
	public static<T extends Enum<T>> boolean isEnumConstant(Class<T> enumType, String constantName) {
		if (enumType == null)
			throw new NullPointerException("Enum type cannot be null");
		
		if (constantName == null)
			throw new NullPointerException("Enum constant name cannot be null");
		
		try {
			Enum.valueOf(enumType, constantName);
			
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	public static<T extends Enum<T>> T toEnum(Class<T> enumType, String constantName) {
		return isEnumConstant(enumType, constantName) ? Enum.valueOf(enumType, constantName) : null;
	}
	
	public static<T extends Enum<T>> boolean isCorrectEnumOrdinal(Class<T> enumType, int ordinal) {		
		return ordinal >= 0 && ordinal < enumType.getEnumConstants().length;
	}
	
	public static<T extends Enum<T>> T toEnum(Class<T> enumType, int ordinal) {
		return isCorrectEnumOrdinal(enumType, ordinal) ? enumType.getEnumConstants()[ordinal] : null;
	}
}
