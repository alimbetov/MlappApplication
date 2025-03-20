package kz.mlapp.ui.pages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import kz.mlapp.dto.MlProjectDto;
import kz.mlapp.dto.UserDto;
import kz.mlapp.service.MlProjectService;
import kz.mlapp.services.impls.UserService;
import kz.mlapp.ui.pages.layout.MainLayout;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@PageTitle("Редактировать проект")
@Route(value = "project/:id", layout = MainLayout.class)
@RolesAllowed("ROLE_ADMIN") // ✅ Только админ может редактировать проекты
public class ProjectAccessEditView extends VerticalLayout implements BeforeEnterObserver {

    private final MlProjectService mlProjectService;
    private final UserService userService;

    private Long projectId;
    private MlProjectDto projectDto;

    private final TextField modelField = new TextField("Модель");
    private final TextField descriptionField = new TextField("Описание");

    private final Grid<UserDto> accessUsersGrid = new Grid<>(UserDto.class, false);
    private final Grid<UserDto> accessNoUsersGrid = new Grid<>(UserDto.class, false);

    public ProjectAccessEditView(MlProjectService mlProjectService, UserService userService) {
        this.mlProjectService = mlProjectService;
        this.userService = userService;

        // ✅ Конфигурируем таблицы пользователей
        configureUserGrids();

        // ✅ Кнопки управления
        Button addButton = new Button("➕ Добавить", event -> addUsers());
        Button removeButton = new Button("➖ Удалить", event -> removeUsers());
        HorizontalLayout buttonLayout = new HorizontalLayout(addButton, removeButton);
        buttonLayout.setAlignItems(Alignment.CENTER);

        // ✅ Кнопки сохранения и отмены
        Button saveButton = new Button("💾 Сохранить", event -> saveProject());
        Button cancelButton = new Button("❌ Отмена", event -> getUI().ifPresent(ui -> ui.navigate("projects")));

        // ✅ Компоновка (Флекс-контейнер)
        FlexLayout gridLayout = new FlexLayout(accessUsersGrid, accessNoUsersGrid);
        gridLayout.setSizeFull();
        gridLayout.setFlexGrow(1, accessUsersGrid, accessNoUsersGrid);

        HorizontalLayout hPanel = new HorizontalLayout();

        hPanel.add( new HorizontalLayout(modelField, descriptionField,saveButton, cancelButton));
        add(hPanel, buttonLayout, gridLayout);
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
            loadUsers();
        } else {
            Notification.show("Ошибка: Проект не найден!", 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("projects"));
        }
    }

    // ✅ Конфигурируем таблицы пользователей
    private void configureUserGrids() {
        accessUsersGrid.addColumn(UserDto::getUsername).setHeader("✅ Имеют доступ");
        accessUsersGrid.setSelectionMode(Grid.SelectionMode.MULTI);

        accessNoUsersGrid.addColumn(UserDto::getUsername).setHeader("❌ Нет доступа");
        accessNoUsersGrid.setSelectionMode(Grid.SelectionMode.MULTI);
    }

    // ✅ Загружаем данные в таблицы
    private void loadUsers() {
        Set<String> accessUsernames = projectDto.getAccessUsers();

        List<UserDto> allUsers = userService.getAllUsers();
        List<UserDto> accessUsers = allUsers.stream()
                .filter(user -> accessUsernames.contains(user.getUsername()))
                .collect(Collectors.toList());
        List<UserDto> noAccessUsers = allUsers.stream()
                .filter(user -> !accessUsernames.contains(user.getUsername()))
                .collect(Collectors.toList());

        accessUsersGrid.setItems(accessUsers);
        accessNoUsersGrid.setItems(noAccessUsers);
    }

    // ✅ Добавление пользователей в проект
    private void addUsers() {
        Set<UserDto> selectedUsers = accessNoUsersGrid.getSelectedItems();
        if (selectedUsers.isEmpty()) {
            Notification.show("Выберите пользователей для добавления!");
            return;
        }
        Set<String> usernames = selectedUsers.stream().map(UserDto::getUsername).collect(Collectors.toSet());
        projectDto.getAccessUsers().addAll(usernames);
        loadUsers();
    }

    // ✅ Удаление пользователей из проекта
    private void removeUsers() {
        Set<UserDto> selectedUsers = accessUsersGrid.getSelectedItems();
        if (selectedUsers.isEmpty()) {
            Notification.show("Выберите пользователей для удаления!");
            return;
        }
        Set<String> usernames = selectedUsers.stream().map(UserDto::getUsername).collect(Collectors.toSet());
        projectDto.getAccessUsers().removeAll(usernames);
        loadUsers();
    }

    private void saveProject() {
        if (projectDto != null) {
            projectDto.setModel(modelField.getValue().trim());
            projectDto.setDescription(descriptionField.getValue().trim());
            mlProjectService.updateProject(projectId, projectDto);
            Notification.show("Проект обновлен!", 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("projects"));
        }
    }
}
