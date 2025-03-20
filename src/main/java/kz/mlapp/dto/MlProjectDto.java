package kz.mlapp.dto;

import jakarta.persistence.*;
import kz.mlapp.model.Category;
import kz.mlapp.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MlProjectDto {

    private Long id;

    private String model;

    private String description;

    private String username;

    Set<String> accessUsers = new HashSet<>();

   Set<CategoryDto> categories = new HashSet<>();

    private LocalDateTime createdAt = LocalDateTime.now();
}
