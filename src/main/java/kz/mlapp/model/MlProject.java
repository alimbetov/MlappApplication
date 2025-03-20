package kz.mlapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ml_projects") // Исправленный импорт
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MlProject {

    @Id // Исправленный импорт
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // Исправленный импорт
    private String model;

    @Column(nullable = false) // Исправленный импорт
    private String description;

    @ManyToOne
    @JoinColumn(name = "creator_user_id")
    private User creatorUser;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "project_users",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> accessUsers = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "project_categories",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    private LocalDateTime createdAt = LocalDateTime.now();
}
