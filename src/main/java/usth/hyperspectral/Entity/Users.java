package usth.hyperspectral.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.*;

import java.util.List;

@Entity
@UserDefinition
public class Users extends PanacheEntityBase {
    @Column(name = "user_id", unique = true)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long user_id;
    @Column(name = "username", unique = true)
    @Username
    public String username;
    @Column(name = "password")
    @Password
    public String password;
    @Column(name = "role")
    public String role;



    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
