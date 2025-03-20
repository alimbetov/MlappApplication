package kz.mlapp.dto;

import kz.mlapp.enums.FileStatusName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileStatusDto {
    private Long id;
    private FileStatusName status;
}