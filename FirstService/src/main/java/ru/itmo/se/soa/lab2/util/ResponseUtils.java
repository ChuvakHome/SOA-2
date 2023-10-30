package ru.itmo.se.soa.lab2.util;

import java.time.LocalDateTime;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import ru.itmo.se.soa.lab2.dto.ErrorDTO;

public final class ResponseUtils {
	private ResponseUtils() {}
	
	public static ErrorDTO errorByStatus(Status status, String message) {
		return errorByStatus(status, message, LocalDateTime.now());
	}
	
	public static ErrorDTO errorByStatus(Status status, String message, LocalDateTime timestamp) {
		if (status == null)
			throw new NullPointerException("Status cannot be null");
		
		if (message == null)
			throw new NullPointerException("Message cannot be null");
		
		if (timestamp == null)
			throw new NullPointerException("Timestamp cannot be null");
		
		ErrorDTO dto = new ErrorDTO();
		
		dto.setHttpStatusCode(status.getStatusCode());
		dto.setHttpError(status.getReasonPhrase());
		dto.setErrorMessage(message);
		dto.setErrorTimestamp(timestamp.toString());
		
		return dto;
	}
	
	public static Response errorRespone(Status status, String message) {
		return Response.status(status).entity(errorByStatus(status, message)).build();
	}
	
	public static Response errorRespone(Status status, String message, LocalDateTime timestamp) {
		return Response.status(status).entity(errorByStatus(status, message, timestamp)).build();
	}
	
	public static Response emptyOKResponse() {
		return Response.ok("").build();
	}
	
	public static Response emptyBadRequestResponse() {
		return Response.status(Status.BAD_REQUEST).entity("").build();
	}
	
	public static Response emptyNotFoundResponse() {
		return Response.status(Status.NOT_FOUND).entity("").build();
	}
}
