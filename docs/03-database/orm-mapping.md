# Описание стратегии ORM

В проекте используется двухуровневая стратегия маппинга объектов, так как приложение является клиент-серверным (Траектория В).

### Entity: UserProfile.java
```java
@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String bio;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhotoLike> likes = new ArrayList<>();
}
```
### Entity: Photo.java
```java
@Entity
@Table(name = "photos", indexes = {
    @Index(name = "idx_photos_author", columnList = "author_id"),
    @Index(name = "idx_photos_created", columnList = "created_at")
})
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_moderated", nullable = false)
    private Boolean isModerated = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private UserProfile author;

    @OneToMany(mappedBy = "photo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhotoLike> likes = new ArrayList<>();
}
```
### Entity: PhotoLike.java
```java
@Entity
@Table(name = "photo_likes", 
    uniqueConstraints = @UniqueConstraint(
        name = "unique_user_photo_like",
        columnNames = {"photo_id", "user_id"}
    ),
    indexes = {
        @Index(name = "idx_likes_photo_user", columnList = "photo_id, user_id")
    }
)
public class PhotoLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "photo_id", nullable = false)
    private Long photoId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id", insertable = false, updatable = false)
    private Photo photo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserProfile user;
}
```

## Android Room ORM (Локальная БД)

В мобильном приложении YPhoto используется **Room Database** для локального кэширования данных. Room entity'ы **упрощены** по сравнению с backend моделью, так как хранят только необходимые для отображения UI данные.

### Room Entity: PhotoEntity.kt
```kotlin
@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val likes: Int,              // счетчик лайков (не связь)
    val isLiked: Boolean,        // флаг - понравилось ли текущему пользователю
    val authorEmail: String,     // денормализованный e-mail автора
    val authorFullName: String,  // денормализованное полное имя
    val authorAvatarUrl: String?
)
```

###  Отличия Room schema от Backend:
| Поле | Backend | Room | Примечание |
|------|---------|------|-----------|
| id | Long (PK) | Int | Упрощено |
| authorId | Long (FK) | - | Заменено на denormalized authorEmail |
| title | String | String |  Совпадает |
| description | String | String |  Совпадает |
| imageUrl | String | String |  Совпадает |
| isModerated | Boolean | - |  Не синхронизируется |
| createdAt | LocalDateTime | - |  Не синхронизируется |
| updatedAt | LocalDateTime | - |  Не синхронизируется |
| likes | OneToMany<PhotoLike> | Int (count) |  Упрощено, не full relationship |
| isLiked | - | Boolean |  Специфично для клиента |

###  Отсутствующие Room Entity'ы (планировалось, не реализовано):
- `UserProfileEntity` — для кэширования профилей пользователей
- `PendingUploadsEntity` — для очереди ожидания фотографий при оффлайн-режиме

### DAO: PhotoDao.kt
```kotlin
@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos ORDER BY id DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE id = :photoId")
    fun getPhotoById(photoId: Int): Flow<PhotoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Update
    suspend fun updatePhoto(photo: PhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
}
```

### Стратегия синхронизации:
- **OnConflictStrategy.REPLACE**: При получении обновленных данных с сервера локальные записи перезаписываются полностью.
- **Single Source of Truth**: UI всегда слушает Room через Flow, Repository обновляет Room из API в фоновом режиме.
- **Отсутствие миграций**:  Во время разработки миграции не управлялись (версия БД остается 1).

