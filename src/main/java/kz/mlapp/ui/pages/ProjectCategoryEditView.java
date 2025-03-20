package kz.mlapp.ui.pages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import kz.mlapp.dto.CategoryDto;
import kz.mlapp.dto.MlProjectDto;
import kz.mlapp.service.CategoryService;
import kz.mlapp.service.MlProjectService;
import kz.mlapp.ui.pages.layout.MainLayout;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@PageTitle("Редактирование проекта")
@Route(value = "project-category/:id", layout = MainLayout.class)
@RolesAllowed("ROLE_ADMIN") // ✅ Только админ может редактировать проекты
public class ProjectCategoryEditView extends VerticalLayout implements BeforeEnterObserver {

    private final MlProjectService mlProjectService;
    private final CategoryService categoryService;

    private Long projectId;
    private MlProjectDto projectDto;

    private final TextField modelField = new TextField("🔹 Модель проекта");
    private final TextField descriptionField = new TextField("📝 Описание проекта");

    private final Grid<CategoryDto> accessUsersGrid = new Grid<>(CategoryDto.class, false);
    private final Grid<CategoryDto> accessNoUsersGrid = new Grid<>(CategoryDto.class, false);

    // ✅ Поля для поиска
    private final TextField searchUsersField = new TextField("🔍 Поиск в 'Имеют доступ'");
    private final TextField searchNoUsersField = new TextField("🔍 Поиск в 'Нет доступа'");

    // ✅ Данные для фильтрации
    private List<CategoryDto> accessUsersList;
    private List<CategoryDto> accessNoUsersList;

