package kz.mlapp.service;

import kz.mlapp.dto.CategoryDto;
import kz.mlapp.model.Category;
import kz.mlapp.repos.CategoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SuperCategoryService superCategoryService;

    public CategoryService(CategoryRepository categoryRepository, SuperCategoryService superCategoryService) {
        this.categoryRepository = categoryRepository;
        this.superCategoryService = superCategoryService;
    }

    // Получить все категории
    public List<Category> getAllCategories() {
        return categoryRepository.findAllWithSuperCategory();
    }
    public List<CategoryDto> getAllDtoCategories() {
        return getAllCategories().stream()
                .map(c -> CategoryDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .parentName(c.getSuperCategory() != null ? c.getSuperCategory().getName() : "Без категории")
                        .direction(c.getSuperCategory() != null ? c.getSuperCategory().getDirection() : "Нет направления")
                        .build())
                .toList();  // ✅ Преобразуем поток в список
    }



    // Получить категорию по ID
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    // Создать или обновить категорию
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }
    public void createOrSaveCategory(CategoryDto dto) {
        // Если ID передан → ищем категорию, иначе создаём новую
        var category = new Category();
                if (dto.getId()!=null) {
                    category = getCategoryById(dto.getId()).orElse(new Category());
                }

        // Устанавливаем имя категории, если оно не пустое
        if (dto.getName() != null && !dto.getName().isBlank()) {
            category.setName(dto.getName());
        }

        // Ищем родительскую категорию и привязываем
        superCategoryService.getSuperCategoryByName(dto.getParentName())
                .ifPresent(category::setSuperCategory);

        // Сохраняем в репозиторий
        saveCategory(category);
    }

    // Удалить категорию
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
