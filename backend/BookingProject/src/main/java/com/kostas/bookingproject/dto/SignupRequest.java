package com.kostas.bookingproject.dto;

public class SignupRequest {
    private String name;
    private String email;
    private String password;
    private String phone;

    public SignupRequest() {}

    public SignupRequest(String name, String email, String password, String phone) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }
}
