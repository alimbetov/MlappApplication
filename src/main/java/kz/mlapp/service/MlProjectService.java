package kz.mlapp.service;

import kz.mlapp.dto.CategoryDto;
import kz.mlapp.dto.MlProjectDto;
import kz.mlapp.model.Category;
import kz.mlapp.model.MlProject;
import kz.mlapp.model.User;
import kz.mlapp.repos.MlProjectRepository;
import kz.mlapp.repos.UserRepository;
import kz.mlapp.repos.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MlProjectService {

    private final MlProjectRepository mlProjectRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    // ✅ Получить список всех проектов (DTO)
    public List<MlProjectDto> getAllProjects() {
        return mlProjectRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ✅ Найти проект по ID (DTO)
    public MlProjectDto getProjectById(Long id) {
        MlProject project = mlProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Проект с ID " + id + " не найден"));
        return convertToDto(project);
    }

    // ✅ Создать новый проект (DTO)
    public MlProjectDto createProject(MlProjectDto dto) {
        MlProject project = new MlProject();
        project.setModel(dto.getModel());
        project.setDescription(dto.getDescription());
        project.setCreatorUser(userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь " + dto.getUsername() + " не найден")));

        if (dto.getAccessUsers()!= null) {
            // Добавляем доступных пользователей
            Set<User> accessUsers = dto.getAccessUsers().stream()
                    .map(username -> userRepository.findByUsername(username)
                            .orElseThrow(() -> new RuntimeException("Пользователь " + username + " не найден")))
                    .collect(Collectors.toSet());
            project.setAccessUsers(accessUsers);
        } else {
            project.setAccessUsers(new HashSet<>());
        }
        // Добавляем категории
        if (dto.getCategories()!= null) {
            Set<Category> categories = dto.getCategories().stream()
                    .map(catDto -> categoryRepository.findById(catDto.getId())
                            .orElseThrow(() -> new RuntimeException("Категория с ID " + catDto.getId() + " не найдена")))
                    .collect(Collectors.toSet());
            project.setCategories(categories);
        } else {
            project.setCategories(new HashSet<>());
        }

        project = mlProjectRepository.save(project);
        return convertToDto(project);
    }

    // ✅ Обновить проект (DTO)
    public MlProjectDto updateProject(Long id, MlProjectDto updatedDto) {
        MlProject project = mlProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Проект с ID " + id + " не найден"));

        project.setModel(updatedDto.getModel());
        project.setDescription(updatedDto.getDescription());

        // Обновляем пользователя
        project.setCreatorUser(userRepository.findByUsername(updatedDto.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь " + updatedDto.getUsername() + " не найден")));

        // Обновляем доступных пользователей
        Set<User> accessUsers = updatedDto.getAccessUsers().stream()
                .map(username -> userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Пользователь " + username + " не найден")))
                .collect(Collectors.toSet());
        project.setAccessUsers(accessUsers);

        // Обновляем категории
        Set<Category> categories = updatedDto.getCategories().stream()
                .map(catDto -> categoryRepository.findById(catDto.getId())
                        .orElseThrow(() -> new RuntimeException("Категория с ID " + catDto.getId() + " не найдена")))
                .collect(Collectors.toSet());
        project.setCategories(categories);

        project = mlProjectRepository.save(project);
        return convertToDto(project);
    }

    // ✅ Удалить проект по ID
    public void deleteProject(Long id) {
        if (!mlProjectRepository.existsById(id)) {
            throw new RuntimeException("Проект с ID " + id + " не найден");
        }
        mlProjectRepository.deleteById(id);
    }

    // ✅ Преобразование в DTO
    private MlProjectDto convertToDto(MlProject project) {
        return new MlProjectDto(
                project.getId(),
                project.getModel(),
                project.getDescription(),
                project.getCreatorUser() != null ? project.getCreatorUser().getUsername() : "Неизвестный пользователь",
                project.getAccessUsers() != null ? project.getAccessUsers().stream()
                        .map(User::getUsername)
                        .collect(Collectors.toSet()) : new HashSet<>(),
                project.getCategories() != null ? project.getCategories().stream()
                        .map(cat -> new CategoryDto(cat.getId(), cat.getName(),
                                cat.getSuperCategory() != null ? cat.getSuperCategory().getName() : "Без категории",
                                cat.getSuperCategory() != null ? cat.getSuperCategory().getDirection() : "Нет направления"))
                        .collect(Collectors.toSet()) : new HashSet<>(),
                project.getCreatedAt()
        );
    }
}
