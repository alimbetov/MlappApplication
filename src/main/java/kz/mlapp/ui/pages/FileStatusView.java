package kz.mlapp.ui.pages;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import kz.mlapp.dto.FileStatusDto;
import kz.mlapp.enums.FileStatusName;
import kz.mlapp.service.FileStatusService;
import kz.mlapp.ui.pages.layout.MainLayout;

import java.util.List;

@PageTitle("Статусы файлов")
@Route(value = "file-statuses", layout = MainLayout.class)
@RolesAllowed("ROLE_ADMIN") // ✅ Только админ может управлять статусами
public class FileStatusView extends VerticalLayout {

    private final FileStatusService fileStatusService;
    private final Grid<FileStatusDto> statusGrid = new Grid<>(FileStatusDto.class);
    private final ComboBox<FileStatusName> statusComboBox = new ComboBox<>("Статус файла");

    public FileStatusView(FileStatusService fileStatusService) {
        this.fileStatusService = fileStatusService;

        configureGrid();
        configureForm();

        Button addButton = new Button("Добавить статус", event -> addStatus());
        Button deleteButton = new Button("Удалить выбранный", event -> deleteStatus());

        add(statusComboBox, new HorizontalLayout(addButton, deleteButton), statusGrid);
        refreshGrid();
    }

    private void configureGrid() {
        statusGrid.setColumns("id", "status");
        statusGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
    }

    private void configureForm() {
        statusComboBox.setItems(FileStatusName.values());
    }

    private void refreshGrid() {
        List<FileStatusDto> statuses = fileStatusService.getAllStatuses();
        statusGrid.setItems(statuses);
    }

    private void addStatus() {
        FileStatusName selectedStatus = statusComboBox.getValue();
        if (selectedStatus == null) {
            Notification.show("Выберите статус перед добавлением!");
            return;
        }

        fileStatusService.createStatus(FileStatusDto.builder().status(selectedStatus).build());
        refreshGrid();
        Notification.show("Статус добавлен!");
    }

    private void deleteStatus() {
        FileStatusDto selectedStatus = statusGrid.asSingleSelect().getValue();
        if (selectedStatus == null) {
            Notification.show("Выберите статус для удаления!");
            return;
        }

        fileStatusService.deleteStatus(selectedStatus.getId());
        refreshGrid();
        Notification.show("Статус удалён!");
    }
}
