package kz.mlapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")  // Исправленное имя таблицы (множественное число - лучше для БД)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "super_category_id")  // Явное указание внешнего ключа
    private SuperCategory superCategory;
}
