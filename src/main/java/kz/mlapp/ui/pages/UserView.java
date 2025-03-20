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
import kz.mlapp.dto.UserDto;
import kz.mlapp.enums.RoleName;
import kz.mlapp.services.impls.UserService;
import kz.mlapp.ui.pages.layout.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Route(value = "users", layout = MainLayout.class)
@PageTitle("Пользователи")
@RolesAllowed("ROLE_ADMIN") // ✅ Ограничиваем доступ только для администраторов
public class UserView extends VerticalLayout {

    @Autowired
    private UserService userService;
    private final Grid<UserDto> userGrid = new Grid<>(UserDto.class);
    private final TextField searchField = new TextField("Поиск по имени"); // ✅ Поле поиска

    public UserView(UserService userService) {
        this.userService = userService;

        Span pageTitle = new Span();
        pageTitle.setHeightFull();
        pageTitle.setTitle("   Панель пользователей");



        // ✅ Добавляем строку поиска
        Button searchButton = new Button("Найти", event -> searchUsers());
        Button resetButton = new Button("Сбросить", event -> refreshGrid());

        HorizontalLayout searchLayout = new HorizontalLayout(searchField, searchButton, resetButton, pageTitle);
        add(searchLayout);


        userGrid.setItems(userService.getAllUsers());
        userGrid.setColumns("id", "username", "roles");
        userGrid.addColumn(UserDto::getBlockState).setHeader("Статус блокировки"); //
        userGrid.addComponentColumn(this::createEditButton).setHeader("Редактировать");
        userGrid.addComponentColumn(this::createDeleteButton).setHeader("Удалить");

        refreshGrid();

        add(userGrid);
    }


    // ✅ Метод для поиска пользователей по `username`
    private void searchUsers() {
        String searchTerm = searchField.getValue().trim();
        if (searchTerm.isEmpty()) {
            refreshGrid();
            return;
        }

        List<UserDto> filteredUsers = userService.getAllUsers().stream()
                .filter(user -> user.getUsername().toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());

        userGrid.setItems(filteredUsers);
    }

    // ✅ Метод для обновления данных в таблице
    private void refreshGrid() {
        List<UserDto> users = userService.getAllUsers();
        userGrid.setItems(users);
    }

    // ✅ Создаёт кнопку "Редактировать"
    private Button createEditButton(UserDto user) {
        return new Button("Редактировать", event -> openEditDialog(user));
    }

    // ✅ Создаёт кнопку "Удалить"

    private Button createDeleteButton(UserDto user) {
        Button deleteButton = new Button("Удалить", event -> openDeleteDialog(user));
        deleteButton.getStyle().set("color", "red"); // ✅ Красная кнопка удаления
        return deleteButton;
    }



    // ✅ Диалоговое окно для редактирования пользователя
    private void openEditDialog(UserDto user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Редактировать пользователя");

        TextField usernameField = new TextField("Имя пользователя");
        usernameField.setValue(user.getUsername());

        Checkbox checkbox = new Checkbox("Статус");
        checkbox.setValue(user.isBlocked());

        // ✅ Создаём CheckBoxGroup для выбора ролей
        CheckboxGroup<RoleName> roleCheckboxGroup = new CheckboxGroup<>();
        roleCheckboxGroup.setLabel("Доступные роли");
        roleCheckboxGroup.setItems(RoleName.values());

        // ✅ Преобразуем `Set<String>` в `Set<RoleName>`
        Set<RoleName> selectedRoles = user.getRoles().stream()
                .map(RoleName::valueOf) // Конвертируем строки в Enum
                .collect(Collectors.toSet());
        roleCheckboxGroup.setValue(selectedRoles);



        Button saveButton = new Button("Сохранить", event -> {
            user.setUsername(usernameField.getValue());
            user.setBlocked(checkbox.getValue());


            // ✅ Преобразуем `Set<RoleName>` обратно в `Set<String>`
            Set<String> updatedRoles =
                    roleCheckboxGroup.getValue().stream()
                    .map(Enum::name)
                    .collect(Collectors.toSet());
            user.setRoles(updatedRoles);
            userService.updateUser(user.getId(), user);
            refreshGrid();
            dialog.close();
            Notification.show("Данные обновлены!");
        });
        Button cancelButton = new Button("Отмена", event -> dialog.close());
        dialog.add(new VerticalLayout(usernameField, checkbox,roleCheckboxGroup, new HorizontalLayout(saveButton, cancelButton)));
        dialog.open();
    }

    // ✅ Диалог подтверждения удаления пользователя
    private void openDeleteDialog(UserDto user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Удалить пользователя?");

        // ✅ Описание с именем пользователя
        VerticalLayout content = new VerticalLayout();
        content.add("Вы уверены, что хотите удалить пользователя " + user.getUsername() + "?");

        // ✅ Кнопка подтверждения удаления
        Button confirmButton = new Button("Да", event -> {
            userService.deleteUser(user.getId());
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

}
