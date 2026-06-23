# Спецификация интерфейсов между слоями

Слои взаимодействуют друг с другом через четко определенные контракты (интерфейсы и сигнатуры функций). Это позволяет тестировать слои изолированно и заменять реализации (например, подменять Retrofit на FakeApi для тестов).

## 1. Интерфейс Control → Mediator (ViewModel → Repository)

Слой `Control` (ViewModel) не знает, откуда берутся данные. Он только запрашивает их и получает реактивный поток (`Flow`).

```kotlin
// Контракт Mediator (PortfolioRepository)
// Реально находится в: foundation/repository/PortfolioRepository.kt
class PortfolioRepository {
    // Возвращает Flow, на который подписывается ViewModel
    val photos: Flow<List<Photo>> 
    
    // Операции обновления данных (suspend функции для Coroutines)
    suspend fun refreshPhotos()
    suspend fun createPhotoFromUrl(request: PhotoCreateRequest, authorEmail: String): Photo?
    suspend fun toggleLike(photoId: Int)
    suspend fun deletePhoto(photoId: Int)
}
```

## 2. Интерфейс Mediator → Foundation (Repository → Data Sources)

Слой `Mediator` использует абстракции слоя `Foundation` для работы с сетью и БД.

```kotlin
// Контракт Foundation (Retrofit API)
interface PhotoApi {
    @GET("/api/photos")
    suspend fun getAllPhotos(): Response<List<PhotoDto>>

    @POST("/api/photos")
    suspend fun createPhoto(@Body request: PhotoCreateRequest): Response<PhotoDto>

    @POST("/api/photos/{id}/like")
    suspend fun toggleLike(@Path("id") id: Long): Response<PhotoDto>
}

// Контракт Foundation (Room DAO)
@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos ORDER BY id DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)
    
    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deletePhotoById(id: Int)
}
```

## 3. Интерфейс Foundation → Entity (Data Mapper)

Преобразование между сетевыми/БД моделями и доменными сущностями осуществляется через функции-расширения (Data Mapper pattern).

```kotlin
// Маппинг DTO (Сеть) -> Entity (БД)
fun PhotoDto.toEntity(): PhotoEntity

// Маппинг Entity (БД) -> Domain (Чистая модель)
fun PhotoEntity.toDomain(): Photo

// Маппинг Domain -> Entity (для локального сохранения)
fun Photo.toEntity(): PhotoEntity
```

## 4. Правило зависимостей

Зависимости направлены **строго сверху вниз**:
```
Presentation (UI)
      ↓
Control (ViewModel)
      ↓
Mediator (Repository)
      ↓
Entity (Domain Models) ← Foundation (Retrofit, Room)
```

**Запрещено:**
-  Presentation → Foundation (напрямую)
-  Foundation → Control (обратная зависимость)
-  Entity → Foundation (сущности не знают о БД)
