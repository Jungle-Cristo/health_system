package com.health.service;

import com.health.dto.AuthResponse;
import com.health.dto.LoginRequest;
import com.health.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout();
}
