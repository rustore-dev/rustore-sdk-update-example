# Пример внедрения SDK обновлений RuStore 
## [Документация SDK обновления приложения](https://www.rustore.ru/help/sdk/updates)


### Оглавление
- [Условия работы SDK обновлений](#Условия-работы-SDK-обновлений)
- [Подготовка требуемых параметров](#Подготовка-требуемых-параметров)
- [Настройка примера приложения](#Настройка-примера-приложения)
- [Сценарий использования](#Сценарий-использования)
- [Условия распространения](#Условия-распространения)
- [Техническая поддержка](#Техническая-поддержка)

### Условия работы SDK обновлений

Для корректной работы SDK необходимо соблюдать следующие условия:

Требования для пользователя:

- ОС Android версии 7.0 или выше.

- На устройстве пользователя установлена актуальная версия RuStore.

- Пользователь авторизован в RuStore.

- Приложение должно быть опубликовано в RuStore.

- Приложению RuStore разрешена установка приложений

Требования для разработчика/приложения:

- ApplicationId, указанный в build.gradle, совпадает с applicationId apk-файла, который вы публиковали в консоль RuStore.

- Подпись keystore должна совпадать с подписью, которой было подписано приложение, опубликованное в консоль RuStore. Убедитесь, что используемый buildType (пр. debug) использует такую же подпись, что и опубликованное приложение (пр. release).


### Подготовка требуемых параметров

1. `applicationId` - из приложения, которое вы публиковали в консоль RuStore, находится в файле build.gradle вашего проекта
    ```
    android {
        defaultConfig {
            applicationId = "ru.rustore.sdk.appupdateexample"
        }
    }
    ```

2. `release.keystore` - подпись, которой было подписано приложение, опубликованное в консоль RuStore.

3. `release.properties` - в этом файле должны быть указаны параметры подписи, которой было подписано приложение, опубликованное в консоль RuStore. [Как работать с ключами подписи APK-файлов](https://www.rustore.ru/help/developers/publishing-and-verifying-apps/app-publication/apk-signature/)

### Настройка примера приложения

1. Замените `applicationId`, в файле build.gradle, на applicationId apk-файла, который вы публиковали в консоль RuStore:
   ```
   android {
       defaultConfig {
          applicationId = "ru.rustore.sdk.appupdateexample"
       }
   }
   ```

2. В директории `cert` замените сертификат `release.keystore` - сертификатом своего приложения, так же в `release.properties` выполните настройку параметров `key_alias`, `key_password`, `store_password`. Подпись `release.keystore` должна совпадать с подписью, которой было подписано приложение, опубликованное в консоль RuStore. Убедитесь, что используемый `buildType` (пр. debug) использует такую же подпись, что и опубликованное приложение (пр. release).

3. Запустите проект и проверьте работу приложения


### Сценарий использования

#### Создание менеджера обновлений
Перед вызовом методов библиотеки необходимо создать менеджер обновлений.

```
val ruStoreAppUpdateManager = RuStoreAppUpdateManagerFactory.create(context)
```

#### Проверка доступности обновления
Сначала нужно проверить доступность обновления для вашего приложения:

```
ruStoreAppUpdateManager
    .getAppUpdateInfo()
    .addOnSuccessListener { appUpdateInfo ->
        if (appUpdateInfo.updateAvailability == UpdateAvailability.UPDATE_AVAILABLE) {
            // Обновление доступно
        }
    }
    .addOnFailureListener { throwable ->
        Log.e(TAG, "getAppUpdateInfo error", throwable)
    }
```

#### Запуск скачивания обновления
Если обновление доступно, можно запускать метод `startUpdateFlow`:

```
ruStoreAppUpdateManager
    .startUpdateFlow(
        // Объект AppUpdateInfo, полученный в методе `getAppUpdateInfo()`
        appUpdateInfo, 
        // Параметры обновления (используем отложенное обновление)
        AppUpdateOptions.Builder().build()
    )
    .addOnSuccessListener { resultCode ->
        if (resultCode == Activity.RESULT_CANCELED) {
            // Пользователь отказался от скачивания
        }
    }
    .addOnFailureListener { throwable ->
        Log.e(TAG, "startUpdateFlow error", throwable)
    }
```

Данный метод отображает пользователю диалог с подтверждением скачивания. Если пользователь соглашается, то метод возвращает Activity.RESULT_OK, иначе Activity.RESULT_CANCELED.
Обратите внимание - каждый объект ``AppUpdateInfo`` может использовать только один раз. Для повторного вызова метода нужно запросить его снова.

В `startUpdateFlow` можно передать параметр обновления, на текущий момент их 3:
- AppUpdateOptions.Builder().build() - отложенное обновление(по умолчанию)
- AppUpdateOptions.Builder().appUpdateType(IMMEDIATE).build() - принудительное обновление
- AppUpdateOptions.Builder().appUpdateType(SILENT).build() - тихое обновление

#### Установка обновления
После того как обновление скачано, можно запускать установку.
Для запуска установки обновления вызовите метод completeUpdate(appUpdateOptions: AppUpdateOptions). В метод можно передавать только 2 типа завершения установки FLEXIBLE и SILENT, Отложенное и тихое обновление соответственно.

```
ruStoreAppUpdateManager.completeUpdate(AppUpdateType.FLEXIBLE)
     .addOnFailureListener { throwable ->
          Log.e(TAG, "completeUpdate error", throwable)
     }
```

- Тип обновлений FLEXIBLE - приложение перезапустится.
- Тип обновлений SILENT - приложение закроется без перезапуска.


#### Использование слушателя (listener)
Для отслеживания статуса скачивания обновления можно зарегистрировать слушатель:

```
val installStateUpdateListener = InstallStateUpdateListener { installState ->
    when (installState.installStatus) {
        InstallStatus.DOWNLOADED -> {
            // Скачивание завершено, можно запускать установку обновления
        }
        InstallStatus.DOWNLOADING -> {
            val totalBytes = installState.totalBytesToDownload
            val bytesDownloaded = installState.bytesDownloaded
            
            // Скачивание в процессе. Можно, например, отобразить ProgressBar
        }
        InstallStatus.FAILED -> {
            // В процессе скачивания возникла ошибка
        }
    }
}

// Перед тем как начинать скачивание обновления, добавьте слушатель
ruStoreAppUpdateManager.registerListener(installStateUpdateListener)

// Когда отслеживание статуса больше не нужно - удалите слушатель
ruStoreAppUpdateManager.unregisterListener(installStateUpdateListener)
```


### Условия распространения
Данное программное обеспечение, включая исходные коды, бинарные библиотеки и другие файлы распространяется под лицензией MIT. Информация о лицензировании доступна в документе `MIT-LICENSE.txt`


### Техническая поддержка
Если появились вопросы по интеграции SDK обновлений, обратитесь по [ссылке](https://www.rustore.ru/help/sdk/updates).
