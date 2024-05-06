package com.carcompany.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_images")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserImage {
    public static final int MAXIMUM_IMAGES_PER_USER = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "image_url", length = 300)
    private String imageUrl;
}
