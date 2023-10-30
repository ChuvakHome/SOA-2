package ru.itmo.se.soa.lab2.util;

import java.util.Set;

public final class EntityVehicleUtils {
	private EntityVehicleUtils() {}
	
	public static final Set<String> VEHICLE_FIELDS = Set.of(
			"id", "name", 
			"coordinates.x", "coordinates.y", 
			"creationDate", "enginePower", 
			"numberOfWheels", "distanceTravelled", "fuelType"
	);
} 