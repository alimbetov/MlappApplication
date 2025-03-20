package kz.mlapp.controller;

import kz.mlapp.model.SuperCategory;
import kz.mlapp.service.SuperCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/super-categories")
public class SuperCategoryController {

    private final SuperCategoryService superCategoryService;

    public SuperCategoryController(SuperCategoryService superCategoryService) {
        this.superCategoryService = superCategoryService;
    }

    // Получить все суперкатегории
    @GetMapping
    public ResponseEntity<List<SuperCategory>> getAllSuperCategories() {
        return ResponseEntity.ok(superCategoryService.getAllSuperCategories());
    }

    // Получить суперкатегорию по ID
    @GetMapping("/{id}")
    public ResponseEntity<SuperCategory> getSuperCategoryById(@PathVariable Long id) {
        Optional<SuperCategory> superCategory = superCategoryService.getSuperCategoryById(id);
        return superCategory.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Создать новую суперкатегорию
    @PostMapping
    public ResponseEntity<SuperCategory> createSuperCategory(@RequestBody SuperCategory superCategory) {
        return ResponseEntity.ok(superCategoryService.saveSuperCategory(superCategory));
    }

    // Обновить суперкатегорию
    @PutMapping("/{id}")
    public ResponseEntity<SuperCategory> updateSuperCategory(@PathVariable Long id, @RequestBody SuperCategory newSuperCategory) {
        return superCategoryService.getSuperCategoryById(id)
                .map(existingSuperCategory -> {
                    existingSuperCategory.setName(newSuperCategory.getName());
                    existingSuperCategory.setDirection(newSuperCategory.getDirection());
                    return ResponseEntity.ok(superCategoryService.saveSuperCategory(existingSuperCategory));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Удалить суперкатегорию
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSuperCategory(@PathVariable Long id) {
        superCategoryService.deleteSuperCategory(id);
        return ResponseEntity.noContent().build();
    }
}
