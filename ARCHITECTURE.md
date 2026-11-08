# DriveNext — как устроен проект

Пакет приложения: `com.drivenext.app`. Код разделён на три слоя — это и есть «чистая архитектура» из требований.

```
com.drivenext.app
├── domain            бизнес-правила, ничего не знает про Android и Supabase
│   ├── model         RegistrationData, Gender — данные анкеты
│   ├── repository    AuthRepository — интерфейс: «что умеет авторизация»
│   └── validation    Validator — правила проверки полей
├── data              откуда берутся данные
│   ├── local         SessionStorage — SharedPreferences (токен, онбординг пройден)
│   ├── network       NetworkChecker — есть ли интернет
│   ├── remote        SupabaseClientProvider — один клиент Supabase на всё приложение
│   └── repository    SupabaseAuthRepository — «как» авторизоваться: реализация AuthRepository
└── ui                экраны (Activity / Fragment) и их ViewModel
    ├── common        общее: RequestState, диалог ошибки, переходы, выбор даты, показ фото
    ├── start         SplashActivity + StartViewModel + StartDestination
    ├── noconnection  экран «Нет подключения»
    ├── onboarding    3 слайда во ViewPager2
    ├── welcome       «Войти» / «Зарегистрироваться»
    ├── login         вход
    ├── registration  3 шага регистрации + экран успеха
    └── main          главный экран (пока пустой)
```

## Правило зависимостей

`ui → domain ← data`

* **domain** ни от кого не зависит. `AuthRepository` — только интерфейс с двумя функциями `signIn` и `signUp`.
* **data** реализует интерфейсы из domain: `SupabaseAuthRepository : AuthRepository`.
* **ui** вызывает бизнес-логику **только из ViewModel**. Activity и Fragment ничего не проверяют и в сеть не ходят — они показывают данные и передают нажатия во ViewModel.

ViewModel хранит поле типа интерфейса:
`private val authRepository: AuthRepository = SupabaseAuthRepository(app)` — если завтра сменить бэкенд, поменяется одна строка, экраны не тронем.

## Как идут данные на примере входа

1. Пользователь вводит email и пароль → `LoginActivity` на каждое изменение текста спрашивает у ViewModel `isFormFilled(...)` и включает/выключает кнопку «Войти».
2. Нажатие «Войти» → `viewModel.signIn(email, password)`.
3. ViewModel сначала проверяет поле через `Validator.isEmailValid`. Если плохо — сразу возвращает id текста ошибки, экран показывает диалог.
4. Если хорошо — ставит `state = Loading` (экран показывает индикатор загрузки) и в корутине (`viewModelScope.launch`) вызывает `authRepository.signIn(...)`.
5. Репозиторий идёт в Supabase. Любое исключение превращает в понятный текст (`toUserMessage`) и возвращает `Result.failure`. При успехе сохраняет токен в `SessionStorage`.
6. ViewModel переводит `Result` в `RequestState.Success` или `RequestState.Error(message)`.
7. `LoginActivity` подписана на `state` (LiveData) и в `render(state)` открывает главный экран или показывает диалог с ошибкой сервера.

Регистрация устроена так же, только данные трёх шагов копятся в одной `RegistrationViewModel`. Её получают все три фрагмента через `activityViewModels()` — поэтому при «Назад» поля остаются заполненными.

## Ключевые понятия

| Понятие | Где в проекте | Простыми словами |
|---|---|---|
| **ViewBinding** | `ActivityLoginBinding.inflate(...)`, `binding.btnSignIn` | Android сам генерирует класс по XML-разметке, и к элементам обращаемся без `findViewById` |
| **ViewModel** | `LoginViewModel`, `RegistrationViewModel` | Хранит состояние экрана и переживает поворот экрана |
| **LiveData** | `val state: LiveData<RequestState>` | Наблюдаемое значение: экран подписан и обновляется сам |
| **sealed class** | `RequestState` | Ограниченный набор состояний: Idle, Loading, Success, Error |
| **Корутины** | `viewModelScope.launch { ... }` | Сетевой запрос без блокировки интерфейса; отменяется, когда экран закрыт |
| **Result** | `Result<Unit>` в `AuthRepository` | Ответ «успех или ошибка» без try/catch во ViewModel |
| **SharedPreferences** | `SessionStorage` | Маленькое хранилище ключ-значение на телефоне |
| **SplashScreen API** | `installSplashScreen()` в `SplashActivity` | Системная заставка при запуске |
| **ViewPager2 + FragmentStateAdapter** | `OnboardingActivity`, `OnboardingPagerAdapter` | Листаемые слайды, каждый слайд — фрагмент |

## Ответы на контрольные вопросы ЛР №2

**1. Что такое MaterialDatePicker?** Диалог выбора даты из Material Components. В проекте открывается из `ui/common/DatePicker.kt` по нажатию на поле даты или иконку календаря.

**2. Как создать через Builder и задать заголовок?**
`MaterialDatePicker.Builder.datePicker().setTitleText(R.string.select_date).build()`

**3. Как показать?** `datePicker.show(parentFragmentManager, "date_picker")` — диалог это фрагмент, поэтому нужен FragmentManager.

**4. Зачем `addOnPositiveButtonClickListener`?** Чтобы получить выбранную дату, когда пользователь нажал «ОК».

**5. Что такое `selectedDateUtcMillis`?** Число миллисекунд от 01.01.1970 по UTC. Переводим в дату: `Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()`, затем форматируем в `dd/MM/yyyy`.

**6. Что такое Uri и почему его получает приложение?** Адрес файла, а не сам файл. Системный пикер не даёт прямой путь к фото, он выдаёт Uri с временным разрешением на чтение — так безопаснее, и не нужны разрешения на всю галерею. Читаем через `contentResolver` (см. `ui/common/ImageLoading.kt`).

**7. Activity Result API и `registerForActivityResult`?** Современный способ запустить другой экран (галерею, камеру) и получить результат. Контракт регистрируется заранее, при создании фрагмента — в `Step3Fragment` это поля `pickImageFromGallery` и `takePhotoWithCamera`.

**8. Что делает `PickVisualMedia`?** Открывает системный выбор фото и возвращает `Uri?` выбранного изображения (null — если пользователь ничего не выбрал).

**9. Что делает `TakePicture`?** Открывает камеру, которая сохраняет снимок в переданный нами Uri, и возвращает `Boolean` — получилось сохранить или нет.

**10. Зачем FileProvider?** Камера — другое приложение, и ему нельзя дать прямой путь к нашей внутренней папке. FileProvider выдаёт `content://` Uri на файл в `cacheDir/images` с временным разрешением на запись. Настроен в `AndroidManifest.xml` (`<provider>`) и `res/xml/file_paths.xml`.

## Supabase

* Ключи лежат в `local.properties` (`supabase.url`, `supabase.key`), в git не попадают. Gradle кладёт их в `BuildConfig`.
* Анкета передаётся при регистрации в метаданных пользователя, а SQL-триггер из `supabase/profiles.sql` копирует её в таблицу `profiles`.
* Таблица защищена RLS: каждый пользователь видит только свою строку.

## Тесты

`app/src/test/.../ValidatorTest.kt` — проверки правил email, номера прав и дат. Запуск: правой кнопкой по файлу → Run.
