package kz.mlapp.ui.pages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import kz.mlapp.dto.FileBoxDto;
import kz.mlapp.dto.FileStatusDto;
import kz.mlapp.dto.MlProjectDto;
import kz.mlapp.security.JwtUtil;
import kz.mlapp.service.FileBoxService;
import kz.mlapp.service.FileStatusService;
import kz.mlapp.service.MlProjectService;
import kz.mlapp.services.impls.UserService;
import kz.mlapp.ui.pages.layout.MainLayout;
import kz.mlapp.utils.VaadinMultipartFile;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@PageTitle("Файлы проекта")
@Route(value = "project-files/:id", layout = MainLayout.class)
@RolesAllowed("ROLE_ADMIN") // ✅ Только админ может просматривать файлы проекта
public class ProjectFilesView extends VerticalLayout implements BeforeEnterObserver {

    private final FileBoxService fileBoxService;
    private final FileStatusService fileStatusService;
    private final JwtUtil jwtUtil;
    private MlProjectService mlProjectService;


    private UserService userService;

    private Long projectId;  // ID проекта
    private Long statusId = 1L; // ❗ По умолчанию статус = 1 (можно передавать через параметры)
    private int currentPage = 0;
    private final int pageSize = 10;

    private final Grid<MlProjectDto> projectDtoGrid = new Grid<>(MlProjectDto.class);
    private final Grid<FileBoxDto> fileGrid = new Grid<>(FileBoxDto.class, false);
    private final TextField searchField = new TextField("🔍 Поиск по файлу");
    private final ComboBox<FileStatusDto> statusComboBox = new ComboBox<>("📌 Статус файлов");

