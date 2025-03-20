package kz.mlapp.ui.pages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import kz.mlapp.dto.AuthRequestDto;
import kz.mlapp.services.impls.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Route("register")
@PageTitle("Регистрация")
@AnonymousAllowed // ✅ Разрешаем доступ без авторизации
public class RegisterView extends VerticalLayout {

    @Autowired
    private UserService userService;

    public RegisterView() {
        EmailField emailField = new EmailField("Email");
        PasswordField passwordField = new PasswordField("Пароль");
        Button registerButton = new Button("Зарегистрироваться", event -> register(emailField.getValue(), passwordField.getValue()));

        add(emailField, passwordField, registerButton);
    }

    private void register(String email, String password) {
        AuthRequestDto requestDto = new AuthRequestDto();
        requestDto.setEmail(email);
        requestDto.setPassword(password);

            if (userService.findByEmail(requestDto.getEmail()).isPresent()) {
                Notification.show("Пользователь зарегестрирован.");
            }
            String resp = "Регистрация успешна";
            try {
                userService.createUser(requestDto);
                Notification.show("Регистрация успешна! Теперь войдите в систему.");
                getUI().ifPresent(ui -> ui.navigate("login")); // Перенаправляем на логин
            } catch (Exception e) {
                Notification.show("Ошибка сервера: " + e.getMessage());
            }

    }
}