# Portfolio Backend

## О проекте

`backend` — серверная часть курсового проекта YPhoto Portfolio. Это Spring Boot приложение, предоставляющее REST API для работы с пользователями, фотографиями и загрузкой изображений.

## Стек технологий

- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA
- JWT Authentication
- PostgreSQL
- OpenAPI / Swagger
- JaCoCo
- Gradle

## Основные функции

- регистрация и вход по JWT
- управление профилем пользователя
- CRUD для фотографий
- загрузка изображений на сервер
- просмотр загруженных файлов
- поиск пользователей и фотографий
- защита эндпоинтов через Spring Security

## Структура

```text
portfolio-backend/
├── build.gradle
├── settings.gradle
├── src/
│   ├── main/
│   │   ├── java/com/portfolioapp/portfoliobackend/
│   │   │   ├── controller/
│   │   │   ├── config/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── PortfolioBackendApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── uploads/ (генерируется при загрузке фото)
```

## Конфигурация

В `src/main/resources/application.properties` задаются параметры сервера, базы данных, JWT и загрузки файлов.

Основные настройки:

- `server.port=8080`
- `spring.datasource.url=jdbc:postgresql://localhost:5432/portfolio_db`
- `spring.datasource.username=postgres`
- `spring.datasource.password=`
- `spring.jpa.hibernate.ddl-auto=update`
- `app.jwt.secret` и `app.jwt.expiration`
- `app.upload.path=${user.dir}/uploads`

> Важно: перед запуском проверьте доступность PostgreSQL и корректность параметров `spring.datasource.*`.

## Запуск

### Сборка и запуск

```bash
cd backend
./gradlew bootRun
```

На Windows:

```bash
gradlew.bat bootRun
```

### Тесты

```bash
./gradlew test
```

### Генерация отчёта JaCoCo

```bash
./gradlew jacocoTestReport
```

Отчет доступен в:

```text
backend/build/reports/jacocoHtml/index.html
```

## REST API

### Основные эндпоинты

#### Аутентификация
- `POST /api/auth/register` — регистрация нового пользователя
- `POST /api/auth/login` — вход и получение JWT

#### Пользователи
- `GET /api/users` — получить список пользователей
- `GET /api/users/me` — получить текущий профиль
- `PUT /api/users/profile` — обновить профиль
- `GET /api/users/search?keyword={keyword}` — поиск пользователей
- `GET /api/users/{id}` — получить публичный профиль пользователя

#### Фотографии
- `GET /api/photos` — получить все фотографии
- `GET /api/photos/{id}` — получить фото по ID
- `POST /api/photos` — создать запись о фото
- `PUT /api/photos/{id}` — обновить фото
- `DELETE /api/photos/{id}` — удалить фото
- `POST /api/photos/{id}/like` — поставить/убрать лайк
- `GET /api/photos/search?keyword={keyword}` — поиск фотографий
- `GET /api/photos/by-author?authorId={id}` — фотографии выбранного автора
- `POST /api/photos/upload` — загрузить изображение на сервер
- `GET /api/photos/files/{filename}` — получить загруженный файл

## Swagger / OpenAPI

Swagger UI доступен по адресу:

```text
http://localhost:8080/swagger-ui.html
```

API docs:

```text
http://localhost:8080/api-docs
```

## Полезные замечания

- Загруженные файлы хранятся в папке `uploads/` рядом с корнем `portfolio-backend`.
- Значение `app.jwt.secret` должно быть секретным в продакшн-окружении.
- Если у клиента используются сетевые адреса, убедитесь, что `BASE_URL` в Android-приложении совпадает с реальным адресом сервера.
