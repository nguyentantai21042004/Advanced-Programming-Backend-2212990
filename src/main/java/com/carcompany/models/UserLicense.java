package com.carcompany.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_licenses")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLicense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "licenses_id", length = 300)
    private Long licensesId;
}
