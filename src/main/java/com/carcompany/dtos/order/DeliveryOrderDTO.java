package com.carcompany.dtos.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

@Data // toString
@Getter
@Setter
@AllArgsConstructor
public class DeliveryOrderDTO {
    @NotNull
    private Long vehicleId;

    @NotNull
    private Long userId;

    @NotNull
    private String startPlace;

    @NotNull
    private String endPlace;

    @NotNull
    private Date startDate;

    @NotNull
    private String phoneNumber;
}

