# Пример внедрения SDK отзывов и оценок RuStore
## [Документация SDK для обновления приложения](https://help.rustore.ru/rustore/for_developers/developer-documentation/sdk_updates)

### Требуемые условия

Для корректной работы SDK необходимо соблюдать следующие условия:

Требования для пользователя:
- На устройстве пользователя установлено приложение RuStore.

- Версия RuStoreApp на устройстве пользователя актуальная.

- Пользователь  авторизован в приложении RuStore.
 
- Для приложения RuStore должна быть разрешена установка из неизвестных источников.

Требования для разработчика/приложения:

- Android 6 (SDK 23)

- ApplicationId, указанный в build.gradle, совпадает с applicationId apk-файла, который вы публиковали в консоль RuStore:

- Подпись keystore должна совпадать с подписью, которой было подписано приложение, опубликованное в консоль RuStore. Убедитесь, что используемый buildType (пр. debug) использует такую же подпись, что и опубликованное приложение (пр. release).

### Использование методов

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

Если обновление доступно, можно запускать следующий метод:

```
ruStoreAppUpdateManager
    .startUpdateFlow(
        // Объект AppUpdateInfo, полученный в методе `getAppUpdateInfo()`
        appUpdateInfo, 
        // Параметры обновления (в текущей версии SDK используются параметры по умолчанию)
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

После того как обновление скачано, можно запускать установку. После запуска установки приложение автоматически закроется.

```
ruStoreAppUpdateManager.completeUpdate()
```

### Есть вопросы
Если появились вопросы по интеграции SDK обновлений, обратитесь по этой ссылке:
https://help.rustore.ru/rustore/trouble/user/help_user_email
или напишите на почту support@rustore.ru.
