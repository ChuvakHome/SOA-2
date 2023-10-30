package ru.itmo.se.soa.lab2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.itmo.se.soa.lab2.service.RestCallResult;
import ru.itmo.se.soa.lab2.service.RestService;

@RestController
@RequestMapping("/api/v1/shop")
public class ShopController {
	@Autowired
	private RestService restService;
	
	@PutMapping("/fix-distance/{vehicle-id}")
	public ResponseEntity<?> fixDistance(@PathVariable("vehicle-id") Long vehicleId) {
		if (vehicleId.longValue() < 0)
			return ResponseEntity.badRequest().build();
		
		RestCallResult result = restService.resetDistanceTravelled(vehicleId);
		
		switch (result) {
			case SUCCESSFULL_OPERATION:
				return ResponseEntity.ok().build();
			case NOT_FOUND:
				return ResponseEntity.notFound().build();
			case SERVER_ERROR:
			default:
				return ResponseEntity.internalServerError().build();
		}
	}
	
	@PutMapping("/add-wheels/{vehicle-id}/number-of-wheels")
	public ResponseEntity<?> addWheels(@PathVariable("vehicle-id") Long vehicleId, @RequestParam("wheels") int wheels) {
		if (vehicleId.longValue() <= 0 || wheels <= 0)
			return ResponseEntity.badRequest().build();
		
		RestCallResult result = restService.addWheels(vehicleId, wheels);
		
		switch (result) {
			case SUCCESSFULL_OPERATION:
				return ResponseEntity.ok().build();
			case NOT_FOUND:
				return ResponseEntity.notFound().build();
			case SERVER_ERROR:
			default:
				return ResponseEntity.internalServerError().build();
		}
	}
}
