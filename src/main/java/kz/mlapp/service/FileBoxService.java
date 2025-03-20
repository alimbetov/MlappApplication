package kz.mlapp.service;

import kz.mlapp.dto.FileBoxDto;
import kz.mlapp.model.FileBox;
import kz.mlapp.model.FileStatus;
import kz.mlapp.model.MlProject;
import kz.mlapp.model.User;
import kz.mlapp.repos.FileBoxRepository;
import kz.mlapp.repos.FileStatusRepository;
import kz.mlapp.repos.MlProjectRepository;
import kz.mlapp.repos.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileBoxService {

    private final FileBoxRepository fileBoxRepository;
    private final MlProjectRepository mlProjectRepository;
    private final UserRepository userRepository;
    private final FileStatusRepository fileStatusRepository;

    private final MinioService minioService;

    public FileBoxService(FileBoxRepository fileBoxRepository,
                          MlProjectRepository mlProjectRepository,
                          UserRepository userRepository,
                          FileStatusRepository fileStatusRepository,
                          MinioService minioService) {
        this.fileBoxRepository = fileBoxRepository;
        this.mlProjectRepository = mlProjectRepository;
        this.userRepository = userRepository;
        this.fileStatusRepository = fileStatusRepository;
        this.minioService = minioService;
    }

    // ✅ Получить все файлы с разбиением по страницам
    public Page<FileBoxDto> getAllFiles(int page, int size) {
        var pageable = PageRequest.of(page, size);
        return fileBoxRepository.findAll(pageable).map(this::convertToDto);
    }

    // ✅ Получить файлы по проекту и статусу с разбиением по страницам
    public Page<FileBoxDto> getFilesByProjectAndStatus(Long projectId, Long statusId, int page, int size) {
        var pageable = PageRequest.of(page, size);

        MlProject project = mlProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Проект не найден"));

        FileStatus status = fileStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Статус не найден"));

        return fileBoxRepository.findAllByProjectAndStatus(project, status, pageable).map(this::convertToDto);
    }

    // ✅ Получить файл по ID
    public Optional<FileBoxDto> getFileById(Long id) {
        return fileBoxRepository.findById(id).map(this::convertToDto);
    }

    // ✅ Создать или обновить файл
    public FileBoxDto saveFile(FileBoxDto dto) {
        FileBox fileBox = convertToEntity(dto);
        FileBox savedFile = fileBoxRepository.save(fileBox);
        return convertToDto(savedFile);
    }
    public FileBoxDto saveFile(FileBoxDto dto, MultipartFile multipartFile) {
        FileBox savedFile = null;
        FileBox fileBox = convertToEntity(dto);
        try {
            minioService.uploadFile(fileBox.getFileKey(), multipartFile);
             savedFile = fileBoxRepository.save(fileBox);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println(multipartFile.getName()+" MINIO SAVE");
        return convertToDto(savedFile);
    }

    // ✅ Удалить файл по ID
    public void deleteFile(Long id) {
        var delDto = fileBoxRepository.getReferenceById(id);
        try {
            minioService.deleteFile(delDto.getFileKey());
            fileBoxRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    // 🔄 Конвертация FileBox → FileBoxDto
    private FileBoxDto convertToDto(FileBox fileBox) {
        return FileBoxDto.builder()
                .id(fileBox.getId())
                .projectId(fileBox.getProject().getId())
                .uploadedUserId(fileBox.getUploadedUser().getId())
                .statusId(fileBox.getStatus().getId())
                .fileKey(fileBox.getFileKey())
                .parentKey(fileBox.getPatentKey())
                .filename(fileBox.getFilename())
                .description(fileBox.getDescription())
                .createdAt(fileBox.getCreatedAt())
                .build();
    }

    // 🔄 Конвертация FileBoxDto → FileBox
    private FileBox convertToEntity(FileBoxDto dto) {
        MlProject project = mlProjectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Проект не найден"));

        User user = userRepository.findById(dto.getUploadedUserId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        FileStatus status = fileStatusRepository.findById(dto.getStatusId())
                .orElseThrow(() -> new RuntimeException("Статус не найден"));

        return FileBox.builder()
                .id(dto.getId())
                .project(project)
                .uploadedUser(user)
                .status(status)
                .fileKey(dto.getFileKey())
                .patentKey(dto.getParentKey())
                .filename(dto.getFilename())
                .description(dto.getDescription())
                .build();
    }
}
