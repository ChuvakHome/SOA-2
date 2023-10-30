package ru.itmo.se.soa.lab2.service;

import java.util.List;
import java.util.Optional;

import ru.itmo.se.soa.lab2.entity.VehicleEntity;
import ru.itmo.se.soa.lab2.model.FuelType;
import ru.itmo.se.soa.lab2.reflection.EntityUpdate;

public interface VehicleService {
	List<VehicleEntity> findAll();

	List<VehicleEntity> findAll(Sort[] sortings, String filterQuery, Pagination pagination);
	
	Optional<VehicleEntity> findById(Long id);
	
	void deleteById(Long id) throws EntityNotExistException;
	
	void deleteByEnginePower(Integer enginePower) throws EntityNotExistException;
	
	VehicleEntity create(VehicleEntity entity) throws EntityAlreadyExistsException;
	
	void update(VehicleEntity entity) throws EntityNotExistException;
	
	void update(Long id, EntityUpdate<VehicleEntity> updates) throws EntityNotExistException;
	
	double getAverageNumberOfWheels();
	
	List<VehicleEntity> getVehiclesFuelTypeLessThan(FuelType type);
}
