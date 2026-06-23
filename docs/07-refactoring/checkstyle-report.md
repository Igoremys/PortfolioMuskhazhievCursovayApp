# Checkstyle Configuration and Report

Для backend-модуля проекта выполнена подготовка конфигурации Checkstyle.

Конфигурационный файл: `backend/config/checkstyle/checkstyle.xml`.

Запуск генерации отчёта выполняется в каталоге `backend`:

```bash
./gradlew checkstyleMain checkstyleTest
```

Ожидаемые результаты:

- `backend/build/reports/checkstyle/main.html`
- `backend/build/reports/checkstyle/test.html`

Если необходимо, можно положить готовый HTML-отчёт в `docs/07-refactoring/` как дополнение к документации. Конфигурация уже включена, отчёт генерируется без изменения проектной структуры.
