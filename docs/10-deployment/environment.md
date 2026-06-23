# Окружение разработки и запуска

## Серверная часть
- Java 17
- Spring Boot 3
- PostgreSQL 15+
- Maven/Gradle для сборки
- OpenAPI/Swagger для документации API

## Клиентская часть
- Kotlin
- Android SDK 33+
- Jetpack Compose
- Room
- Retrofit
- Coroutines

## Локальное развертывание
1. Настройте PostgreSQL и создайте базу данных `yphoto`.
2. Укажите параметры в `server/src/main/resources/application.properties` или `application.yml`.
3. Запустите сервер:
   ```bash
   ./gradlew bootRun
   ```
4. Запустите Android-приложение из Android Studio.

## Переменные окружения
- `SPRING_DATASOURCE_URL` — JDBC URL базы данных.
- `SPRING_DATASOURCE_USERNAME` — имя пользователя базы данных.
- `SPRING_DATASOURCE_PASSWORD` — пароль.
- `JWT_SECRET` — секретный ключ для подписи JWT.
