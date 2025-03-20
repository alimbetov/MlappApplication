package kz.mlapp.enums;

import java.util.Arrays;

public enum RoleName {
    ROLE_USER, ROLE_ADMIN, ROLE_ANATATOR, ROLE_INSPECTOR, ROLE_MODERATOR;

    // ✅ Метод для поиска роли по названию строки
    public static RoleName fromString(String name) {
        return Arrays.stream(RoleName.values())
                .filter(role -> role.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестная роль: " + name));
    }
}
