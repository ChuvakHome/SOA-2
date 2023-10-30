package ru.itmo.se.soa.lab2.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class VehicleDTO {
    private String name; //Поле не может быть null, Строка не может быть пустой
    private CoordinatesDTO coordinates; //Поле не может быть null
    private Integer enginePower; //Поле не может быть null, Значение поля должно быть больше 0
    private Integer numberOfWheels; //Поле не может быть null, Значение поля должно быть больше 0
    private int distanceTravelled; //Значение поля должно быть больше 0
    private FuelType fuelType; //Поле может быть null
}
