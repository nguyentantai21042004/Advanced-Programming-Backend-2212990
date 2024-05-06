package com.carcompany.dtos.order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data // toString
@Getter
@Setter
@AllArgsConstructor
public class CalculateDTO {
    @NotNull
    Double distance;

    @NotNull
    Double duration;

    @NotNull
    Double VehiclePrice;

    @NotNull
    Double DriverPrice;

    @NotNull
    Double SumOfExpense;

    @NotNull
    Double profit;
}
