package com.kostas.bookingproject.models;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String name;
    private String email;
    private String password;
    private String phone;

    // Always stored as a list in DB
    private List<String> roles = new ArrayList<>();

    public String getRole() {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        return roles.get(0);
    }

    public void setRole(String role) {
        this.roles = List.of(role);
    }
}
