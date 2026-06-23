# Спецификация методов

## Серверная часть (Spring Boot):
- `AuthController.register()` — регистрация нового пользователя с хешированием пароля (BCrypt);
- `AuthController.login()` — аутентификация пользователя и выдача JWT-токена;
- `ProfileController.getCurrentUser()` — получение данных авторизованного пользователя;
- `ProfileController.updateProfile()` — обновление имени, описания и аватара профиля;
- `ProfileController.getAllUsers()` — получение списка всех пользователей для поиска;
- `PhotoController.getAllPhotos()` — получение ленты фотографий с пагинацией;
- `PhotoController.createPhoto()` — публикация новой фотографии;
- `PhotoController.uploadPhoto()` — загрузка файла изображения (Multipart);
- `PhotoController.deletePhoto()` — удаление фотографии по ID;
- `LikeController.toggleLike()` — постановка/снятие лайка.

## Клиентская часть (Android Native):
- `AuthViewModel.login()` — отправка учетных данных на сервер и сохранение JWT;
- `AuthViewModel.register()` — регистрация нового аккаунта;
- `AuthViewModel.loadCurrentUser()` — загрузка профиля после входа;
- `AuthViewModel.updateProfile()` — редактирование данных профиля;
- `AuthViewModel.logout()` — выход из аккаунта и очистка токена;
- `PhotoViewModel.loadPhotos()` — загрузка ленты фото с кэшированием в Room;
- `PhotoViewModel.createPhotoFromUrl()` — публикация фото по URL;
- `PhotoViewModel.uploadAndCreatePhoto()` — загрузка файла с устройства;
- `PhotoViewModel.toggleLike()` — оптимистичное обновление лайка;
- `PhotoViewModel.deletePhoto()` — удаление фото с синхронизацией;
- `PortfolioRepository.refreshPhotos()` — фоновая синхронизация данных (сеть ↔ кэш);
- `PortfolioRepository.toggleLike()` — бизнес-логика переключения лайка;
- `SearchViewModel.loadAllUsers()` — загрузка списка пользователей для поиска;
- `SearchViewModel.loadPublicProfile()` — загрузка публичного профиля;
- `TokenManager.saveToken()` — сохранение JWT в EncryptedSharedPreferences;
- `TokenManager.getToken()` — получение токена для авторизованных запросов;
- `PhotoDao.getAllPhotos()` — реактивный поток фото из локальной БД (Flow);
- `PhotoDao.insertPhotos()` — вставка/обновление кэша с OnConflictStrategy.REPLACE;
- `PhotoDao.getPhotoById()` — получение конкретного фото для оптимистичного обновления.

## Назначение
Документ помогает связать диаграммы последовательности  с конкретными методами контроллеров, сервисов, ViewModel и репозиториев. Обеспечивает трассируемость требований к реализации и упрощает навигацию по кодовой базе.
