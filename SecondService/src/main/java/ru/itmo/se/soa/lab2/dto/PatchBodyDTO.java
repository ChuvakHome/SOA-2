package ru.itmo.se.soa.lab2.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PatchBodyDTO implements Serializable {
	private static final long serialVersionUID = 2281342381143142192L;
	
	private List<FieldPatch> fields;
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	public static class FieldPatch implements Serializable {
		private static final long serialVersionUID = -2383449874656146487L;
		
		@JsonProperty("field-name")
		String fieldName;
		Object value;
	}
}