    public ProjectCategoryEditView(MlProjectService mlProjectService, CategoryService categoryService) {
        this.mlProjectService = mlProjectService;
        this.categoryService = categoryService;

        configureGrids(); // ✅ Конфигурируем таблицы категорий

        // ✅ Добавляем фильтры
        searchUsersField.addValueChangeListener(e -> filterAccessUsers());
        searchNoUsersField.addValueChangeListener(e -> filterAccessNoUsers());

        // ✅ Раздел с заголовком и основными полями
        VerticalLayout projectInfoLayout = new VerticalLayout(
                modelField,
                descriptionField
        );
        projectInfoLayout.setPadding(true);
        projectInfoLayout.setSpacing(true);

        // ✅ Кнопки управления доступом
        Button addButton = new Button("➕ Добавить", VaadinIcon.PLUS_CIRCLE.create(), event -> addCategories());
        Button removeButton = new Button("➖ Удалить", VaadinIcon.MINUS_CIRCLE.create(), event -> removeCategories());

        HorizontalLayout buttonLayout = new HorizontalLayout(addButton, removeButton);
        buttonLayout.setSpacing(true);
        buttonLayout.setAlignItems(Alignment.CENTER);

        // ✅ Кнопки сохранения и отмены
        Button saveButton = new Button("💾 Сохранить", VaadinIcon.CHECK.create(), event -> saveProject());
        Button cancelButton = new Button("❌ Отмена", VaadinIcon.CLOSE.create(), event -> getUI().ifPresent(ui -> ui.navigate("projects")));

        saveButton.getStyle().set("background-color", "#4CAF50").set("color", "white");
        cancelButton.getStyle().set("background-color", "#F44336").set("color", "white");

        HorizontalLayout actionButtons = new HorizontalLayout(saveButton, cancelButton);
        actionButtons.setSpacing(true);

        // ✅ Заголовки таблиц
        VerticalLayout accessUsersLayout = new VerticalLayout(
                new HorizontalLayout(VaadinIcon.CHECK_SQUARE.create(), new TextField("✅ Имеют доступ", "", "")),
                searchUsersField,
                accessUsersGrid
        );

        VerticalLayout accessNoUsersLayout = new VerticalLayout(
                new HorizontalLayout(VaadinIcon.CLOSE_CIRCLE.create(), new TextField("❌ Нет доступа", "", "")),
                searchNoUsersField,
                accessNoUsersGrid
        );

        // ✅ Компоновка (Флекс-контейнер)
        FlexLayout gridLayout = new FlexLayout(accessUsersLayout, accessNoUsersLayout);
        gridLayout.setSizeFull();
        gridLayout.setFlexGrow(1, accessUsersLayout, accessNoUsersLayout);
        gridLayout.getStyle().set("gap", "16px");

        add(projectInfoLayout, buttonLayout, actionButtons, gridLayout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String id = event.getRouteParameters().get("id").orElse(null);
        if (id != null) {
            projectId = Long.parseLong(id);
            loadProject(projectId);
        } else {
            Notification.show("Ошибка: ID проекта не найден!", 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("projects"));
        }
    }

    private void loadProject(Long id) {
        projectDto = mlProjectService.getProjectById(id);
        if (projectDto != null) {
            modelField.setValue(projectDto.getModel());
            descriptionField.setValue(projectDto.getDescription());
            loadCategories();
        } else {
            Notification.show("Ошибка: Проект не найден!", 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("projects"));
        }
    }

    private void configureGrids() {
        accessUsersGrid.addColumn(CategoryDto::getName).setHeader("Категория");
        accessUsersGrid.addColumn(CategoryDto::getParentName).setHeader("Группа");
        accessUsersGrid.setSelectionMode(Grid.SelectionMode.MULTI);

        accessNoUsersGrid.addColumn(CategoryDto::getName).setHeader("Категория");
        accessNoUsersGrid.addColumn(CategoryDto::getParentName).setHeader("Группа");
        accessNoUsersGrid.setSelectionMode(Grid.SelectionMode.MULTI);
    }

    private void loadCategories() {
        Set<Long> accessCategoryIds = projectDto.getCategories().stream()
                .map(CategoryDto::getId)
                .collect(Collectors.toSet());

        List<CategoryDto> allCategories = categoryService.getAllDtoCategories();
        accessUsersList = allCategories.stream()
                .filter(cat -> accessCategoryIds.contains(cat.getId()))
                .collect(Collectors.toList());
        accessNoUsersList = allCategories.stream()
                .filter(cat -> !accessCategoryIds.contains(cat.getId()))
                .collect(Collectors.toList());

        filterAccessUsers();
        filterAccessNoUsers();
    }

    private void filterAccessUsers() {
        String filterText = searchUsersField.getValue().trim().toLowerCase();
        List<CategoryDto> filteredList = accessUsersList.stream()
                .filter(cat -> cat.getName().toLowerCase().contains(filterText))
                .collect(Collectors.toList());
        accessUsersGrid.setItems(filteredList);
    }

    private void filterAccessNoUsers() {
        String filterText = searchNoUsersField.getValue().trim().toLowerCase();
        List<CategoryDto> filteredList = accessNoUsersList.stream()
                .filter(cat -> cat.getName().toLowerCase().contains(filterText))
                .collect(Collectors.toList());
        accessNoUsersGrid.setItems(filteredList);
    }

    private void addCategories() {
        Set<CategoryDto> selectedCategories = accessNoUsersGrid.getSelectedItems();
        projectDto.getCategories().addAll(selectedCategories);
        loadCategories();
    }

    private void removeCategories() {
        Set<CategoryDto> selectedCategories = accessUsersGrid.getSelectedItems();
        projectDto.getCategories().removeAll(selectedCategories);
        loadCategories();
    }

    private void saveProject() {
        projectDto.setModel(modelField.getValue().trim());
        projectDto.setDescription(descriptionField.getValue().trim());
        mlProjectService.updateProject(projectId, projectDto);
        Notification.show("Проект обновлен!", 3000, Notification.Position.MIDDLE);
        getUI().ifPresent(ui -> ui.navigate("projects"));
    }
}
