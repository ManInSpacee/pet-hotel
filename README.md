# Pet Hotel

## Обзор

Pet Hotel - учебное Java-приложение для работы с базой данных гостиницы для животных. Проект содержит JDBC-репозитории, схему PostgreSQL, тестовые данные и утилиту для выгрузки базы в Excel.

Технологический стек: Java 23+, Maven, PostgreSQL 18, Docker Compose, JDBC, Apache POI.

## Возможности

На данный момент проект содержит:

```text
[owners] Владельцы животных
[pets] Питомцы с привязкой к владельцам
[enclosures] Вольеры разных размеров
[bookings] Бронирования с датами и статусами
[excel] Снимок таблиц базы в формате XLSX, по одному листу на таблицу
```

## Требования

- Windows 10 или новее
- JDK 23 или новее
- Apache Maven 3.9 или новее
- Docker Desktop с Docker Compose
- Доступ к интернету при первой сборке, чтобы Maven загрузил зависимости

Файл `run.bat` проверяет наличие JDK, Maven и Docker Desktop. Если чего-то не хватает, BAT-файл пытается установить соответствующий пакет через `winget`. После установки закройте окно и запустите `run.bat` повторно. Docker Desktop может потребовать перезагрузку или повторный вход в Windows.

## Запуск через BAT-файл

Запустите `run.bat` из корневой папки репозитория. Скрипт соберёт Java-проект, запустит PostgreSQL через Docker Compose и дождётся готовности базы. Затем выберите действие:

```text
1. Java demo application
2. Create an Excel database snapshot
```

Первый вариант запускает демонстрационный класс `Main`. Второй создаёт Excel-снимок базы в `pet_hotel/db_snapshots`.

При первом запуске PostgreSQL создаёт таблицы и загружает данные из `db/01_schema.sql` и `db/02_seed.sql`. Docker выполняет эти скрипты только при создании нового хранилища базы.

## Запуск вручную

Откройте PowerShell в корневой папке репозитория. Проверьте установку необходимых инструментов:

```powershell
java -version
javac -version
mvn -version
docker compose version
```

При необходимости установите инструменты через `winget`:

```powershell
winget install --exact --id EclipseAdoptium.Temurin.25.JDK
winget install --exact --id Apache.Maven
winget install --exact --id Docker.DockerDesktop
```

Запустите базу данных из корневой папки проекта, где расположен `docker-compose.yaml`:

```powershell
docker compose up -d
docker compose ps
```

Соберите Java-код и получите classpath зависимостей:

```powershell
$classpathFile = Join-Path (Resolve-Path .\pet_hotel).Path 'target\runtime-classpath.txt'
mvn -f .\pet_hotel\pom.xml -DskipTests compile dependency:build-classpath "-Dmdep.outputFile=$classpathFile"
$runtimeClasspath = (Get-Content $classpathFile -Raw).Trim()
```

Создайте Excel-снимок:

```powershell
Push-Location .\pet_hotel
java -cp "target\classes;$runtimeClasspath" ru.mirea.project.util.DatabaseExcelDump
Pop-Location
```

Чтобы вместо снимка запустить демонстрационный класс приложения, используйте его имя:

```powershell
Push-Location .\pet_hotel
java -cp "target\classes;$runtimeClasspath" ru.mirea.project.Main
Pop-Location
```

Класс `Main` демонстрирует операции с вольерами и может изменять записи в таблице `enclosures`.

## Настройки базы данных

По умолчанию приложение подключается к PostgreSQL со следующими параметрами:

```text
URL:      jdbc:postgresql://localhost:5437/pet_hotel
Пользователь: postgres
Пароль:   postgres
```

Для `DatabaseExcelDump` параметры подключения можно переопределить переменными среды `DB_URL`, `DB_USER`, `DB_PASSWORD` или системным аргументом приложения. Папка снимков по умолчанию называется `db_snapshots` относительно рабочей папки `pet_hotel`. Её можно переопределить свойством `db.snapshot.directory`.

Если база уже создана, но тестовые данные отсутствуют, примените сиды вручную. Команда обновляет демонстрационные строки с указанными в скрипте ID:

```powershell
docker compose exec -T postgres psql -v ON_ERROR_STOP=1 -U postgres -d pet_hotel -f /docker-entrypoint-initdb.d/02_seed.sql
```

## Excel-снимки

Каждый запуск `DatabaseExcelDump` создаёт новый файл с датой и временем в имени:

```text
pet_hotel/db_snapshots/pet_hotel_snapshot_YYYYMMDD_HHMMSS.xlsx
```

Листы Excel: `owners`, `pets`, `enclosures`, `bookings`. Снимок формируется в рамках одной транзакции чтения.

## Управление базой

Просмотр логов PostgreSQL:

```powershell
docker compose logs postgres
```

Остановка контейнера с сохранением данных:

```powershell
docker compose down
```

## Структура проекта

```text
.
├── db/
│   ├── 01_schema.sql
│   └── 02_seed.sql
├── pet_hotel/
│   ├── db_snapshots/
│   ├── src/main/java/ru/mirea/project/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── service/
│   │   └── util/DatabaseExcelDump.java
│   └── pom.xml
├── docker-compose.yaml
├── README.md
└── run.bat
```
