package it.epicode.Capstone.login.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
