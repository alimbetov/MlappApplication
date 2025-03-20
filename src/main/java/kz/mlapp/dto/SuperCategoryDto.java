package kz.mlapp.dto;

import jakarta.persistence.Column;
import kz.mlapp.model.SuperCategory;
import kz.mlapp.model.User;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
public class SuperCategoryDto {
    private Long id;
    private String name;
    private String direction;

}
