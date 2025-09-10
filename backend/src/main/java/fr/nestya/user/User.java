package fr.nestya.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    // Getters and Setters
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    private String firstName;
    @Getter
    @Setter
    private String lastName;

    @Column(unique = true, nullable = false)
    @Getter
    @Setter
    private String email;

    @Column(nullable = false)
    @Getter
    @Setter
    private String password;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For simplicity, returning an empty list. Implement roles/authorities as needed.
        return Collections.emptyList();
    }

    @Override
    public String getUsername() {
        // Using email as the username
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        // For simplicity, returning true. Implement logic as needed.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // For simplicity, returning true. Implement logic as needed.
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // For simplicity, returning true. Implement logic as needed.
        return true;
    }

    @Override
    public boolean isEnabled() {
        // For simplicity, returning true. Implement logic as needed.
        return true;
    }
}
