package kz.mlapp.service;

import kz.mlapp.dto.SuperCategoryDto;

import kz.mlapp.model.SuperCategory;

import kz.mlapp.repos.SuperCategoryRepository;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SuperCategoryService {

    private final SuperCategoryRepository superCategoryRepository;

    public SuperCategoryService(SuperCategoryRepository superCategoryRepository) {
        this.superCategoryRepository = superCategoryRepository;
    }

    // Получить все суперкатегории
    public List<SuperCategory> getAllSuperCategories() {
        return superCategoryRepository.findAll();
    }

    public SuperCategoryDto updateSuperCategory(Long id, SuperCategoryDto dto) {
            var result = toSuperCategory(dto);
        return saveSuperCategory(result).toDto();
    }

    public List<SuperCategoryDto> getAllDtoSuperCategories() {
        return getAllSuperCategories()
                .stream()
                .map(SuperCategory::toDto) // ✅ Преобразуем `SuperCategory` в `SuperCategoryDto`
                .collect(Collectors.toList()); // ✅ Собираем в список
    }

    // Найти суперкатегорию по ID
    public Optional<SuperCategory> getSuperCategoryById(Long id) {
        return superCategoryRepository.findById(id);
    }

    public Optional<SuperCategory> getSuperCategoryByName(String name) {
        return superCategoryRepository.findByName(name);
    }

    // Создать или обновить суперкатегорию
    public SuperCategory saveSuperCategory(SuperCategory superCategory) {
        return superCategoryRepository.save(superCategory);
    }

    public SuperCategoryDto createSuperCategory(SuperCategoryDto dto) {
        SuperCategory superCategory = SuperCategory.builder()
                .name(dto.getName())
                .direction(dto.getDirection())
                .build();

        SuperCategory savedCategory = superCategoryRepository.save(superCategory);
        return savedCategory.toDto();
    }

    // Удалить суперкатегорию
    public void deleteSuperCategory(Long id) {
        superCategoryRepository.deleteById(id);
    }

    public SuperCategory toSuperCategory(SuperCategoryDto dto) {
        var result = new SuperCategory();
        if (dto.getId() != null) {
            result = getSuperCategoryById(dto.getId()).orElse(new SuperCategory());
        }
        if (dto.getId() != null) {
            result.setId(dto.getId());
        }
        if (dto.getName() != null) {
            result.setName(dto.getName());
        }
        if (dto.getDirection() != null) {
            result.setDirection(dto.getDirection());
        }
        return result;
    }


}
