# TechStore

Магазин техники с REST API на Java 17, Spring Boot и Spring Data JPA.
Можно добавлять и редактировать товары, искать их по фильтрам, работать
с пользователями, справочниками и скидками. Данные хранятся в PostgreSQL.

## Запуск

Нужны JDK 17 или новее и запущенный Docker Desktop.
Из папки проекта:

```sh
make run
```

PostgreSQL запускается в Docker, приложение — через Maven на компьютере.
После запуска API доступно в [Swagger](http://localhost:8081/swagger-ui.html).
Для остановки приложения — Ctrl+C, затем `make down` для остановки базы.
Данные при этом сохраняются.

Без Make:

```sh
docker compose up -d --wait
./mvnw spring-boot:run
```

В Windows вместо `./mvnw` используется `mvnw.cmd`.
База останавливается командой `docker compose stop`.

Основные команды:

- `make build` — сборка и тесты (`./mvnw clean verify`);
- `make test` — только тесты;
- `make up` / `make down` — запуск и остановка базы;
- `make logs` — журнал базы.

## Настройки базы

По умолчанию: адрес `localhost:5434`, база и пользователь `techstore`,
пароль локальной базы `techstore_local`. Приложение использует порт `8081`.
Для запуска с настройками по умолчанию `.env` не нужен.
Docker Compose читает из `.env` параметры `DB_PORT` и `DB_PASSWORD`
(пример — `.env.example`). Maven этот файл не читает: изменённые параметры
нужно также передать приложению через переменные окружения `DB_URL`
и `DB_PASSWORD`. Для приложения доступны `DB_USER` и `PORT`.
Например, при `DB_PORT=5435` адрес приложения к базе:
`DB_URL=jdbc:postgresql://localhost:5435/techstore`.
Изменение пароля в `.env` не меняет пароль уже созданной базы.

SQL из `db/` создаёт таблицы и справочники при первом запуске с пустым volume.
Hibernate проверяет схему через `ddl-auto=validate`.

## База данных

Таблицы:

- `brand`, `category` — бренды и категории товаров;
- `specification`, `category_specification` — характеристики и их связь с категориями;
- `product`, `product_specification`, `rear_camera` — товары, значения характеристик и камеры;
- `app_user` — пользователи;
- `discount_state`, `discount_backup` — состояние скидки и исходные цены;
- `sales_order`, `order_item` — заказы и их позиции.

Таблицы заказов есть в схеме, но API их не обслуживает.

ER-диаграмма из pgAdmin:

![Схема базы данных в pgAdmin](images/database-schema.png)

## API

В Swagger есть формы для добавления смартфона, планшета, ноутбука и наушников.
Достаточно заполнить название и цену, остальные поля можно изменить перед
отправкой. Новый бренд из формы создаётся автоматически. Артикул возвращается
в ответе и используется в запросах изменения и удаления.

Основные маршруты:

- `/api/products` — GET: список, POST: создание через JSON;
- `/api/products/{article}` — GET, PUT, DELETE;
- `/api/products/{article}/details` — PATCH основных полей через форму;
- `/api/products/smartphones`, `/tablets`, `/laptops`, `/headphones` — POST форм товаров;
- `/api/users` и `/api/users/{email}` — CRUD пользователей;
- `/api/users/form` — POST формы, `/api/users/{email}/form` — PUT формы;
- `/api/brands` и `/api/brands/{name}` — CRUD брендов;
- `/api/categories` — GET категорий;
- `/api/specifications` и `/api/specifications/{name}` — CRUD характеристик;
- `/api/categories/{code}/specifications` — GET характеристик категории;
- `/api/categories/{code}/specifications/{name}` — PUT или DELETE связи;
- `/api/discount` — GET состояния, POST применения, DELETE отмены скидки.

Фильтры товаров: `name`, `brand`, `category`, `minPrice`, `maxPrice`, `available`.
Параметры `page` и `size` задают страницу и её размер.
Примеры запросов находятся в `examples/requests.http`.

PUT заменяет товар целиком. PATCH меняет только заполненные поля формы.
Пароли хешируются BCrypt и не возвращаются в ответах. Проверки прав
HTTP-клиентов и входа в систему нет.

## Структура проекта

Пакеты находятся в `com.example.techstore`:

- `model` — JPA-сущности товаров, справочников, пользователей и скидок;
- `repository` — Spring Data JPA и фильтры;
- `service` — интерфейсы сервисов, проверки характеристик и расчёт цен;
- `service.impl` — бизнес-логика и транзакции;
- `controller` — REST-контроллеры;
- `dto` — данные запросов, форм и ответов;
- `exception` — ошибки API;
- `config` — настройки BCrypt и OpenAPI.

Характеристики товара хранятся отдельными записями. Проверки обязательных
полей зависят от категории. Пользователи наследуются от абстрактного `User`.
Скидка рассчитывается в потоках `price-worker-N`; исходные цены сохраняются
в базе для отмены. Пока скидка действует, изменение каталога заблокировано.

## Тесты

`make test` запускает тесты JUnit, Mockito и MockMvc с `@SpringBootTest` без подключения к БД.
GitHub Actions выполняет Maven verify на Java 17.
