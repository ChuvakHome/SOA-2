package ru.itmo.se.soa.lab2.dto;

import java.util.List;

import jakarta.json.bind.annotation.JsonbProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PatchBodyDTO {
	private List<FieldPatch> fields;
	
	@NoArgsConstructor
	@Data
	public static class FieldPatch {
		@JsonbProperty("field-name")
		String fieldName;
		Object value;
	}
}
