package kz.mlapp.ui.pages;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import kz.mlapp.dto.CategoryDto;
import kz.mlapp.dto.SuperCategoryDto;
import kz.mlapp.service.CategoryService;
import kz.mlapp.service.SuperCategoryService;
import kz.mlapp.ui.pages.layout.MainLayout;

import java.util.List;
import java.util.stream.Collectors;


@Route(value = "category", layout = MainLayout.class)
@PageTitle("Детальная категория")
@RolesAllowed("ROLE_ADMIN") // ✅ Ограничиваем доступ только для администраторов
public class CategoryView extends VerticalLayout {


    private SuperCategoryService superCategoryService;
    private CategoryService categoryService;
    private final Grid<CategoryDto> categoryGrid = new Grid<>(CategoryDto.class);
    private final TextField searchField = new TextField("Поиск по имени"); // ✅ Поле поиска

    public CategoryView(SuperCategoryService superCategoryService, CategoryService categoryService) {
        this.superCategoryService = superCategoryService;
        this.categoryService = categoryService;

        Span pageTitle = new Span();
        pageTitle.setHeightFull();
        pageTitle.setTitle("   Панель Детальной категории");



        // ✅ Добавляем строку поиска
        Button searchButton = new Button("Найти", event -> searchCategories());
        Button resetButton = new Button("Сбросить", event -> refreshGrid());
        Button createButton = new Button("Создать", event -> openCreateDialog());

        HorizontalLayout vertCreatPanel = new HorizontalLayout(resetButton, searchField,searchButton);

        HorizontalLayout searchLayout = new HorizontalLayout(createButton, vertCreatPanel, pageTitle);
        add(searchLayout);

        categoryGrid.setItems(categoryService.getAllDtoCategories());
        categoryGrid.setColumns("id", "name", "parentName", "direction");
        categoryGrid.addComponentColumn(this::createEditButton).setHeader("Редактировать");
        categoryGrid.addComponentColumn(this::createDeleteButton).setHeader("Удалить");

        refreshGrid();

        add(categoryGrid);
    }


    // ✅ Метод для поиска пользователей по `username`
// ✅ Метод для поиска категорий по названию
    private void searchCategories() {
        String searchTerm = searchField.getValue().trim();
        if (searchTerm.isEmpty()) {
            refreshGrid();
            return;
        }

        List<CategoryDto> filteredCategories = categoryService.getAllDtoCategories().stream()
                .filter(cat -> cat.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());

        categoryGrid.setItems(filteredCategories);
    }

    // ✅ Метод для обновления данных в таблице
    private void refreshGrid() {
        List<CategoryDto> categories = categoryService.getAllDtoCategories();
        categoryGrid.setItems(categories);
    }

    // ✅ Создаёт кнопку "Редактировать"
    private Button createEditButton(CategoryDto dto) {
        return new Button("Редактировать", event -> openEditDialog(dto));
    }

    // ✅ Создаёт кнопку "Удалить"

    private Button createDeleteButton(CategoryDto dto) {
        Button deleteButton = new Button("Удалить", event -> openDeleteDialog(dto));
        deleteButton.getStyle().set("color", "red"); // ✅ Красная кнопка удаления
        return deleteButton;
    }



    // ✅ Диалоговое окно для редактирования категории
    // ✅ Метод для редактирования категории
    private void openEditDialog(CategoryDto dto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Редактировать Детальную категорию");

        TextField nameField = new TextField("Детальная категория");
        nameField.setValue(dto.getName());

        var listResult = superCategoryService.getAllDtoSuperCategories().stream()
                .map(SuperCategoryDto::getName)
                .collect(Collectors.toSet());

        ComboBox<String> parentNameField = new ComboBox<>("Главная категория");
        parentNameField.setItems(listResult);
        parentNameField.setValue(dto.getParentName()); // ✅ Устанавливаем текущее значение

        Button saveButton = new Button("Сохранить", event -> {
            dto.setName(nameField.getValue());

            if (parentNameField.getValue() != null) {
                dto.setParentName(parentNameField.getValue());
            }

            categoryService.createOrSaveCategory(dto);
            refreshGrid();
            dialog.close();
            Notification.show("Данные обновлены!");
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());

        dialog.add(new VerticalLayout(nameField, parentNameField, new HorizontalLayout(saveButton, cancelButton)));
        dialog.open();
    }

    // ✅ Диалог подтверждения удаления категории
    private void openDeleteDialog(CategoryDto dto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Удалить пользователя?");

        // ✅ Описание с именем пользователя
        VerticalLayout content = new VerticalLayout();
        content.add("Вы уверены, что хотите удалить категорию " + dto.getName() + "?");

        // ✅ Кнопка подтверждения удаления
        Button confirmButton = new Button("Да", event -> {

            if (dto.getId() != null) {
                categoryService.deleteCategory(dto.getId());
                refreshGrid();
                dialog.close();
                Notification.show("Категория удалена!");
            } else {
                Notification.show("Ошибка: Невозможно удалить категорию без ID!", 3000, Notification.Position.MIDDLE);
            }
        });

        // ✅ Кнопка отмены удаления
        Button cancelButton = new Button("Нет", event -> dialog.close());

        // ✅ Выравниваем кнопки горизонтально
        HorizontalLayout buttonLayout = new HorizontalLayout(confirmButton, cancelButton);

        dialog.add(content, buttonLayout);
        dialog.open();
    }



    // ✅ Диалоговое окно для создания новой категории
    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Создать главную категорию");

        TextField nameField = new TextField("детальная категория");

        var listResult = superCategoryService.getAllDtoSuperCategories().stream().map(SuperCategoryDto::getName).collect(Collectors.toSet());

        ComboBox<String> parentNameField = new ComboBox<>("Главная категория");
        parentNameField.setItems(listResult);


        Button saveButton = new Button("Сохранить", event -> {
            String name = nameField.getValue().trim();
            String parentName = parentNameField.getValue() != null ? parentNameField.getValue().trim() : "";


            if (name.isEmpty() || parentName.isBlank()) {
                Notification.show("Ошибка: Все поля должны быть заполнены!", 3000, Notification.Position.MIDDLE);
                return;
            }

            // ✅ Создаём новый объект DTO и передаём в сервис
            CategoryDto newCategory = CategoryDto.builder()
                    .name(name)
                    .parentName(parentName)
                    .build();

            categoryService.createOrSaveCategory(newCategory);
            refreshGrid();
            dialog.close();
            Notification.show("Категория создана!");
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());

        dialog.add(new VerticalLayout(nameField, parentNameField, new HorizontalLayout(saveButton, cancelButton)));
        dialog.open();
    }

}
