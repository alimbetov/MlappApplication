package kz.mlapp.ui.pages;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import kz.mlapp.ui.pages.layout.MainLayout;

@Route(value = "settings", layout = MainLayout.class)
@PageTitle("Настройки")
public class SettingsView extends VerticalLayout {
    public SettingsView() {
        add(new Paragraph("Настройки пока недоступны"));
    }
}
