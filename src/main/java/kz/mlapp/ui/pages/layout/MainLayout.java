package kz.mlapp.ui.pages.layout;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import kz.mlapp.security.JwtUtil;
import kz.mlapp.security.SecurityService;
import kz.mlapp.ui.pages.*;

import java.util.*;

import static com.helger.commons.system.SystemProperties.getUserName;

public class MainLayout extends AppLayout {
    private final SecurityService securityService;
    private final JwtUtil jwtUtil;

    public MainLayout(SecurityService securityService, JwtUtil jwtUtil ) {
        this.securityService = securityService;
        this.jwtUtil=jwtUtil;
        Set<String> roles = new HashSet<>();
        H1 title = new H1("ML App");
        title.getStyle().set("margin", "0").set("padding", "10px");

        SideNav sideNav = new SideNav();
        sideNav.addItem(new SideNavItem("Главная", MainView.class));



        String token = getTokken();
        //var vname = jwtUtil.getUsername(token);


        // ✅ Проверяем, является ли пользователь админом перед добавлением ссылки на "Пользователи"
        if (isAdmin()) {
            sideNav.addItem(new SideNavItem("Пользователи", UserView.class));
            sideNav.addItem(new SideNavItem("Статусы", FileStatusView.class));
            sideNav.addItem(new SideNavItem("Главная категория", SuperCategoryView.class));
            sideNav.addItem(new SideNavItem("Детальная категория", CategoryView.class));
            sideNav.addItem(new SideNavItem("Проект", MlProjectView.class));



        }

        HorizontalLayout topBar = new HorizontalLayout(title);

        if (token != null) {

            var vname = Optional.ofNullable(jwtUtil.getUsername(token)).filter(name -> !name.isEmpty()).orElse("Неизвестный");
            Avatar userAvatar = new Avatar(vname);
            userAvatar.setSizeFull();
            userAvatar.setMaxWidth("50px");


            Button logoutButton = new Button("Выйти", event -> {
                VaadinSession.getCurrent().setAttribute("JWT_TOKEN", null);
                VaadinSession.getCurrent().setAttribute("USER_ROLES", null);
                getUI().ifPresent(ui -> ui.navigate("login"));
            });

            topBar.add(userAvatar, logoutButton);
        } else {
            Button loginButton = new Button("Вход", event -> getUI().ifPresent(ui -> ui.navigate("login")));
            Button registerButton = new Button("Регистрация", event -> getUI().ifPresent(ui -> ui.navigate("register")));
            topBar.add(loginButton, registerButton);
        }

        addToNavbar(topBar);
        addToDrawer(sideNav);
    }


    private boolean isAdmin() {
        Object rolesAttribute = VaadinSession.getCurrent().getAttribute("USER_ROLES");

        if (rolesAttribute instanceof List) {
            // ✅ Преобразуем List в Set, чтобы избежать ClassCastException
            Set<String> roles = new HashSet<>((List<String>) rolesAttribute);
            return roles.contains("ROLE_ADMIN");
        } else if (rolesAttribute instanceof Set) {
            return ((Set<String>) rolesAttribute).contains("ROLE_ADMIN");
        }

        return false;
    }


    private String getTokken() {
        return (String) VaadinSession.getCurrent().getAttribute("JWT_TOKEN");
    }
}

