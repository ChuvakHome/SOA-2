package ru.itmo.se.soa.lab2.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EntityUpdate<T> {
	private Class<? extends T> entityClass;
	private Map<String, Object> updateFieldsList;
	
	public EntityUpdate(Class<? extends T> entityClass) {
		updateFieldsList = new HashMap<>();
		this.entityClass = entityClass;
	}
	
	private void addUpdate(String prefix, String fieldName, Object value, Class<?> entityClass) throws NoSuchFieldException {
		if (fieldName == null)
			throw new NullPointerException("Field name cannot be null");
		
		Field field = null;
		
		if (fieldName.contains(".") && !fieldName.startsWith(".") && !fieldName.endsWith(".")) {
			String fieldNameParts[] = fieldName.split("\\.", 2);
			
			String rootFieldName = fieldNameParts[0];
			String targetFieldName = String.join(".", Arrays.copyOfRange(fieldNameParts, 1, fieldNameParts.length));
			
			try {
				field = entityClass.getDeclaredField(rootFieldName);
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			
			final Class<?> fieldType = field.getType();
			
			addUpdate(prefix == null ? rootFieldName : prefix + "." + rootFieldName, targetFieldName, value, fieldType);
		}
		else {
			try {
				field = entityClass.getDeclaredField(fieldName);
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			
			final Class<?> fieldType = field.getType();
				
			if (value instanceof Map) {
				Map<?, ?> map = (Map<?, ?>) value;
					
				map.forEach((k, v) -> {
					try {
						addUpdate(fieldName, k.toString(), v, fieldType);
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					}
				});
			}
			else {
				String fieldKey = prefix == null ? fieldName : prefix + "." + fieldName;
					
				if (fieldType == Integer.class) {
					if (value instanceof Number)
						updateFieldsList.put(fieldKey, Integer.valueOf(((Number) value).intValue()));
					else {
						Integer i = Integer.decode(value.toString());
						updateFieldsList.put(fieldKey, i);
					}
				}
				else if (fieldType == int.class) {
					if (value instanceof Number)
						updateFieldsList.put(fieldKey, ((Number) value).intValue());
					else {
						Integer i = Integer.decode(value.toString());
						updateFieldsList.put(fieldKey, i);
					}
				}
				else if (fieldType == Long.class) {
					if (value instanceof Number)
						updateFieldsList.put(fieldKey, Long.valueOf(((Number) value).longValue()));
					else { 
						Long l = Long.decode(value.toString());
						updateFieldsList.put(fieldKey, l);
					}
				}
				else if (fieldType == long.class) {
					if (value instanceof Number)
						updateFieldsList.put(fieldKey, ((Number) value).longValue());
					else {
						long l = Long.parseLong(value.toString());
						updateFieldsList.put(fieldKey, l);
					}
				}
				else if (fieldType == Float.class) {
					if (value instanceof Number)
						updateFieldsList.put(fieldKey, Float.valueOf(((Number) value).floatValue()));
					else {
						Float f = Float.parseFloat(value.toString());
						updateFieldsList.put(fieldKey, f);
					}
				}
				else if (fieldType == float.class) {
					if (value instanceof Number)
						updateFieldsList.put(fieldKey, ((Number) value).floatValue());
					else {
						float f = Float.parseFloat(value.toString());
						updateFieldsList.put(fieldKey, f);
					}
				}
				else if (fieldType == Double.class) {
					if (value instanceof Number)
						updateFieldsList.put(fieldKey, Double.valueOf(((Number) value).doubleValue()));
					else {
						Double d = Double.parseDouble(value.toString());
						updateFieldsList.put(fieldKey, d);
					}
				}
				else if (fieldType == double.class) {
					if (value instanceof Number)
						updateFieldsList.put(fieldKey, ((Number) value).doubleValue());
					else {
						double d = Double.parseDouble(value.toString());
						updateFieldsList.put(fieldKey, d);
					}
				}
				else if (fieldType == String.class)
					updateFieldsList.put(fieldKey, value.toString());
				else if (fieldType == LocalDateTime.class)
					updateFieldsList.put(fieldKey, LocalDateTime.parse(value.toString()));
				else if (fieldType.isEnum()) {
					if (value == null)
						updateFieldsList.put(fieldKey, null);
					else {
						Object[] enumConstants = fieldType.getEnumConstants();
						
						if (value instanceof Number)
							updateFieldsList.put(fieldKey, enumConstants[((Number) value).intValue()]);
						else if (value.getClass().isEnum()) {
							updateFieldsList.put(fieldKey, findEnumByName(enumConstants, ((Enum<?>) value).name()));
						}
						else {
							try {
								updateFieldsList.put(fieldKey, enumConstants[Integer.parseInt(value.toString())]);
							} catch (NumberFormatException e) {
								updateFieldsList.put(fieldKey, findEnumByName(enumConstants, value.toString()));
							}
						}
					}
				}
			}
		}
	}
	
	private static Enum<?> findEnumByName(Object[] enumConstants, String name) {
		for (int i = 0; i < enumConstants.length; ++i) {
			Enum<?> enumValue = (Enum<?>) enumConstants[i];
			
			if (enumValue.name().equals(name))
				return enumValue;
		}
		
		return null;
	}
	
	private static class FieldSearchResult {
		private final Field field;
		private final Object fieldHolder;
		
		private FieldSearchResult(Field field, Object fieldHolder) {
			this.field = field;
			this.fieldHolder = fieldHolder;
		}
	}
	
	private static<T extends Annotation> Method getMethodForField(Field field, Class<T> annotationClass, AnotatedMethodValidator<T> validator) {
		Class<?> clazz = field.getDeclaringClass();
		
		for (Method m: clazz.getMethods()) {
			if (m.isAnnotationPresent(annotationClass)) {
				if (validator.validate(m, m.getAnnotation(annotationClass))) {
					return m;
				}
			}
		}
		
		return null;
	}
	
	private static Method findSetterMethodFor(Field field) throws NoSuchMethodException {
		String fieldName = field.getName();
		
		String naiveSetterName = String.format("set%c%s", Character.toUpperCase(fieldName.charAt(0)), fieldName.substring(1));
				
		try {
			return field.getDeclaringClass().getMethod(naiveSetterName, field.getType());
		} catch (NoSuchMethodException e) {
			Method m = getMethodForField(field, SetterFor.class, (method, annot) -> {
				final int parameterCount = method.getParameterCount();
				final Class<?>[] parameterTypes = method.getParameterTypes();
				
				return annot.value().equals(fieldName) && parameterCount == 1 && parameterTypes[0] == field.getType();
			});
			
			if (m != null)
				return m;
		}
		
		throw new NoSuchMethodException();
	}
	
	private static Method findGetterMethodFor(Field field) throws NoSuchMethodException {
		String fieldName = field.getName();
		
		String naiveGetterName = String.format("get%c%s", Character.toUpperCase(fieldName.charAt(0)), fieldName.substring(1));
				
		try {
			return field.getDeclaringClass().getMethod(naiveGetterName);
		} catch (NoSuchMethodException e) {
			Method m = getMethodForField(field, GetterFor.class, (method, annot) -> {
				return annot.value().equals(fieldName) && method.getParameterCount() == 0;
			});
			
			if (m != null)
				return m;
		}
		
		throw new NoSuchMethodException();
	}
	
	private static FieldSearchResult getField(String fieldName, Object fieldHolder) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, UnreachableFieldException {
		String[] fieldNameParts = fieldName.split("\\.");
		
		Field field = null;
		Object holder = fieldHolder;
		Object prevHolder = holder;
		Class<?> c = fieldHolder.getClass();
		
		for (String fieldPart: fieldNameParts) {
			prevHolder = holder;
			
			field = c.getDeclaredField(fieldPart);
			c = field.getType();
			
			try {
				holder = findGetterMethodFor(field).invoke(holder);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				throw new UnreachableFieldException();
			}
		}
		
		return new FieldSearchResult(field, prevHolder);
	}
	
	private static interface AnotatedMethodValidator<T extends Annotation> {
		boolean validate(Method m, T annotation);
	}
	
	public void addUpdate(String fieldName, Object value) throws NoSuchFieldException {
		addUpdate(null, fieldName, value, entityClass);
	}
	
	public void applyTo(T t) {
		updateFieldsList.forEach((name, value) -> {
			try {
				FieldSearchResult searchResult = getField(name, t);
				
				Method m = findSetterMethodFor(searchResult.field);				
				m.invoke(searchResult.fieldHolder, value);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (UnreachableFieldException e) {
				e.printStackTrace();
			}
		});
	}
	
	public Map<String, Object> getUpdateMap() {
		return Collections.unmodifiableMap(updateFieldsList);
	}
}
