BEGIN;

-- ---------------------------------------------------------------
-- Бренды
-- ---------------------------------------------------------------
CREATE TABLE brand (
    name varchar(100) PRIMARY KEY CHECK (btrim(name) <> '')
);

-- ---------------------------------------------------------------
-- Категории
-- ---------------------------------------------------------------
CREATE TABLE category (
    code varchar(20) PRIMARY KEY,
    name varchar(30) NOT NULL UNIQUE,

    CHECK ((code, name) IN (
        ('SMARTPHONE', 'Смартфоны'),
        ('TABLET', 'Планшеты'),
        ('LAPTOP', 'Ноутбуки'),
        ('HEADPHONES', 'Наушники')
    ))
);

-- ---------------------------------------------------------------
-- Характеристики
-- ---------------------------------------------------------------
CREATE TABLE specification (
    name       varchar(100) PRIMARY KEY CHECK (btrim(name) <> ''),
    value_type varchar(10)  NOT NULL,
    unit       varchar(20),

    CHECK (value_type IN (
        'INTEGER', 'DECIMAL', 'TEXT', 'BOOLEAN', 'ENUM'
    )),
    CHECK (unit IS NULL OR btrim(unit) <> '')
);

-- ---------------------------------------------------------------
-- Характеристики категорий
-- ---------------------------------------------------------------
CREATE TABLE category_specification (
    category_code      varchar(20)  NOT NULL REFERENCES category (code),
    specification_name varchar(100) NOT NULL REFERENCES specification (name),

    PRIMARY KEY (category_code, specification_name)
);

-- ---------------------------------------------------------------
-- Товары
-- ---------------------------------------------------------------
CREATE TABLE product (
    article       integer      PRIMARY KEY CHECK (article > 0),
    name          varchar(200) NOT NULL CHECK (btrim(name) <> ''),
    price         numeric(9,2) NOT NULL,
    available     boolean      NOT NULL DEFAULT true,
    brand_name    varchar(100) NOT NULL REFERENCES brand (name),
    category_code varchar(20)  NOT NULL REFERENCES category (code),

    CHECK (price > 0 AND price <> 'NaN'::numeric)
);

-- ---------------------------------------------------------------
-- Значения характеристик товаров
-- ---------------------------------------------------------------
CREATE TABLE product_specification (
    product_article    integer      NOT NULL REFERENCES product (article),
    specification_name varchar(100) NOT NULL REFERENCES specification (name),
    value              text         NOT NULL CHECK (btrim(value) <> ''),

    PRIMARY KEY (product_article, specification_name)
);

-- ---------------------------------------------------------------
-- Модули задних камер
-- ---------------------------------------------------------------
CREATE TABLE rear_camera (
    product_article integer      NOT NULL REFERENCES product (article),
    camera_type     varchar(15)  NOT NULL,
    resolution_mp   numeric(8,3) NOT NULL,
    quantity        integer      NOT NULL CHECK (quantity > 0),

    PRIMARY KEY (product_article, camera_type, resolution_mp),

    CHECK (camera_type IN (
        'WIDE', 'ULTRA_WIDE', 'TELEPHOTO', 'MACRO'
    )),
    CHECK (resolution_mp > 0 AND resolution_mp <> 'NaN'::numeric)
);

-- ---------------------------------------------------------------
-- Пользователи
-- ---------------------------------------------------------------
CREATE TABLE app_user (
    email         varchar(254) PRIMARY KEY,
    name          varchar(100) NOT NULL CHECK (btrim(name) <> ''),
    password_hash text         NOT NULL CHECK (btrim(password_hash) <> ''),
    role          varchar(10)  NOT NULL,

    CHECK (role IN ('ADMIN', 'MANAGER', 'CUSTOMER')),
    CHECK (
        email = lower(btrim(email))
        AND email ~ '^[^[:space:]@]+@[^[:space:]@]+[.][^[:space:]@]+$'
    )
);

-- ---------------------------------------------------------------
-- Заказы
-- ---------------------------------------------------------------
CREATE TABLE sales_order (
    order_number    varchar(30)  PRIMARY KEY CHECK (btrim(order_number) <> ''),
    user_email      varchar(254) NOT NULL REFERENCES app_user (email),
    created_at      timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status          varchar(15)  NOT NULL DEFAULT 'NEW',
    recipient_name  varchar(100) NOT NULL CHECK (btrim(recipient_name) <> ''),
    recipient_phone varchar(16)  NOT NULL,
    completed_at    timestamp with time zone,

    CHECK (status IN (
        'NEW', 'CONFIRMED', 'READY', 'ISSUED', 'CANCELLED'
    )),
    CHECK (recipient_phone ~ '^[+][1-9][0-9]{0,14}$'),
    CHECK (
        (
            status IN ('ISSUED', 'CANCELLED')
            AND completed_at IS NOT NULL
            AND completed_at >= created_at
        )
        OR
        (
            status IN ('NEW', 'CONFIRMED', 'READY')
            AND completed_at IS NULL
        )
    )
);

