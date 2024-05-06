package com.carcompany.responses.order;

import com.carcompany.models.DeliveryOrder;
import lombok.*;

import java.util.Date;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private Long id;

    private Long vehicleId;

    private String vehicleName;

    private Long driverId;

    private String driverName;

    private String driverPhone;

    private String startPlace;

    private String endPlace;

    private Date startDate;

    private String customerPhone;

    private Double distance;

    private Double duration;

    String status;

    private Double sumOfExpense;

    private Double profit;

    public static OrderResponse fromDeliveryOrder(DeliveryOrder orders){
        return OrderResponse.builder()
                .id(orders.getId())
                .vehicleId(orders.getVehicle().getId())
                .vehicleName(orders.getVehicle().getName())
                .driverId(orders.getUser().getId())
                .driverName(orders.getUser().getFullName())
                .driverPhone(orders.getUser().getPhoneNumber())
                .startPlace(orders.getStartPlace())
                .endPlace(orders.getEndPlace())
                .startDate(orders.getStartDate())
                .customerPhone(orders.getPhoneNumber())
                .distance(orders.getDistance())
                .duration(orders.getDuration())
                .status(orders.getStatus())
                .sumOfExpense(orders.getSumOfExpense())
                .profit(orders.getProfit())
                .build();
    }
}
