package com.carcompany.dtos.maintenance;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data//toString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateMaintenancyDTO {
    @NotBlank
    private String status;
}
