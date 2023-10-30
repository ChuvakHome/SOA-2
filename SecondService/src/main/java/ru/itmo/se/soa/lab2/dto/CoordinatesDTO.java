package ru.itmo.se.soa.lab2.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CoordinatesDTO {
    private double x; //Максимальное значение поля: 52
    private Long y; //Максимальное значение поля: 573, Поле не может быть null
}
