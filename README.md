# ⚡ TajSend — High-Speed File Transfer by Kolid Company

> **TajSend** — сверхбыстрое, безопасное приложение для беспроводной передачи файлов по QR-коду между Android устройствами без интернета и без рекламы (современная альтернатива Shareit).

![TajSend](app/src/main/res/drawable/tajsend_hero.jpg)

---

## 🚀 Возможности TajSend

- ⚡ **Высокая скорость передачи**: до 45+ МБ/с через прямой локальный Wi-Fi / Hotspot канал (чанковая буферизация).
- 📷 **Мгновенный QR-сканер**: интеграция CameraX с прицельной сеткой и лазерной анимацией, а также возможность ввода 6-значного PIN или IP вручную.
- 📂 **Категории файлов**: Фото, Видео, Музыка, Документы, Приложения (APK) и встроенный системный выбор файлов.
- 🔒 **100% Приватность**: файлы передаются напрямую между устройствами без сохранения в облаках и без сбора данных.
- 🚫 **0% Рекламы**: чистый, футуристический дизайн в стиле Cyber Emerald & Cyan Neon от **Kolid Company**.

---

## 🛠 Автоматическая сборка APK через GitHub Actions

В проекте уже настроен автоматический CI/CD рабочий процесс в файле `.github/workflows/build-apk.yml`.

### Как получить APK через ваш репозиторий `makon5757-cyber/dowload-`:

1. **Загрузите (Push) код в репозиторий**:
   ```bash
   git remote add origin https://github.com/makon5757-cyber/dowload-.git
   git branch -M main
   git push -u origin main
   ```

2. **GitHub Actions автоматически запустит сборку**:
   - Откройте вкладку **Actions** в репозитории `makon5757-cyber/dowload-`.
   - Дождитесь зеленой галочки выполнения сборки (около 2 минут).

3. **Скачайте готовый APK**:
   - В разделе **Artifacts** появится архив **`TajSend-APK`** с файлом `TajSend-v1.0.apk`.
   - В разделе **Releases** (Релизы) появится готовый для загрузки установочный файл **`TajSend-v1.0.apk`**!

---

## 📱 Локальная сборка на компьютере

```bash
# Для Linux / macOS:
chmod +x gradlew
./gradlew assembleDebug

# Для Windows:
gradlew.bat assembleDebug
```
Собранный APK появится в папке:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🏢 Разработчик
**Kolid Company** © 2026. Все права защищены.
