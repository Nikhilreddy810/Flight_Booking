package com.example.flight_auth_service.security;

public final class Roles {

    public static final String ADMIN = "ROLE_ADMIN";
    public static final String USER = "ROLE_USER";

    private Roles() {}

    public static boolean isAdmin(String role) {
        return ADMIN.equals(role);
    }
}
