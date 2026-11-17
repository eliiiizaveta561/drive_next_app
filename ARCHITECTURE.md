# DriveNext — как устроен проект

Пакет приложения: `com.drivenext.app`. Код разделён на три слоя — это и есть «чистая архитектура» из требований.

```
com.drivenext.app
├── domain            бизнес-правила, ничего не знает про Android и Supabase
│   ├── model         RegistrationData, Gender, Car, UserProfile
│   ├── repository    интерфейсы AuthRepository, CarRepository, ProfileRepository
│   ├── search        CarSearch — очистка поискового запроса
│   └── validation    Validator — правила проверки полей
├── data              откуда берутся данные
│   ├── local         SessionStorage (SharedPreferences), ImageCompressor (сжатие фото)
│   ├── mapper        JSON-строка таблицы → доменная модель (toCar, toUserProfile)
│   ├── network       NetworkChecker — есть ли интернет
│   ├── remote        SupabaseClientProvider (Auth + Postgrest + Storage), runRequest — ошибки → текст
│   └── repository    SupabaseAuthRepository, SupabaseCarRepository, SupabaseProfileRepository
└── ui                экраны (Activity / Fragment) и их ViewModel
    ├── common        общее: RequestState, диалог ошибки, переходы, выбор даты, загрузка картинок
    ├── start         SplashActivity + StartViewModel + StartDestination
    ├── noconnection  экран «Нет подключения»
    ├── onboarding    3 слайда во ViewPager2
    ├── welcome       «Войти» / «Зарегистрироваться»
    ├── login         вход
    ├── registration  3 шага регистрации + экран успеха
    ├── main          MainActivity: контейнер фрагментов + нижнее меню (BottomNavigationView)
    ├── cars          общее для списков машин: CarAdapter, CarsState, CarListViewModel, CarListFragment
    ├── home          «Главная»: поиск + список автомобилей
    ├── search        «Результаты поиска»
    ├── favorites     вкладка «Избранное» (заглушка, экран — в ЛР №4)
    ├── settings      «Настройки»
    ├── profile       «Профиль»: смена аватара, выход
    └── stub          StubActivity — временный экран для переходов из следующих ЛР
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

## ЛР №3: главный экран, поиск, настройки, профиль

### Навигация

После входа открывается `MainActivity`. Это одна Activity с контейнером `FragmentContainerView` и нижним меню `BottomNavigationView`, а пункты меню описаны в `res/menu/bottom_nav.xml`:

| Вкладка | Иконка | Фрагмент |
|---|---|---|
| Главная | дом | `HomeFragment` |
| Избранное | закладка | `FavoritesFragment` (заглушка, экран будет в ЛР №4) |
| Настройки | шестерёнка | `SettingsFragment` |

* Нажатие на вкладку → `setOnItemSelectedListener` → `showTab()`: очищается стек вложенных экранов, и фрагмент вкладки подставляется через `replace`.
* Вложенные экраны («Результаты поиска», «Профиль») открываются через `openInCurrentTab()` с `addToBackStack`, поэтому нижнее меню остаётся на месте, как в макете, а системная кнопка «Назад» возвращает на предыдущий экран.
* Повторное нажатие на активную вкладку (`setOnItemReselectedListener`) возвращает к её первому экрану.
* Экраны из следующих ЛР («Детали», «Оформление аренды», «Мои бронирования», «Добавление автомобиля») пока открывают `StubActivity` с заголовком и кнопкой «Назад».

### «Главная» и «Результаты поиска»

Оба экрана показывают одинаковый список, поэтому общий код вынесен в пакет `ui/cars`:

* `item_car.xml` — карточка: модель, марка, цена «в день», коробка передач, топливо, фото, кнопки «Забронировать» и «Детали». Фото занимает половину ширины карточки (`layout_constraintWidth_percent`), поэтому вёрстка тянется под любой экран.
* `CarAdapter` — адаптер `RecyclerView`. Переопределяет `onCreateViewHolder` (создаёт карточку из XML), `onBindViewHolder` (заполняет её данными) и `getItemCount`. Внутри `CarViewHolder` хранятся ссылки на элементы через ViewBinding. Новый список применяется через `DiffUtil`, поэтому перерисовываются только изменившиеся карточки.
* `CarsState` — состояния экрана: `Loading`, `Content(cars)`, `Error(текст)`, `NoConnection`.
* `CarListViewModel` — общая загрузка: проверяет интернет, показывает индикатор, обрабатывает `Result` из репозитория. Наследники только говорят, что загружать: `HomeViewModel` загружает все машины, `SearchResultsViewModel` — машины по запросу.
* `CarListFragment` — рисует состояния (индикатор, список, «ничего не найдено», ошибка с кнопкой «Повторить попытку»). Если интернета нет, открывает экран «Нет подключения» и после восстановления связи сам повторяет загрузку.

Тексты ошибок взяты из задания: «Не удалось загрузить данные. Попробуйте снова.» и «Не удалось выполнить поиск. Попробуйте снова.». Текст ошибки от сервера дополнительно показывается в диалоге.

**Поиск.** `SearchView` на «Главной» → `OnQueryTextListener`:
* `onQueryTextChange` вызывается на каждый введённый символ; здесь ничего не отправляем, чтобы не делать запрос на каждую букву;
* `onQueryTextSubmit` вызывается по кнопке «Поиск» на клавиатуре: `CarSearch` очищает запрос, и открывается `SearchResultsFragment`, который с индикатором загружает результаты с сервера.

На сервер уходит запрос `cars?or=(brand.ilike.%q%,model.ilike.%q%)`, то есть регистронезависимый поиск по марке или модели. Символы, которые ломают такой фильтр (`, ( ) % _ *`), `CarSearch.normalize` убирает (см. `CarSearchTest`). Сам запрос хранится в аргументах фрагмента, а `SearchResultsViewModel` получает его через `SavedStateHandle`. Поэтому после поворота экрана или перезапуска приложения системой результаты загружаются заново по тому же запросу, а новый запрос с «Главной» обновляет список.

Фото машин загружает библиотека Coil (`ui/common/RemoteImages.kt`) с кэшем. Пока фото грузится или если загрузка не удалась, показывается заглушка `car_placeholder`.

### «Настройки» и «Профиль»

* `SupabaseProfileRepository.getProfile()` читает строку пользователя из таблицы `profiles`: имя, фамилию, email, пол, дату регистрации и `avatar_url`.
* «Настройки»: аватар, имя и email. Нажатие на карточку пользователя открывает «Профиль». Пункты меню: «Мои бронирования» и «Подключить свой автомобиль» открывают заглушки, «Уведомления» открывают системные настройки уведомлений приложения, «Пригласи друга» открывает системное окно «Поделиться», «Тема» и «Помощь» показывают сообщение «Раздел в разработке». Профиль перечитывается при каждом показе экрана, поэтому новый аватар сразу появляется и здесь.
* «Профиль»: аватар, имя, строка «Присоединился в июле 2024» (месяц в предложном падеже из `string-array`), почта, пол, «Выйти из профиля».
  * Нажатие на аватар открывает системный выбор фото (`PickVisualMedia`). `ImageCompressor` уменьшает фото до 512 px и сжимает в JPEG, затем фото загружается в Supabase Storage (бакет `avatars`, файл `{user_id}/avatar.jpg`), а ссылка сохраняется в `profiles.avatar_url`. Так выполняется требование «Сохранение изменений профиля».
  * «Выйти из профиля»: `auth.signOut()` и очистка `SessionStorage`, затем `openScreenAndClearBackStack(LoginActivity)` открывает экран входа с флагами `NEW_TASK | CLEAR_TASK`, поэтому стек навигации очищается.

## Ответы на контрольные вопросы ЛР №3

**1. Что такое SearchView и для чего он используется?** Виджет поля поиска с иконкой лупы. Пользователь вводит запрос, а приложение фильтрует или загружает данные. В проекте используется на «Главной» (`fragment_home.xml`, стиль `CarSearchView`: подсказка, `iconifiedByDefault=false`, кнопка «Поиск» на клавиатуре).

**2. Как обновлять список при изменении текста?** Установить `setOnQueryTextListener` и в `onQueryTextChange` / `onQueryTextSubmit` передавать текст во ViewModel. ViewModel загружает данные и кладёт их в LiveData, экран подписан на неё и передаёт новый список в адаптер (`submitList`), а `DiffUtil` обновляет изменившиеся карточки.

**3. Зачем `onQueryTextSubmit` и `onQueryTextChange`?** `onQueryTextChange` вызывается при каждом изменении текста и подходит для «живой» фильтрации. `onQueryTextSubmit` вызывается, когда пользователь подтвердил ввод. У нас по нему открываются «Результаты поиска», чтобы не нагружать сервер запросом на каждую букву. Возврат `true` означает, что событие обработано.

**4. Что такое RecyclerView?** Компонент для длинных и меняющихся списков. Он создаёт только видимые карточки и при прокрутке переиспользует их для других элементов, что экономит память и ускоряет прокрутку. В проекте это `rvCars` в `layout_car_list.xml`.

**5. Что такое Adapter?** Связующее звено между данными (`List<Car>`) и ячейками списка: он создаёт ViewHolder и заполняет его данными нужной позиции. В проекте это `CarAdapter`.

**6. Какие методы переопределяются в адаптере?** `onCreateViewHolder` создаёт View ячейки из XML (`item_car.xml`) и оборачивает в ViewHolder. `onBindViewHolder` заполняет ячейку данными по позиции (вызывается и при переиспользовании). `getItemCount` сообщает, сколько всего элементов.

**7. Что такое LayoutManager?** Объект, который отвечает за расположение элементов и прокрутку. Самые частые: `LinearLayoutManager` (вертикальный или горизонтальный список, у нас), `GridLayoutManager` (сетка) и `StaggeredGridLayoutManager` (плитки разной высоты).

**8. Для чего BottomNavigationView?** Для быстрого переключения между 3–5 основными разделами. Меню всегда видно внизу экрана, а выбранная вкладка подсвечена (у нас цвет `@color/bottom_nav_item`: выбранная иконка фиолетовая, остальные серые).

**9. Где описываются пункты меню?** В XML-ресурсе меню `res/menu/…`, у нас это `res/menu/bottom_nav.xml`. Каждый `<item>` содержит id, иконку и заголовок. Ресурс подключается атрибутом `app:menu="@menu/bottom_nav"`.

**10. Как переключать экраны?** Обычно через фрагменты: в `setOnItemSelectedListener` по `item.itemId` выбирается фрагмент и подставляется в контейнер (`supportFragmentManager.commit { replace(...) }`), как в `MainActivity.showTab()`. Другой вариант — Navigation Component (`NavHostFragment` + `setupWithNavController`), где переходы описываются в графе навигации.

## Supabase

* Ключи лежат в `local.properties` (`supabase.url`, `supabase.key`), в git не попадают. Gradle кладёт их в `BuildConfig`.
* Анкета передаётся при регистрации в метаданных пользователя, а SQL-триггер из `supabase/profiles.sql` копирует её в таблицу `profiles`.
* Таблица защищена RLS: каждый пользователь видит только свою строку.
* **ЛР №3:** выполнить `supabase/cars.sql` в SQL Editor. Скрипт создаёт таблицу `cars` с тестовыми машинами (читать могут все), добавляет колонку `profiles.avatar_url`, а также публичный бакет `avatars` с политиками: загружать файлы можно только в свою папку `{user_id}/`.
* Клиент подключает модули `Auth`, `Postgrest` (запросы к таблицам) и `Storage` (файлы). Все запросы обёрнуты в `runRequest` (`data/remote/RemoteRequest.kt`): любое исключение превращается в `Result.failure` с понятным текстом, поэтому приложение не падает.

## Тесты

* `app/src/test/.../ValidatorTest.kt` — проверки правил email, номера прав и дат.
* `app/src/test/.../CarSearchTest.kt` — очистка поискового запроса.

Запуск: правой кнопкой по файлу → Run.