-- ---------------------------------------------------------------
-- Позиции заказов
-- ---------------------------------------------------------------
CREATE TABLE order_item (
    order_number    varchar(30)  NOT NULL REFERENCES sales_order (order_number),
    product_article integer      NOT NULL REFERENCES product (article),
    quantity        integer      NOT NULL DEFAULT 1 CHECK (quantity > 0),
    unit_price      numeric(9,2) NOT NULL,
    product_name    varchar(200) NOT NULL CHECK (btrim(product_name) <> ''),

    PRIMARY KEY (order_number, product_article),

    CHECK (unit_price > 0 AND unit_price <> 'NaN'::numeric)
);

-- =======================================================
-- Комментарии таблиц
-- =======================================================

COMMENT ON TABLE brand IS 'Бренды товаров';
COMMENT ON TABLE category IS 'Категории товаров';
COMMENT ON TABLE specification IS 'Технические характеристики';
COMMENT ON TABLE category_specification IS 'Характеристики категорий';
COMMENT ON TABLE product IS 'Товары каталога';
COMMENT ON TABLE product_specification IS 'Значения характеристик товаров';
COMMENT ON TABLE rear_camera IS 'Группы одинаковых модулей задней камеры';
COMMENT ON TABLE app_user IS 'Пользователи магазина';
COMMENT ON TABLE sales_order IS 'Заказы покупателей';
COMMENT ON TABLE order_item IS 'Позиции заказов';

-- =======================================================
-- Комментарии столбцов
-- =======================================================

COMMENT ON COLUMN brand.name IS 'Название бренда';

COMMENT ON COLUMN category.code IS 'Код категории';
COMMENT ON COLUMN category.name IS 'Название категории';

COMMENT ON COLUMN specification.name IS 'Название характеристики';
COMMENT ON COLUMN specification.value_type IS 'Тип значения';
COMMENT ON COLUMN specification.unit IS 'Единица измерения';

COMMENT ON COLUMN category_specification.category_code IS 'Код категории';
COMMENT ON COLUMN category_specification.specification_name IS 'Характеристика';

COMMENT ON COLUMN product.article IS 'Артикул';
COMMENT ON COLUMN product.name IS 'Название модели';
COMMENT ON COLUMN product.price IS 'Текущая цена в рублях';
COMMENT ON COLUMN product.available IS 'Доступность заказа';
COMMENT ON COLUMN product.brand_name IS 'Бренд';
COMMENT ON COLUMN product.category_code IS 'Категория';

COMMENT ON COLUMN product_specification.product_article IS 'Товар';
COMMENT ON COLUMN product_specification.specification_name IS 'Характеристика';
COMMENT ON COLUMN product_specification.value IS 'Одно значение без единицы измерения';

COMMENT ON COLUMN rear_camera.product_article IS 'Товар';
COMMENT ON COLUMN rear_camera.camera_type IS 'Тип камеры';
COMMENT ON COLUMN rear_camera.resolution_mp IS 'Разрешение в Мп';
COMMENT ON COLUMN rear_camera.quantity IS 'Количество модулей в группе';

COMMENT ON COLUMN app_user.email IS 'Email в нижнем регистре';
COMMENT ON COLUMN app_user.name IS 'Имя пользователя';
COMMENT ON COLUMN app_user.password_hash IS 'Хеш пароля';
COMMENT ON COLUMN app_user.role IS 'Роль ADMIN, MANAGER или CUSTOMER';

COMMENT ON COLUMN sales_order.order_number IS 'Номер заказа';
COMMENT ON COLUMN sales_order.user_email IS 'Покупатель';
COMMENT ON COLUMN sales_order.created_at IS 'Время оформления';
COMMENT ON COLUMN sales_order.status IS 'Статус';
COMMENT ON COLUMN sales_order.recipient_name IS 'Имя получателя';
COMMENT ON COLUMN sales_order.recipient_phone IS 'Телефон получателя в формате + и 1-15 цифр';
COMMENT ON COLUMN sales_order.completed_at IS 'Время завершения';

COMMENT ON COLUMN order_item.order_number IS 'Заказ';
COMMENT ON COLUMN order_item.product_article IS 'Товар';
COMMENT ON COLUMN order_item.quantity IS 'Количество';
COMMENT ON COLUMN order_item.unit_price IS 'Цена единицы при оформлении в рублях';
COMMENT ON COLUMN order_item.product_name IS 'Название товара при оформлении';

COMMIT;
