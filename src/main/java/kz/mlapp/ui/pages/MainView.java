package kz.mlapp.ui.pages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import kz.mlapp.ui.pages.layout.MainLayout;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Главная")
public class MainView extends VerticalLayout {
    public MainView() {
        add(new Button("Нажми меня", e -> Notification.show("Привет, Vaadin!")));
    }
}

/*


public class MainView extends VerticalLayout {
    private final SecurityService securityService;

    public MainView(SecurityService securityService) {
        this.securityService = securityService;

        Button button = new Button("Нажми меня", e -> Notification.show("Привет, Vaadin!"));
        add(button);

        String currentUser = securityService.getAuthenticatedUser();
        if (currentUser != null) {
            TextField userField = new TextField("Вы вошли как:", currentUser);
            userField.setReadOnly(true);
            Button logoutButton = new Button("Выйти", event -> {
                securityService.logout();
                getUI().ifPresent(ui -> ui.getPage().reload());
            });
            add(new HorizontalLayout(userField, logoutButton));
        }
    }
}
*/
