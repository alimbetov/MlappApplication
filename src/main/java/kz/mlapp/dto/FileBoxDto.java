package kz.mlapp.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileBoxDto {
    private Long id;
    private Long projectId;
    private Long uploadedUserId;
    private Long statusId;
    private String fileKey;
    private String parentKey;
    private String filename;
    private String description;
    private  String fileType;
    private Long fileSize;
    private LocalDateTime createdAt;
}
