# "NeWork"

## Дипломная работа курса Нетологии "Android-разработчик с нуля".

### Краткое описание.

Приложение , в котором пользователи
могут создавать посты и события с медиафайлами, отмечать их на Яндекс картах, указывать места
работы и социальные связи (упоминание в постах, конференциях).

### Инструменты.

- Архитектура MVVM
- Библиотеки:
    - Material Design
    - ROOM
    - OKHTTP
    - Retrofit
    - Hilt
    - LiveData, Flow
    - Coroutines
    - YandexMapsMobile
    - Glide
    - Navigation
    - [ImagePicker](https://github.com/Dhaval2404/ImagePicker)

### Функционал приложения.

- **Основной экран.**
    - Функционал:
        - Просмотр стены постов, событий или юзеров
        - Возможность ставить like
        - Прослушивать аудио или видео вложение
        - Переход в детальную карточку поста или события
        - Переход в детальную карточку юзера
        - Возможность редактировать или удалить свой пост
        - Поделиться текстом поста или события
        - Переход через меню на авторизацию или регистрацию
        - Переход на создание постов или событий, если пользователь авторизирован

<img src ="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Основной%20Экран%20Пост.png" width=25% height=25%><img src ="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Основной%20Экран%20Ивент.png" width=25% height=25%><img src ="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Основной%20Экран%20Юзеры.png" width=25% height=25%>

Меню с авторизацией или профилем, в случае если пользователь авторизирован

<img src ="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Основной%20Экран%20Меню%20с%20Авторизацией.png" width=25% height=25%><img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Основной%20Экран%20Меню%20с%20Профилем.png" width=25% height=25%>

Если не авторизирован, выдаст диалог с прохождением авторизации.

<img src ="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Экран%20С%20просьбой%20Авторизации.png" width=25% height=25%>

- **Регистрация и авторизация.**
    - Функционал:
        - Возможность перейти на окно регистрации из авторизации
        - Загрузка аватара и его превью

<img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Экран%20Авторизации.png" width=25% height=25%><img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Экран%20Регистрации%20.png" width=25% height=25%>
    

- **Создание поста.**
    - Функционал:
        - Ввод текста поста
        - Ввод ссылки поста
        - Добавление вложения аудио или видео, изображения из галереи или фото с камеры
        - Прослушивать аудио или видео вложение
        - Переход на фрагмент карты с добавлением метки
        - Переход на фрагмент со списком пользователей с возможностью их отметить 

<img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Экран%20Создания%20Поста%201%20(Основной).png" width=25% height=25%><img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Экран%20Создания%20Поста%201%20(Карта).png" width=25% height=25%><img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Экран%20Создания%20Поста%201%20(Упомянутые).png" width=25% height=25%>
<img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Экран%20Создания%20Поста%201%20(Вложение%20Фото).png" width=25% height=25%><img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Экран%20Создания%20Поста%201%20(Вложение%20АудиоВидео).png" width=25% height=25%>

- **Редактирование поста**
    - Функционал аналогичен созданию поста
  
<img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Экран%20Редактирования%20Поста.png" width=25% height=25%>


- **Детальная карточка поста**
    - Функционал :
      - Возможность поделиться текстом сообщения
      - Просмотр списка лайкеров и упомянутых людей, если их больше 5
      - Открытие фрагмента карты на весь экран
      - Прослушивание вложений  аудио или видео

<img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Пост%20детальная%20карточка%201.png" width=25% height=25%><img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Пост%20детальная%20карточка(Список%20Упомянутых)%202.png" width=25% height=25%><img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Пост%20детальная%20карточка%203%20(Фрагменты%20С%20картой).png" width=25% height=25%>

- **Создание и редактирование события.**
    - Функционал аналогичен посту, отличия:
        - Переход на фрагмент со списком пользователей с возможностью отметить их как спикеров
        - Указание формата события и даты его проведения

<img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Создания%20события%201.png" width=25% height=25%><img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Создания%20события%202.png" width=25% height=25%><img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Создания%20события%203.png" width=25% height=25%><img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Создания%20события%204.png" width=25% height=25%>


- **Детальная карточка события**
    - Функционал аналогичен посту отличия :
        - Просмотр списка лайкеров , спикеров и участников, если их больше 5 

<img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Детальная%20карточка%20события%20(Основа).png" width=25% height=25%>

- **Детальная карточка юзера**
    - Функционал :
        - Просмотр постов юзера
        - Возможность редактирования и удаления своих постов
        - Просмотр работ юзера
        - Добавление и удаление своих работ

<img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Детальная%20карточка%20юзера%201%20(Посты).png" width=25% height=25%><img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Детальная%20карточка%20юзера%202%20Работы).png" width=25% height=25%><img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Детальная%20карточка%20юзера%203%20(Создание%20Работы).png" width=25% height=25%>
<img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Детальная%20карточка%20юзера%203%20(Создание%20Работы%20Ввод%20Даты).png" width=25% height=25%><img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Детальная%20карточка%20юзера%203%20(Создание%20Работы%20Календарь).png" width=25% height=25%><img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Детальная%20карточка%20юзера%203%20(Создание%20Работы%20Ввод%20Даты%20В%20Ручную).png" width=25% height=25%>




    