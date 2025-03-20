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
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import kz.mlapp.dto.CategoryDto;
import kz.mlapp.dto.MlProjectDto;
import kz.mlapp.dto.SuperCategoryDto;
import kz.mlapp.dto.UserDto;
import kz.mlapp.security.JwtUtil;
import kz.mlapp.service.CategoryService;
import kz.mlapp.service.MlProjectService;
import kz.mlapp.ui.pages.layout.MainLayout;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


@Route(value = "projects", layout = MainLayout.class)
@PageTitle("Проекты")
@RolesAllowed("ROLE_ADMIN") // ✅ Ограничиваем доступ только для администраторов
public class MlProjectView extends VerticalLayout {


    private MlProjectService mlProjectService;
    private final JwtUtil jwtUtil;

    private final Grid<MlProjectDto> projectDtoGrid = new Grid<>(MlProjectDto.class);



    private final TextField searchField = new TextField("Поиск по имени"); // ✅ Поле поиска

    public MlProjectView(MlProjectService mlProjectService, JwtUtil jwtUtil) {
        this.mlProjectService = mlProjectService;
        this.jwtUtil = jwtUtil;


        Span pageTitle = new Span();
        pageTitle.setHeightFull();
        pageTitle.setTitle("   Панель проекта");


        // ✅ Добавляем строку поиска
        Button searchButton = new Button("Найти", event -> searchCategories());
        Button resetButton = new Button("Сбросить", event -> refreshGrid());
        Button createButton = new Button("Создать", event -> openCreateDialog());
        HorizontalLayout vertCreatPanel = new HorizontalLayout(resetButton, searchField,searchButton);

        HorizontalLayout searchLayout = new HorizontalLayout(createButton, vertCreatPanel, pageTitle);
        add(searchLayout);
        projectDtoGrid.setItems(mlProjectService.getAllProjects());
        projectDtoGrid.setColumns("id", "model", "description", "username");
        projectDtoGrid.addComponentColumn(this::createEditButton).setHeader("Редактировать");
        projectDtoGrid.addComponentColumn(this::createDeleteButton).setHeader("Удалить");
        projectDtoGrid.addComponentColumn(this::createLinkEditButton).setHeader("доступ пользователей");
        projectDtoGrid.addComponentColumn(this::createCategoryLinkEditButton).setHeader("категорический");
        projectDtoGrid.addComponentColumn(this::createFilesLinkEditButton).setHeader("файлы");




        refreshGrid();


        add(projectDtoGrid);
    }



    private void searchCategories() {
        String searchTerm = searchField.getValue().trim();
        if (searchTerm.isEmpty()) {
            refreshGrid();
            return;
        }

        List<MlProjectDto> filteredCategories = mlProjectService.getAllProjects().stream()
                .filter(cat -> cat.getModel().toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());

        projectDtoGrid.setItems(filteredCategories);
    }

    // ✅ Метод для обновления данных в таблице
    private void refreshGrid() {
        List<MlProjectDto> categories = mlProjectService.getAllProjects();
        projectDtoGrid.setItems(categories);
    }

    // ✅ Создаёт кнопку "Редактировать"
    private Button createEditButton(MlProjectDto dto) {
        return new Button("Редактировать", event -> openEditDialog(dto));
    }

    private Button createDeleteButton(MlProjectDto dto) {
        Button deleteButton = new Button("Удалить", event -> openDeleteDialog(dto));
        deleteButton.getStyle().set("color", "red"); // ✅ Красная кнопка удаления
        return deleteButton;
    }

    private void openEditDialog(MlProjectDto dto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Создать главную категорию");

        TextField modelField = new TextField("модель");
        modelField.setValue(dto.getModel());
        TextField descriptionField = new TextField("описание");
        descriptionField.setValue(dto.getDescription());

        Button saveButton = new Button("Сохранить", event -> {
            String model = modelField.getValue().trim();
            String description = descriptionField.getValue() != null ? descriptionField.getValue().trim() : "";


            if (model.isBlank() || description.isBlank()) {
                Notification.show("Ошибка: Все поля должны быть заполнены!", 3000, Notification.Position.MIDDLE);
                return;
            }

            // ✅ Создаём новый объект DTO и передаём в сервис
            MlProjectDto newDto = MlProjectDto.builder()
            .id(dto.getId())
            .model(model)
            .description(description)
            .username(getUserName())
                    .accessUsers(new HashSet<>())
                    .categories(new HashSet<>())
                    .build();

            mlProjectService.updateProject(dto.getId(),newDto);
            refreshGrid();
            dialog.close();
            Notification.show("Категория создана!");
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());

        dialog.add(new VerticalLayout(modelField, descriptionField, new HorizontalLayout(saveButton, cancelButton)));
        dialog.open();
    }


    private void openDeleteDialog(MlProjectDto dto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Удалить пользователя?");

        // ✅ Описание с именем пользователя
        VerticalLayout content = new VerticalLayout();
        content.add("Вы уверены, что хотите удалить категорию " + dto.getModel() + "?");

        // ✅ Кнопка подтверждения удаления
        Button confirmButton = new Button("Да", event -> {

            if (dto.getId() != null) {
                mlProjectService.deleteProject(dto.getId());
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


    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Создать главную категорию");

        TextField modelField = new TextField("модель");
        TextField descriptionField = new TextField("описание");


        Button saveButton = new Button("Сохранить", event -> {
            String model = modelField.getValue().trim();
            String description = descriptionField.getValue() != null ? descriptionField.getValue().trim() : "";


            if (model.isBlank() || description.isBlank()) {
                Notification.show("Ошибка: Все поля должны быть заполнены!", 3000, Notification.Position.MIDDLE);
                return;
            }

            // ✅ Создаём новый объект DTO и передаём в сервис
            MlProjectDto newDto = MlProjectDto.builder()
                    .model(model)
                    .description(description)
                    .username(getUserName())
                    .accessUsers(new HashSet<>())
                    .categories(new HashSet<>())
                    .build();

            mlProjectService.createProject(newDto);
            refreshGrid();
            dialog.close();
            Notification.show("Категория создана!");
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());

        dialog.add(new VerticalLayout(modelField, descriptionField, new HorizontalLayout(saveButton, cancelButton)));
        dialog.open();
    }



    private String getUserName() {
        String token = (String) VaadinSession.getCurrent().getAttribute("JWT_TOKEN");
        return  jwtUtil.getUsername(token);
    }

    private Button createLinkEditButton(MlProjectDto dto) {
        return new Button("участники", event ->
                getUI().ifPresent(ui -> ui.navigate("project/" + dto.getId()))
        );
    }

    private Button createCategoryLinkEditButton(MlProjectDto dto) {
        return new Button("категории", event ->
                getUI().ifPresent(ui -> ui.navigate("project-category/" + dto.getId()))
        );
    }

    private Button createFilesLinkEditButton(MlProjectDto dto) {
        return new Button("файлы", event ->
                getUI().ifPresent(ui -> ui.navigate("project-files/" + dto.getId()))
        );
    }
}
