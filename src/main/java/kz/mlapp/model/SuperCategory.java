package kz.mlapp.model;

import jakarta.persistence.*;
import kz.mlapp.dto.SuperCategoryDto;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "super_categories")  // Исправленное имя таблицы
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuperCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String direction;

    @OneToMany(mappedBy = "superCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> categories;


    // ✅ Переименовываем `getSuperCategoryDto()` → `toDto()`
    public SuperCategoryDto toDto() {
        return SuperCategoryDto.builder()
                .id(id)
                .name(name)
                .direction(direction)
                .build();
    }
}

