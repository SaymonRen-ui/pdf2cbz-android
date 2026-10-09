# PDF2CBZ Android — передача дел

## Что это и где
Конвертер PDF → CBZ для читалок манги. Пакет `com.saymon.pdf2cbz`,
versionCode 1 / versionName 1.0.0. Статус: **версия 1.0.0 отправлена
на модерацию RuStore, ждём**.

- Проект: `PDF2CBZAndroid/` (рядом лежит `PDFcon-app/` — первое приложение, там те же схемы)
- Репозиторий: https://github.com/SaymonRen-ui/pdf2cbz-android (ветка `main`)
- Git: `user.name=SaymonRen`, `user.email=app.project@bk.ru`
- Внутренний контекст прошлого чата: `PROJECT.md` (устаревает — обнови статусы)

## Стек
Kotlin 2.2.10, AGP 8.13.0, Gradle 9.3.0, compileSdk/targetSdk 36, minSdk 26,
Compose Material3 (BOM 2025.10.00), Yandex Mobile Ads 7.18.7.
Рендер — штатный `PdfRenderer`, зависимостей ноль.

## Ключевые файлы
- `app/.../core/convert/Converter.kt` — рендер + ZIP (Store) + ETA
- `app/.../ui/ConvertViewModel.kt` — очередь, запуск
- `app/.../ui/MainScreen.kt` — главный экран, кнопка «Конвертировать»
- `app/.../ui/SettingsScreen.kt` — настройки + карточка «О приложении»
- `app/.../ConvertService.kt` — фоновый `dataSync` + уведомление
- `app/.../core/ads/AdManager.kt` — реклама, `AD_UNIT_ID = "R-M-20206448-1"`
- `app/.../Pdf2CbzApp.kt` — init MobileAds + preload
- `store/` — `icon_512.png`, скрины `01-03` (1080×1920, без шторки),
  `listing.txt` (тексты стора), `test-manga*.pdf` (свои тестовые PDF)

## Аккаунты и ссылки (несекретное)
- Почта поддержки: app.project@bk.ru
- Политика: https://github.com/SaymonRen-ui/pdf2cbz-android/blob/main/PRIVACY.md
- РСЯ: приложение PDF2CBZ + блок interstitial `R-M-20206448-1`
  (тестовые заглушки до модерации/владения в РСЯ — это нормально)
- RuStore: аккаунт физлица → только бесплатные приложения

## Ключи (секретное — только локально!)
- `pdf2cbz-release.jks` (JKS, alias `pdf2cbz`) + пароли в `local.properties`
- В Git НЕ уходят (`.gitignore`: `*.jks`, `local.properties`, `pepk*`, `upload_cert.pem`)
- Бэкап .jks + паролей обязателен (флешка/менеджер паролей). Потеря = смерть обновлений
- Подпись для RuStore: `pepk.jar` (в корне) + команда из консоли стора
  (encryptionkey у каждого приложения свой — брать из своей консоли!),
  пароли вводятся вручную во всплывающем окне, дальше `upload_cert.pem`

## Грабли (проверено болью)
1. Сервер РСЯ НЕ отдаёт рекламу дебажным сборкам (ошибка code=3).
   Проверять показ только на релизе!
2. `keytool` по умолчанию делает PKCS12 — он игнорирует отдельный пароль ключа.
   Для схемы «пароль хранилища + пароль ключа» генерировать с `-storetype JKS`.
3. Новым зависимостям нужен онлайн (`assembleDebug` без `--offline`) хотя бы раз.
4. Статус-бар на скринах: резать по факту (тут 100px), автоопределение по чёрному
   не работает при прозрачном топбаре. Финал: 1080×1920, JPG.
5. Иконка 512 — строго без прозрачности, иначе чёрные углы в сторе.
6. Android 15 + targetSdk 35+: `enableEdgeToEdge()` + `safeDrawing` + цвет иконок
   панелей под тему, иначе отказ модерации (кнопки под системными).
7. Этот тестовый телефон режет adb-тапы — проверки руками.
8. Демо-ID `demo-interstitial-yandex` на нашем WiFi не грузится (режется демо-хост),
   живой блок работает. Не пугаться.
9. `INTERNET` + `ACCESS_NETWORK_STATE` в манифесте обязательны для SDK рекламы.

## Частые команды (Windows PowerShell, из корня проекта)
```
.\gradlew.bat :app:assembleDebug --offline
.\gradlew.bat :app:bundleRelease --offline    # AAB: app\build\outputs\bundle\release\app-release.aab
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb install -r app\build\outputs\apk\release\Pdf2Cbz.apk
& $adb shell "monkey -p com.saymon.pdf2cbz -c android.intent.category.LAUNCHER 1"
& $adb shell screencap -p /sdcard/x.png
```

## Что осталось
1. Дождаться модерации RuStore (ориентир ~3 рабочих дня) — выйдет само (автопубликация)
2. Взять ссылку на приложение в сторе → вставить в РСЯ (модерация + владение)
3. Проверить живую рекламу из стора-версии
4. Дальше — развитие по фидбэку

## Окружение этой машины (на другой — адаптировать пути!)
- Windows, JDK 17 (Temurin), Android SDK: `C:\Users\Saymon\AppData\Local\Android\Sdk`
- Путь проекта на этой машине: `C:\Users\Saymon\Desktop\Project\PDFcon\PDF2CBZAndroid`
