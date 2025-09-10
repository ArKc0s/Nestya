package fr.nestya.auth;

import fr.nestya.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Getter
    @Setter
    private User user;

    @Column(nullable = false, unique = true)
    @Getter
    @Setter
    private String token;

    @Column(nullable = false)
    @Getter
    @Setter
    private Instant expiryDate;

}