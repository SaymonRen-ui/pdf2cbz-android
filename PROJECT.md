# PDF2CBZ Android — контекст для другого чата

## Что это
Мобильный конвертер PDF → CBZ для читалок манги. Пара к десктопному
[PDF2CBZ (C# / WPF)](https://github.com/SaymonRen-ui/pdf2cbz), логика перенесена
оттуда: качество JPEG 1–100 (дефолт 90), DPI 100/150/200/300, имена
`001.jpg…240.jpg`, ZIP без сжатия (Store), запись через `.tmp`-подход
(на Android — поток сразу в выходной файл).

Цель: публикация в RuStore (бесплатное приложение, физлицо).

## Стек
- Kotlin 2.2.10, AGP 8.13.0, Gradle 9.3.0, compileSdk/targetSdk 36, minSdk 26
- Jetpack Compose + Material3, lifecycle-viewmodel, activity-compose,
  documentfile, material-icons-core; тесты JUnit4
- Рендер — штатный `android.graphics.pdf.PdfRenderer` (зависимостей ноль)
- Пакет `com.saymon.pdf2cbz`, versionCode 1 / versionName 1.0.0, APK `Pdf2Cbz.apk`

## Что сделано
- Очередь файлов (SAF `OpenMultipleDocuments`), дедуп, предзагрузка числа страниц
- Прогресс двухуровневый: общий по страницам + текущий файл; ETA как на ПК
- Отмена, ошибки одного файла не останавливают очередь, статусы ✓/✗
- Вывод: Загрузки через MediaStore **или** своя папка (SAF tree, запоминается)
- Темы: системная/светлая/тёмная (SharedPreferences), экран настроек
- «Поделиться → PDF2CBZ» из других приложений (SEND / SEND_MULTIPLE)
- Фоновый сервис `dataSync` + уведомление с прогрессом и отменой
- Альбом: две колонки (файлы | параметры), портрет — одна
- Edge-to-edge под Android 15: `enableEdgeToEdge()`, `safeDrawing`, иконки
  системных панелей под тему
- Иконка V7 (книга с закладкой): adaptive + legacy mipmap + `store/icon_512.png`
  без прозрачности (требование стора); исходник — PC `icon.ico`
- Не гасить экран во время конвертации; жест «назад» в настройках — на главный экран
- Юнит-тесты ядра (имена, ETA)

## Грабли (чтобы не наступать снова)
- MediaStore + MIME `application/zip` даёт `test.cbz.zip` — нужен
  `application/vnd.comicbook+zip`
- Демон требует тулчейн 25 (`gradle/gradle-daemon-jvm.properties`), путь к JBR —
  только в `%USERPROFILE%/.gradle/gradle.properties`, не в проекте
- Тестовый телефон режет adb-тапы (нужен тумблер «USB debugging security»),
  keyevent работают; проверки — руками или скринами
- Телефон висит на локскрине — `wm dismiss-keyguard` не всегда снимает

## Осталось (RuStore)
- [ ] Репозиторий `pdf2cbz-android` + CI (тесты + Debug APK в Releases)
- [ ] Новый keystore под этот пакет + пароли в `local.properties` (с бэкапом!)
- [ ] Тексты: `PRIVACY.md`, экран «О приложении», `store/listing.txt`
- [ ] Скрины 1080×1920 9:16 без статус-бара (устройство 1080×2340 — резать/жать)
- [ ] Решение по рекламе РСЯ (AdManager копипастом + ID блока) — да/нет
- [ ] Релизная сборка (AAB), подпись, карточка стора

## Структура
- `app/src/main/.../core/convert/Converter.kt` — рендер + ZIP + ETA
- `app/src/main/.../ui/ConvertViewModel.kt` — очередь, настройки запуска
- `app/src/main/.../ui/MainScreen.kt`, `SettingsScreen.kt`, `theme/`
- `app/src/main/.../data/SettingsStore.kt` — тема + папка вывода
- `app/src/main/.../ConvertService.kt`, `MainActivity.kt` (шаринг, edge-to-edge)
- `store/icon_512.png` — иконка для стора
