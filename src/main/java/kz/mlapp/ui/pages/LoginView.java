package kz.mlapp.ui.pages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import kz.mlapp.dto.AuthRequestDto;
import kz.mlapp.dto.AuthResponseDto;
import kz.mlapp.services.impls.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route("login")
@PageTitle("Вход")
@AnonymousAllowed // ✅ Разрешаем доступ без авторизации
public class LoginView extends VerticalLayout {

    @Autowired
    private UserService userService;
    public LoginView() {
        EmailField emailField = new EmailField("Email");
        PasswordField passwordField = new PasswordField("Пароль");
        Button loginButton = new Button("Войти", event -> login(emailField.getValue(), passwordField.getValue()));
        Button registerButton = new Button("Регистрация", event -> getUI().ifPresent(ui -> ui.navigate("register"))); // ✅ Добавили кнопку "Регистрация"

        add(emailField, passwordField, loginButton, registerButton);

    }

    private void login(String email, String password) {
        AuthRequestDto requestDto = new AuthRequestDto();
        requestDto.setEmail(email);
        requestDto.setPassword(password);

        try {
            ResponseEntity<AuthResponseDto> response = userService.login(requestDto);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String token = response.getBody().getToken();
                VaadinSession.getCurrent().setAttribute("JWT_TOKEN", token);
                // ✅ Преобразуем List в Set перед сохранением
                Set<String> roleSet = response.getBody().getRoles().stream().collect(Collectors.toSet());
                VaadinSession.getCurrent().setAttribute("USER_ROLES", roleSet);

                getUI().ifPresent(ui -> ui.navigate("")); // Перенаправляем на главную
            } else {
                Notification.show("Ошибка входа: Неверный email или пароль");
            }
        } catch (Exception e) {
            Notification.show("Ошибка сервера: " + e.getMessage());
        }
    }
}
