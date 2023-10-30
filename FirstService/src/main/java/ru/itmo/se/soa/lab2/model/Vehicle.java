package ru.itmo.se.soa.lab2.model;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.se.soa.lab2.entity.VehicleEntity;

@NoArgsConstructor
@Data
public class Vehicle {
    private long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    
    @NotBlank(message = "name cannot be blank string")
    private String name; //Поле не может быть null, Строка не может быть пустой
	
	@NotNull(message = "coordinates cannot be null")
	@Valid
    private Coordinates coordinates; //Поле не может быть null
    
    @NotNull(message = "enginePower cannot be null")
    @Positive(message = "enginePower must be positive")
    private Integer enginePower; //Поле не может быть null, Значение поля должно быть больше 0
    
    @NotNull(message = "numberOfWheels cannot be null")
    @Positive(message = "numberOfWheels must be positive")
    private Integer numberOfWheels; //Поле не может быть null, Значение поля должно быть больше 0
    
    @PositiveOrZero(message = "distanceTravelled must be non-negative")
    private int distanceTravelled; //Значение поля должно быть больше 0
    
    @Nullable
    private FuelType fuelType; //Поле может быть null
    
    public VehicleEntity toEntity() {    	
    	VehicleEntity entity = new VehicleEntity();
    	entity.setId(null);
    	entity.setName(name);
    	entity.setCoordinates(coordinates.toEntity());
    	entity.setCreationDate(null);
    	entity.setEnginePower(enginePower);
    	entity.setNumberOfWheels(numberOfWheels);
    	entity.setDistanceTravelled(distanceTravelled);
    	entity.setFuelType(fuelType);
    	
    	return entity;
    }
}
