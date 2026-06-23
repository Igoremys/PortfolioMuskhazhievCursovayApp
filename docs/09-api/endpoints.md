# REST API: Эндпоинты

## Auth
- `POST /api/auth/register` — регистрация нового пользователя.
- `POST /api/auth/login` — вход пользователя и получение JWT.

## Users
- `GET /api/users/me` — получение данных текущего пользователя.
- `PUT /api/users/profile` — обновление профиля пользователя.
- `GET /api/users` — поиск пользователей.
- `GET /api/users/{id}` — получение публичного профиля.

## Photos
- `GET /api/photos` — получение списка фотографий.
- `GET /api/photos/{id}` — получение информации о фото.
- `POST /api/photos` — публикация новой фотографии.
- `POST /api/photos/upload` — загрузка файла изображения (Multipart).
- `PUT /api/photos/{id}` — обновление метаданных фотографии.
- `DELETE /api/photos/{id}` — удаление фотографии.
- `POST /api/photos/{id}/like` — постановка/снятие лайка.

## Search
- `GET /api/photos/search?query={text}` — поиск фото по заголовку и описанию.
- `GET /api/users/search?query={text}` — поиск пользователей.

## Примечания
- Все защищенные запросы требуют заголовок `Authorization: Bearer {token}`.
- В ответах чаще всего возвращается JSON с объектами `PhotoDto`, `UserDto`, `AuthResponse`.
