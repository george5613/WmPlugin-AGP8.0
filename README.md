## 修改[WmPlugin](https://github.com/meituan/WMRouter/tree/master/WmPlugin "WmPlugin")适配到Gradle 8.0

## To Run

To execute example you need to enter command:

`./gradlew :app:assembleDebug`

You will see output similar to following:

```
> Task :app:debugGenRouteServiceInitClass
GenerateInit: start...
found class: com.truecolor.router.generated.service.ServiceInit_4f40d69fdd68b71d269d994dc4fadbce.class
found class: com.truecolor.router.generated.service.ServiceInit_11c03b3ae0bb10e1c437a62fedfd04b8.class
found class: com.truecolor.router.generated.service.ServiceInit_8ed96e8b6ac379f4b63a17eb5108f307.class
found class: com.truecolor.router.generated.service.ServiceInit_33bbbff58b1818b41577307a2ec26584.class
GenerateInit: finished cost 2972ms
```

## [ 2024 年 8 月 31 日起：新应用和应用更新必须以 Android 14](https://developer.android.com/google/play/requirements/target-sdk?hl=zh-cn)

## [特定 Android API 级别所要求的最低工具版本](https://developer.android.com/build/releases/gradle-plugin?hl=zh-cn#api-level-support)

## [Transform API 移除]([Android Gradle 插件 API 更新 &nbsp;|&nbsp; Android Studio &nbsp;|&nbsp; Android Developers](https://developer.android.com/build/releases/gradle-plugin-api-updates?hl=zh-cn#transform-removed))

```kotlin
variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
    .use(taskProvider)
    .toTransform(
        ScopedArtifact.CLASSES,
        ModifyClassesTask::allJars,
        ModifyClassesTask::allDirectories,
        ModifyClassesTask::output,
    )
```

## [修改自 gradle-recipes/transformAllClasses](https://github.com/android/gradle-recipes/tree/agp-8.5/transformAllClasses)












