package com.spring.homeless_user.user.entity;

public enum Provider {
    LOCAL, GOOGLE, GITHUB;

    public static Provider fromRegistrationId(String registrationId) {
        return switch (registrationId.toUpperCase()) {
            case "GOOGLE" -> GOOGLE;
            case "GITHUB" -> GITHUB;
            default -> throw new IllegalArgumentException("Unknown provider: " + registrationId);
        }
    }
}
