# "NeWork"

## Дипломная работа курса Нетологии "Android-разработчик с нуля".

### Краткое описание.

Приложение в котором пользователи
могут создавать посты и события с медиафайлами и отмечать их на Яндекс картах, указывать места
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
    - [ImagePicker](https://github.com/Dhaval2404/ImagePicker)

### Функционал приложения.

- **Основной экран.**

    Содержит экран постов , событий и пользователей.

<img src ="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Основной%20Экран%20Пост.png" width=25% height=25%><img src ="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Основной%20Экран%20Ивент.png" width=25% height=25%><img src ="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Основной%20Экран%20Юзеры.png" width=25% height=25%>

   Также меню с авторизацией или профилем в случае если пользователь авторизирован

<img src ="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Основной%20Экран%20Меню%20с%20Авторизацией.png" width=25% height=25%><img src="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Основной%20Экран%20Меню%20с%20Профилем.png" width=25% height=25%>

- Функционал основных экранов :
  - Просмотр стены постов событий или юзеров
  - Возможность ставить like
  - Прослушивать аудио или видео вложение
  - Переход в  детальную карточку поста или события 
  - Переход в   детальную карточку юзера
  - Возможность редкатировать или удалить свой пост
  - Поделиться текстом поста или события
  - Переход через меню на авторизацию или регистрацию
  - Переход на создание постов или событий если вы авторизированы

В случае если вы не авторизированы вам выдаст диалог с прохождением регистрации или авторизации

<img src ="https://github.com/Shelq27/NeWork/blob/main/app/src/main/res/screenshots/Экран%20С%20просьбой%20Авторизации.png" width=25% height=25%>

