# DDL-скрипты (PostgreSQL)

Ниже приведены SQL-скрипты для создания таблиц на сервере (Spring Boot + PostgreSQL). Скрипты включают ограничения целостности и индексы для оптимизации запросов.

```sql
-- 1. Таблица профилей пользователей
CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    bio TEXT,
    avatar_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индекс для быстрого поиска по email (аутентификация)
CREATE INDEX idx_user_profiles_email ON user_profiles(email);

-- 2. Таблица фотографий
CREATE TABLE photos (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_moderated BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_photos_author FOREIGN KEY (author_id) 
        REFERENCES user_profiles(id) ON DELETE CASCADE
);

-- Индексы для оптимизации запросов
CREATE INDEX idx_photos_author ON photos(author_id);
CREATE INDEX idx_photos_created ON photos(created_at DESC);
CREATE INDEX idx_photos_moderated ON photos(is_moderated);

-- 3. Таблица лайков (связь многие-ко-многим)
CREATE TABLE photo_likes (
    id BIGSERIAL PRIMARY KEY,
    photo_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_likes_photo FOREIGN KEY (photo_id) 
        REFERENCES photos(id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id) 
        REFERENCES user_profiles(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_photo_like UNIQUE (photo_id, user_id)
);

-- Составной индекс для быстрого подсчета лайков
CREATE INDEX idx_likes_photo_user ON photo_likes(photo_id, user_id);
CREATE INDEX idx_likes_user ON photo_likes(user_id);
```
 Описание ограничений целостности:

1. ON DELETE CASCADE: 
   - При удалении пользователя удаляются все его фото и лайки
   - При удалении фото удаляются все связанные лайки

2. UNIQUE (photo_id, user_id):
   - Гарантирует, что один пользователь может поставить только один лайк одной фотографии

3. NOT NULL на всех обязательных полях:
   - email, password_hash, full_name для пользователя
   - author_id, title, image_url для фото
   - photo_id, user_id для лайка
   - 
4. FOREIGN KEY:
   - author_id → user_profiles.id
   - photo_id → photos.id
   - user_id → user_profiles.id
