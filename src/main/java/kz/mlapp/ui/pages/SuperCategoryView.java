package kz.mlapp.ui.pages;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
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
import kz.mlapp.dto.SuperCategoryDto;
import kz.mlapp.dto.UserDto;
import kz.mlapp.enums.RoleName;
import kz.mlapp.service.SuperCategoryService;
import kz.mlapp.ui.pages.layout.MainLayout;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Route(value = "super-category", layout = MainLayout.class)
@PageTitle("Главная категория")
@RolesAllowed("ROLE_ADMIN") // ✅ Ограничиваем доступ только для администраторов
public class SuperCategoryView extends VerticalLayout {


    private SuperCategoryService superCategoryService;
    private final Grid<SuperCategoryDto> categoryGrid = new Grid<>(SuperCategoryDto.class);
    private final TextField searchField = new TextField("Поиск по имени"); // ✅ Поле поиска

    public SuperCategoryView(SuperCategoryService superCategoryService) {
        this.superCategoryService = superCategoryService;

        Span pageTitle = new Span();
        pageTitle.setHeightFull();
        pageTitle.setTitle("   Панель главной категории");



        // ✅ Добавляем строку поиска
        Button searchButton = new Button("Найти", event -> searchUsers());
        Button resetButton = new Button("Сбросить", event -> refreshGrid());
        Button createButton = new Button("Создать", event -> openCreateDialog());

        HorizontalLayout vertCreatPanel = new HorizontalLayout(resetButton, searchField,searchButton);

        HorizontalLayout searchLayout = new HorizontalLayout(createButton, vertCreatPanel, pageTitle);
        add(searchLayout);


        categoryGrid.setItems(superCategoryService.getAllDtoSuperCategories());
        categoryGrid.setColumns("id", "name", "direction");
        categoryGrid.addComponentColumn(this::createEditButton).setHeader("Редактировать");
        categoryGrid.addComponentColumn(this::createDeleteButton).setHeader("Удалить");

        refreshGrid();

        add(categoryGrid);
    }


    // ✅ Метод для поиска пользователей по `username`
    private void searchUsers() {
        String searchTerm = searchField.getValue().trim();
        if (searchTerm.isEmpty()) {
            refreshGrid();
            return;
        }

        List<SuperCategoryDto> filteredUsers = superCategoryService.getAllDtoSuperCategories().stream()
                .filter(cat -> cat.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());

        categoryGrid.setItems(filteredUsers);
    }

    // ✅ Метод для обновления данных в таблице
    private void refreshGrid() {
        List<SuperCategoryDto> categories = superCategoryService.getAllDtoSuperCategories();
        categoryGrid.setItems(categories);
    }

    // ✅ Создаёт кнопку "Редактировать"
    private Button createEditButton(SuperCategoryDto dto) {
        return new Button("Редактировать", event -> openEditDialog(dto));
    }

    // ✅ Создаёт кнопку "Удалить"

    private Button createDeleteButton(SuperCategoryDto dto) {
        Button deleteButton = new Button("Удалить", event -> openDeleteDialog(dto));
        deleteButton.getStyle().set("color", "red"); // ✅ Красная кнопка удаления
        return deleteButton;
    }



    // ✅ Диалоговое окно для редактирования категории
    private void openEditDialog(SuperCategoryDto dto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Редактировать главную категорию");

        TextField nameField = new TextField("главная категория");
        nameField.setValue(dto.getName());

        TextField directionField = new TextField("направление");
        directionField.setValue(dto.getDirection());



        Button saveButton = new Button("Сохранить", event -> {
            dto.setName(nameField.getValue());
            dto.setDirection(directionField.getValue());

            superCategoryService.updateSuperCategory(dto.getId(), dto);
            refreshGrid();
            dialog.close();
            Notification.show("Данные обновлены!");
        });
        Button cancelButton = new Button("Отмена", event -> dialog.close());
        dialog.add(new VerticalLayout(nameField, directionField, new HorizontalLayout(saveButton, cancelButton)));
        dialog.open();
    }

    // ✅ Диалог подтверждения удаления категории
    private void openDeleteDialog(SuperCategoryDto dto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Удалить пользователя?");

        // ✅ Описание с именем пользователя
        VerticalLayout content = new VerticalLayout();
        content.add("Вы уверены, что хотите удалить категорию " + dto.getName() + "?");

        // ✅ Кнопка подтверждения удаления
        Button confirmButton = new Button("Да", event -> {
            superCategoryService.deleteSuperCategory(dto.getId());
            refreshGrid();
            dialog.close();
            Notification.show("Пользователь удалён!");
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

        TextField nameField = new TextField("Главная категория");
        TextField directionField = new TextField("Направление");

        Button saveButton = new Button("Сохранить", event -> {
            String name = nameField.getValue().trim();
            String direction = directionField.getValue().trim();

            if (name.isEmpty() || direction.isEmpty()) {
                Notification.show("Ошибка: Все поля должны быть заполнены!", 3000, Notification.Position.MIDDLE);
                return;
            }

            // ✅ Создаём новый объект DTO и передаём в сервис
            SuperCategoryDto newCategory = SuperCategoryDto.builder()
                    .name(name)
                    .direction(direction)
                    .build();

            superCategoryService.createSuperCategory(newCategory);
            refreshGrid();
            dialog.close();
            Notification.show("Категория создана!");
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());

        dialog.add(new VerticalLayout(nameField, directionField, new HorizontalLayout(saveButton, cancelButton)));
        dialog.open();
    }

}
