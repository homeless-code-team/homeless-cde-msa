package com.spring.homelesscode.friends_server.entity;

public enum AddStatus {
    ACCEPT, REJECTED, PENDING;

    public boolean equalsIgnoreCase(String name) {
        if (name == null) {
            return false; // null과 비교 시 false 반환
        }
        return this.name().equalsIgnoreCase(name); // 대소문자를 무시하고 비교
    }

}
