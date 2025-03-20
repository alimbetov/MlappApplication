package kz.mlapp.controller;

import kz.mlapp.dto.FileBoxDto;
import kz.mlapp.service.FileBoxService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "*") // ✅ Разрешаем фронтенду делать запросы
public class FileBoxController {

    private final FileBoxService fileBoxService;

    public FileBoxController(FileBoxService fileBoxService) {
        this.fileBoxService = fileBoxService;
    }

    // ✅ Получить все файлы с разбиением по страницам
    @GetMapping
    public Page<FileBoxDto> getAllFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return fileBoxService.getAllFiles(page, size);
    }

    // ✅ Получить файлы по проекту и статусу с разбиением по страницам
    @GetMapping("/search")
    public Page<FileBoxDto> getFilesByProjectAndStatus(
            @RequestParam Long projectId,
            @RequestParam Long statusId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return fileBoxService.getFilesByProjectAndStatus(projectId, statusId, page, size);
    }

    // ✅ Получить файл по ID
    @GetMapping("/{id}")
    public ResponseEntity<FileBoxDto> getFileById(@PathVariable Long id) {
        Optional<FileBoxDto> file = fileBoxService.getFileById(id);
        return file.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ Создать / обновить файл
    @PostMapping
    public ResponseEntity<FileBoxDto> saveFile(@RequestBody FileBoxDto dto) {
        return ResponseEntity.ok(fileBoxService.saveFile(dto));
    }

    // ✅ Удалить файл по ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        fileBoxService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
