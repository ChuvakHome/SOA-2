package ru.itmo.se.soa.lab2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.se.soa.lab2.model.FuelType;

@Entity
@Table(name = "vehicles")
@NoArgsConstructor
@Data
public class VehicleEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @Column(name = "name", nullable = false, columnDefinition = "text CHECK (name <> '')")
    private String name;
    
    @Embedded
    private CoordinatesEntity coordinates; //Поле не может быть null
    
    @Column(name = "creation_date", nullable = false)
    private java.time.LocalDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    
    @Column(name = "engine_power", nullable = false, columnDefinition = "integer CHECK (engine_power > 0)")
    private Integer enginePower; //Поле не может быть null, Значение поля должно быть больше 0
    
    @Column(name = "number_of_wheels", nullable = false, columnDefinition = "integer CHECK (number_of_wheels > 0)")
    private Integer numberOfWheels; //Поле не может быть null, Значение поля должно быть больше 0
    
    @Column(name = "distance_travelled", nullable = false, columnDefinition = "integer CHECK (distance_travelled >= 0)")
    private int distanceTravelled; //Значение поля должно быть больше 0 (FIX: не меньше 0)
    
    @Column(name = "fuel_type", nullable = true)
    @Enumerated(EnumType.ORDINAL)
    private FuelType fuelType; //Поле может быть null
}
