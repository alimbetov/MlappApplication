package kz.mlapp.enums;


public enum FileStatusName {
    DRAFT,           // Черновик, еще не обработан
    SEARCH,          // Поиск или предобработка
    SEGMENTATION,    // Сегментация изображения
    VALIDATION,      // Валидация данных или модели
    EXCLUDE,         // Исключен из процесса
    TRAIN,           // Используется в обучении (общий статус)
    TRAIN_POSITIVE,  // Позитивный пример для модели
    TRAIN_NEGATIVE,  // Негативный пример для модели
    TEST_POSITIVE,   // Позитивный пример для тестирования
    TEST_NEGATIVE,   // Негативный пример для тестирования
    READY            // Данные готовы к использованию
}
