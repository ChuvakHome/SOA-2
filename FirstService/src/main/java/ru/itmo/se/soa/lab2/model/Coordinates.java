package ru.itmo.se.soa.lab2.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.se.soa.lab2.entity.CoordinatesEntity;

@NoArgsConstructor
@Data
public class Coordinates {
	@Max(value = 52, message = "coordinates.x must be less or equal 52")
	private double x; //Максимальное значение поля: 52
    
	@NotNull(message = "coordinates.y cannot be null")
	@Max(value = 573, message = "coordinates.y must be less or equal 573")
    private Long y; //Максимальное значение поля: 573, Поле не может быть null
    
    public CoordinatesEntity toEntity() {
    	CoordinatesEntity entity = new CoordinatesEntity();
    	entity.setX(x);
    	entity.setY(y);
    	
    	return entity;
    }
}