    public ProjectFilesView(FileBoxService fileBoxService,
                            FileStatusService fileStatusService,
                            MlProjectService mlProjectService,
                            JwtUtil jwtUtil, UserService userService ) {
        this.fileBoxService = fileBoxService;
        this.fileStatusService = fileStatusService;
        this.mlProjectService = mlProjectService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;




        configureGrid(); // ✅ Конфигурируем таблицу
        configureStatusComboBox(); // ✅ Комбо-бокс статусов

        // ✅ Кнопки навигации (пагинация)
        Button prevPageButton = new Button("⬅️ Назад", event -> changePage(-1));
        Button nextPageButton = new Button("Вперёд ➡️", event -> changePage(1));
        prevPageButton.setEnabled(false);
        // ✅ Кнопка "Добавить файл"
        Button addFileButton = new Button("📂 Добавить файл", event -> openFileUploadDialog());

        // ✅ Поле поиска
        searchField.addValueChangeListener(e -> filterFiles());

        // ✅ Статус + пагинация
        HorizontalLayout controls = new HorizontalLayout(statusComboBox, prevPageButton, nextPageButton, searchField, addFileButton);
        add(projectDtoGrid, controls, fileGrid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String id = event.getRouteParameters().get("id").orElse(null);
        if (id != null) {
            projectId = Long.parseLong(id);
            try {
                MlProjectDto projectDto = mlProjectService.getProjectById(projectId); // ✅ Получаем DTO проекта
                projectDtoGrid.setItems(List.of(projectDto)); // ✅ Загружаем в Grid
                projectDtoGrid.setColumns("id", "model", "description");
                projectDtoGrid.setHeight("100px");
                loadFiles();
            } catch (RuntimeException e) {
                Notification.show("Ошибка: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
                getUI().ifPresent(ui -> ui.navigate("projects"));
            }
        } else {
            Notification.show("Ошибка: ID проекта не найден!", 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("projects"));
        }
    }

    // ✅ Настраиваем таблицу
    private void configureGrid() {
        fileGrid.addColumn(FileBoxDto::getFilename).setHeader("📄 Имя файла");
        fileGrid.addColumn(FileBoxDto::getDescription).setHeader("📌 Описание");
        fileGrid.addColumn(FileBoxDto::getCreatedAt).setHeader("📅 Дата загрузки");
        fileGrid.addComponentColumn(this::createDeleteButton).setHeader("🗑 Удалить");
    }

    // ✅ Конфигурируем выпадающий список статусов
    private void configureStatusComboBox() {
        List<FileStatusDto> statuses = fileStatusService.getAllStatuses();
        statusComboBox.setItems(statuses);;
        statusComboBox.setItemLabelGenerator(status -> status.getStatus().name());
        statusComboBox.setValue(statuses.stream().findFirst().orElse(null));

        // ✅ При изменении статуса загружаем файлы
        statusComboBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                statusId = event.getValue().getId();
                loadFiles();
            }
        });
    }

    // ✅ Загружаем файлы по `projectId` и `statusId`
    private void loadFiles() {
        Page<FileBoxDto> filePage = fileBoxService.getFilesByProjectAndStatus(projectId, statusId, currentPage, pageSize);
        List<FileBoxDto> fileList = filePage.getContent();

        fileGrid.setItems(fileList);
    }

    // ✅ Фильтрация файлов по названию
    private void filterFiles() {
        String searchTerm = searchField.getValue().trim().toLowerCase();
        Page<FileBoxDto> filePage = fileBoxService.getFilesByProjectAndStatus(projectId, statusId, currentPage, pageSize);
        List<FileBoxDto> filteredFiles = filePage.getContent().stream()
                .filter(file -> file.getFilename().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());

        fileGrid.setItems(filteredFiles);
    }

    // ✅ Управление страницами
    private void changePage(int step) {
        currentPage += step;
        loadFiles();
    }

    // ✅ Кнопка удаления файла
    private Button createDeleteButton(FileBoxDto fileDto) {
        return new Button("🗑 Удалить", event -> {
            fileBoxService.deleteFile(fileDto.getId());
            Notification.show("Файл удалён!", 3000, Notification.Position.MIDDLE);
            loadFiles();
        });
    }

    // ✅ Диалоговое окно для загрузки файла (моковая загрузка)


    private void openFileUploadDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("📂 Добавить файл");

        TextField filenameField = new TextField("Имя файла");
        TextField descriptionField = new TextField("Описание");

        // ✅ Компонент для загрузки файла
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/jpg", ".dcm"); // ❗ Поддержка DICOM + JPEG
        upload.setMaxFiles(1);

        // ✅ Поле для предпросмотра изображения
        Image previewImage = new Image();
        previewImage.setMaxWidth("200px");
        previewImage.setVisible(false);

        AtomicReference<String> mimeType = new AtomicReference<>("");

        // ✅ Получение пользователя
        String userName = getUserName();
        var userDto = Optional.ofNullable(userService.findUserDtoByEmail(userName))
                .orElseThrow(() -> new RuntimeException("Ошибка: Пользователь не найден!"));

        // ✅ Обработчик успешной загрузки файла
        upload.addSucceededListener(event -> {
            filenameField.setValue(event.getFileName()); // Автозаполнение имени файла
            mimeType.set(event.getMIMEType()); // ✅ Устанавливаем MIME-тип
            String fileName = event.getFileName();

            // ✅ Проверяем, является ли файл изображением или DICOM
            if (mimeType.get().startsWith("image")) {
                previewImage.setSrc(new StreamResource(fileName, buffer::getInputStream));
                previewImage.setVisible(true);
            } else if (fileName.endsWith(".dcm")) {
                Notification.show("Файл DICOM загружен, предпросмотр недоступен.", 3000, Notification.Position.MIDDLE);
                previewImage.setVisible(false);
            }
        });

        // ✅ Кнопка сохранения
        Button saveButton = new Button("Сохранить", event -> {
            String filename = filenameField.getValue().trim();
            String description = descriptionField.getValue().trim();

            if (filename.isEmpty()) {
                Notification.show("Ошибка: Укажите имя файла!", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                // ✅ Конвертируем в MultipartFile
                VaadinMultipartFile multipartFile = new VaadinMultipartFile(buffer, mimeType.get());

                // ✅ Генерация ключей
                String fileKey = UUID.randomUUID().toString();
                String parentKey = "project-" + projectId; // ✅ Теперь уникальный ключ

                FileBoxDto newFile = FileBoxDto.builder()
                        .projectId(projectId)
                        .uploadedUserId(userDto.getId())
                        .statusId(statusId)
                        .fileKey(fileKey)
                        .parentKey(parentKey)
                        .filename(filename)
                        .description(description)
                        .fileType(multipartFile.getContentType())
                        .fileSize(multipartFile.getSize())
                        .createdAt(LocalDateTime.now())
                        .build();

                fileBoxService.saveFile(newFile, multipartFile);
                loadFiles();
                dialog.close();
                Notification.show("Файл загружен!", 3000, Notification.Position.MIDDLE);
            } catch (IOException e) {
                Notification.show("Ошибка загрузки файла: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());

        dialog.add(new VerticalLayout(
                upload, previewImage, filenameField, descriptionField,
                new HorizontalLayout(saveButton, cancelButton)
        ));
        dialog.open();
    }

    private String getUserName() {
        String token = (String) VaadinSession.getCurrent().getAttribute("JWT_TOKEN");
        return  jwtUtil.getUsername(token);
    }
}
