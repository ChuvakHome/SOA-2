package ru.itmo.se.soa.lab2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Embeddable
public class CoordinatesEntity {
	@Column(name = "coordinates_x", nullable = false, columnDefinition = "double precision CHECK (coordinates_x <= 52)")
    private double x; //Максимальное значение поля: 52
    
	@Column(name = "coordinates_y", nullable = false, columnDefinition = "bigint CHECK (coordinates_y <= 573)")
    private Long y; //Максимальное значение поля: 573, Поле не может быть null
}
