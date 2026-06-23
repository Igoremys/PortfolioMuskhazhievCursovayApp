# Техническая спецификация

## Общая архитектура
Проект разделен на клиентскую и серверную части. Клиент реализован по паттерну MVVM и PCMEF:
- Presentation: Jetpack Compose UI;
- Control: ViewModel + StateFlow;
- Mediator: Repository;
- Entity: доменные модели;
- Foundation: Room и Retrofit.

## Сервер
- Spring Boot 3;
- Spring Data JPA;
- PostgreSQL;
- JWT-аутентификация;
- REST API для управления фото и пользователями.

## Клиент
- Kotlin + Jetpack Compose;
- Retrofit для HTTP-запросов;
- Room Database для локального кэша;
- Coroutines и Flow для реактивной обработки данных.

## Безопасность
- JWT для защиты API;
- BCrypt для хеширования паролей;
- Проверка прав доступа для операций удаления и редактирования.

## Оффлайн-режим
- Кэширование данных в Room;
- Отображение последнего состояния при отсутствии сети;
- Фоновая синхронизация при восстановлении соединения.
