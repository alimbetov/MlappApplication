package kz.mlapp.model;

import jakarta.persistence.*;
import kz.mlapp.enums.FileStatusName;
import lombok.*;

@Entity
@Table(name = "file_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private FileStatusName status;
}
