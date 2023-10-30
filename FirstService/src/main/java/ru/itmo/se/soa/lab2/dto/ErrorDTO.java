package ru.itmo.se.soa.lab2.dto;

import jakarta.json.bind.annotation.JsonbProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ErrorDTO {
	@JsonbProperty("http-status-code")
	private int httpStatusCode;
	
	@JsonbProperty("http-error")
	private String httpError;
	
	@JsonbProperty("error-message")
	private String errorMessage;
	
	@JsonbProperty("error-timestamp")
	private String errorTimestamp;
}
