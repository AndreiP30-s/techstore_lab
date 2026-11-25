package com.example.techstore.exception;

public enum ErrorCode {
    PRODUCT_NOT_FOUND("Проверьте артикул по каталогу."),
    USER_NOT_FOUND("Проверьте email пользователя."),
    BRAND_NOT_FOUND("Проверьте название по списку брендов."),
    CATEGORY_NOT_FOUND("Выберите категорию из справочника."),
    CHARACTERISTIC_NOT_FOUND("Проверьте список характеристик категории."),
    DUPLICATE_EMAIL("Укажите другой email."),
    DUPLICATE_BRAND("Укажите другое название бренда."),
    BRAND_IN_USE("Сначала назначьте товарам другой бренд."),
    DISCOUNT_ALREADY_ACTIVE("Отмените действующую скидку."),
    NO_ACTIVE_DISCOUNT("Проверьте состояние скидки перед отменой."),
    CATALOG_LOCKED_BY_DISCOUNT("Отмените действующую скидку перед изменением каталога."),
    EMPTY_CATALOG("Добавьте хотя бы один товар."),
    DISCOUNT_PRODUCT_NOT_FOUND("Обратитесь к администратору для проверки данных скидки."),
    DATA_CONFLICT("Проверьте уникальность данных и связанные записи."),
    INVALID_PRICE_RANGE("Укажите неотрицательные цены и minPrice не больше maxPrice, например minPrice=10&maxPrice=100."),
    INVALID_PAGINATION("Укажите page от 0 и size от 1 до 100."),
    EMPTY_UPDATE("Передайте хотя бы одно поле, например price."),
    INVALID_EMAIL("Укажите email в формате name@example.com."),
    PASSWORD_TOO_LONG("Выберите пароль длиной не более 72 байт в UTF-8."),
    INVALID_DISCOUNT_PERCENT("Укажите процент от 0 до 100."),
    INVALID_DISCOUNT_RESULT("Уменьшите процент скидки, чтобы итоговые цены остались положительными."),
    INVALID_THREAD_COUNT("Укажите целое число потоков от 1 до 32."),
    UNKNOWN_CATEGORY("Выберите категорию из справочника."),
    INVALID_CAMERA_CONFIGURATION("Проверьте количество и группы задних камер для выбранной категории."),
    MISSING_CHARACTERISTIC("Заполните обязательные характеристики категории."),
    INVALID_CHARACTERISTIC_VALUE("Передайте значение в формате, указанном в описании характеристики."),
    CHARACTERISTIC_NOT_ALLOWED("Уберите характеристику или выберите подходящую категорию."),
    INVALID_PARAMETER_TYPE("Проверьте тип параметра в документации API."),
    MISSING_PARAMETER("Добавьте обязательный параметр в запрос."),
    MALFORMED_REQUEST("Проверьте синтаксис JSON и типы полей в теле запроса."),
    VALIDATION_FAILED("Исправьте поля, перечисленные в details."),
    PRICE_CALCULATION_FAILED("Повторите запрос позже. Если ошибка повторится, обратитесь к администратору."),
    DISCOUNT_CONFIGURATION_ERROR("Обратитесь к администратору для проверки настройки скидок."),
    METHOD_NOT_ALLOWED("Выберите HTTP-метод из заголовка Allow."),
    UNSUPPORTED_MEDIA_TYPE("Укажите Content-Type, который поддерживает этот эндпоинт."),
    DATABASE_UNAVAILABLE("Повторите запрос позже. Если ошибка повторится, обратитесь к администратору.");

    private final String hint;

    ErrorCode(String hint) {
        this.hint = hint;
    }

    public String getHint() {
        return hint;
    }
}
