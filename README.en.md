<!-- ── Language switch (EN active) ──────────────────────────────────── -->
<div align="left" style="margin:0 0 14px 0;">

  <span style="display:inline-block;
               padding:.28rem .6rem;
               border:1px solid rgba(0,0,0,.18);
               border-radius:10px 0 0 10px;
               font-weight:400;
               font-size:12px;
               letter-spacing:.06em;
               color:#111827;
               background:linear-gradient(180deg,#ffffff,#f3f4f6);
               box-shadow:0 1px 0 rgba(0,0,0,.06);">
    [RU][ru]
  </span><span style="display:inline-block;
               margin-left:-1px;
               padding:.28rem .6rem;
               border:1px solid rgba(0,0,0,.14);
               border-radius:0 10px 10px 0;
               font-weight:400;
               font-size:12px;
               letter-spacing:.06em;
               background:linear-gradient(180deg,#e9edf2,#ffffff);
               box-shadow:inset 0 2px 6px rgba(0,0,0,.10);">
    EN
  </span>

</div>
<!-- ────────────────────────────────────────────────────────────────── -->

# Example of integrating the RuStore Updates SDK
## [SDK Update Documentation](https://www.rustore.ru/help/sdk/updates)


### Table of Contents
- [Conditions for SDK Operation](#Conditions-for-SDK-Operation)
- [Preparing Required Parameters](#Preparing-Required-Parameters)
- [Setting Up the Example Application](#Setting-Up-the-Example-Application)
- [Usage Scenario](#Usage-Scenario)
- [Distribution Conditions](#Distribution-Conditions)
- [Technical Support](#Technical-Support)

### Conditions for SDK Operation

For the SDK to function correctly, the following conditions must be met:

User Requirements:

- Android OS version 7.0 or higher.

- The user's device has the latest version of RuStore installed.

- The user is logged into RuStore.

- The application must be published in RuStore.

- RuStore is allowed to install applications.

Developer/Application Requirements:

- The ApplicationId specified in build.gradle matches the applicationId of the apk file you published in the RuStore console.

- The keystore signature must match the signature used to sign the application published in the RuStore console. Ensure that the buildType being used (e.g., debug) uses the same signature as the published application (e.g., release).

### Preparing Required Parameters

1. `applicationId` - from the application you published in the RuStore console, is located in your project's build.gradle file
    ```
    android {
        defaultConfig {
            applicationId = "ru.rustore.sdk.appupdateexample"
        }
    }
    ```

2. `release.keystore` - the signature used to sign the application published in the RuStore console.

3. `release.properties` - this file should contain the signing parameters used to sign the application published in the RuStore console. [How to Work with APK Signature Keys](https://www.rustore.ru/help/developers/publishing-and-verifying-apps/app-publication/apk-signature/)

### Setting Up the Example Application

1. Replace the `applicationId` in the build.gradle file with the applicationId of the apk file you published in the RuStore console:
   ```
   android {
       defaultConfig {
          applicationId = "ru.rustore.sdk.appupdateexample"
       }
   }
   ```

2. In the `cert` directory, replace the `release.keystore` certificate with your own application's certificate, and also configure the parameters `key_alias`, `key_password`, `store_password` in `release.properties`. The `release.keystore` signature must match the signature used to sign the application published in the RuStore console. Make sure that the `buildType` being used (e.g., debug) uses the same signature as the published application (e.g., release).

3. Run the project and check the application functionality

### Usage Scenario

#### Creating an Update Manager
Before calling library methods, create an update manager.

```
val ruStoreAppUpdateManager = RuStoreAppUpdateManagerFactory.create(context)
```

#### Checking for Available Updates
First, check if an update is available for your application:

```
ruStoreAppUpdateManager
    .getAppUpdateInfo()
    .addOnSuccessListener { appUpdateInfo ->
        if (appUpdateInfo.updateAvailability == UpdateAvailability.UPDATE_AVAILABLE) {
            // Update is available
        }
    }
    .addOnFailureListener { throwable ->
        Log.e(TAG, "getAppUpdateInfo error", throwable)
    }
```

#### Starting Update Download
If an update is available, call the `startUpdateFlow` method:

```
ruStoreAppUpdateManager
    .startUpdateFlow(
        // AppUpdateInfo object obtained from the `getAppUpdateInfo()` method
        appUpdateInfo, 
        // Update options (using flexible update by default)
        AppUpdateOptions.Builder().build()
    )
    .addOnSuccessListener { resultCode ->
        if (resultCode == Activity.RESULT_CANCELED) {
            // User declined download
        }
    }
    .addOnFailureListener { throwable ->
        Log.e(TAG, "startUpdateFlow error", throwable)
    }
```

This method displays a confirmation dialog to the user. If the user agrees, it returns Activity.RESULT_OK; otherwise, Activity.RESULT_CANCELED.
Note: Each `AppUpdateInfo` object can only be used once. To call the method again, you need to request it again.

The `startUpdateFlow` method accepts update parameters. Currently, there are 3 types:
- AppUpdateOptions.Builder().build() - Flexible update (default)
- AppUpdateOptions.Builder().appUpdateType(IMMEDIATE).build() - Immediate update
- AppUpdateOptions.Builder().appUpdateType(SILENT).build() - Silent update

#### Installing the Update
After the update is downloaded, you can start installation.
To initiate the update installation, call the `completeUpdate(appUpdateOptions: AppUpdateOptions)` method. Only two completion types can be passed: FLEXIBLE and SILENT, corresponding to flexible and silent updates respectively.

```
ruStoreAppUpdateManager.completeUpdate(AppUpdateType.FLEXIBLE)
     .addOnFailureListener { throwable ->
          Log.e(TAG, "completeUpdate error", throwable)
     }
```

- FLEXIBLE update type - the application will restart.
- SILENT update type - the application will close without restarting.

#### Using a Listener
To track the status of the update download, register a listener:

```
val installStateUpdateListener = InstallStateUpdateListener { installState ->
    when (installState.installStatus) {
        InstallStatus.DOWNLOADED -> {
            // Download completed, you can now start installing the update
        }
        InstallStatus.DOWNLOADING -> {
            val totalBytes = installState.totalBytesToDownload
            val bytesDownloaded = installState.bytesDownloaded
            
            // Download in progress. For example, display a ProgressBar
        }
        InstallStatus.FAILED -> {
            // An error occurred during download
        }
    }
}

// Before starting the update download, add the listener
ruStoreAppUpdateManager.registerListener(installStateUpdateListener)

// When tracking is no longer needed, remove the listener
ruStoreAppUpdateManager.unregisterListener(installStateUpdateListener)
```


### Distribution Conditions
This software, including source codes, binary libraries, and other files, is distributed under the MIT license. Licensing information is available in the `MIT-LICENSE.txt` document.

### Technical Support
If you have questions about integrating the Updates SDK, contact us via [this link](https://www.rustore.ru/help/sdk/updates).

[ru]: README.md
[en]: README.en.md
