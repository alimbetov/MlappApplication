package kz.mlapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_box")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FileBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private MlProject project;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User uploadedUser;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private FileStatus status;

    @Column(name = "file_key", unique = true, nullable = false)
    private String fileKey; // Ключ для MinIO

    @Column(name = "parent_key", nullable = false)
    private String patentKey; // Ключ для MinIO

    @Column(name = "filename", nullable = false)
    private String filename; // Оригинальное имя файла

    @Column(name = "description")
    private String description; // Описание файла (если нужно)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
