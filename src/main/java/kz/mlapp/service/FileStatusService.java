package kz.mlapp.service;


import kz.mlapp.dto.FileStatusDto;
import kz.mlapp.model.FileStatus;
import kz.mlapp.repos.FileStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileStatusService {

    private final FileStatusRepository fileStatusRepository;

    public FileStatusService(FileStatusRepository fileStatusRepository) {
        this.fileStatusRepository = fileStatusRepository;
    }

    // ✅ Получить все статусы (DTO)
    public List<FileStatusDto> getAllStatuses() {
        return fileStatusRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ✅ Найти статус по ID
    public Optional<FileStatusDto> getStatusById(Long id) {
        return fileStatusRepository.findById(id)
                .map(this::convertToDto);
    }

    // ✅ Создать новый статус
    public FileStatusDto createStatus(FileStatusDto dto) {
        FileStatus newStatus = FileStatus.builder()
                .status(dto.getStatus())
                .build();
        return convertToDto(fileStatusRepository.save(newStatus));
    }

    // ✅ Удалить статус
    public void deleteStatus(Long id) {
        fileStatusRepository.deleteById(id);
    }

    // 🔄 Конвертация Entity → DTO
    private FileStatusDto convertToDto(FileStatus fileStatus) {
        return FileStatusDto.builder()
                .id(fileStatus.getId())
                .status(fileStatus.getStatus())
                .build();
    }
}
