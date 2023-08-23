# MiracleAlarm

### MVVM패턴을 적용한 알람 앱

<a href="https://acoustic-station-8c0.notion.site/MiracleAlarm-fa1202369b1042e7b00d77a0a1b75dae?pvs=4"><img src = "https://github.com/Grusie/MiracleAlarm/assets/75468060/e724806c-a914-4270-a3c9-43c6bba5711e" width= "700" height = "300"/></a>

**[앱 다운로드 링크](https://acoustic-station-8c0.notion.site/MiracleAlarm-fa1202369b1042e7b00d77a0a1b75dae?pvs=4)**


<a href="https://acoustic-station-8c0.notion.site/MiracleAlarm-fa1202369b1042e7b00d77a0a1b75dae?pvs=4"><img src="https://img.shields.io/badge/Notion-black?style=flat-square&amp;logo=Notion&amp;logoColor=white" /></a>



## 개요
- 알람 어플리케이션
- 알람 생성 | 삭제 기능을 제공한다.
- 미션을 통한 알람 끄기 기능을 제공한다.
- 부재중 알람 표시 기능을 제공한다.

## 버전 1.0
- 미라클 알람 출시


## 설명
- Kotlin + MVVM + LiveData + DataBinding + Room
- 깔끔한 UI 디자인으로 높은 접근성 제공
- 알람 끄는 방법, 소리, 볼륨, 진동, 미루기 시간까지 조절 가능
- 시간 뿐 아니라 날짜 및 요일까지도 알람 생성이 가능하도록 구현
- AlarmManager, AudioManager, Vibration을 활용한 알람 기능
- BroadcastReceiver, ForegroundService, Notification을 활용한 알람 및 부재중 알람 표시 기능

## skills
- Room
- LiveData
- ViewModel
- DataBinding
- LifeCycle
- Coroutine
- Glide
- AdMob
- AlarmManager

## DB구조

### alarm_table

|Field           |Type							 |Description                         |
|----------------|-------------------------------|-----------------------------|
|**id(Primary key)** |Int          |알람 아이디|
|**title**          |String |알람 제목            |
|**time** |String|알람 시간(format -> a:hh:mm)|
|**holiday**  |Boolean|공휴일 알람 허용 여부(미사용)|
|**date**|String|알람 날짜 or 요일|
|**dateRepeat**     |Boolean|알람 요일 반복 여부|
|**enabled**   |Boolean|알람 사용 여부|
|**sound**  |String|알람 소리|
|**volume**|Int|알람 볼륨|
|**vibrate**  |String|알람 진동|
|**delay** |String|알람 미루기|
|**delayCount**  |Int|미루기 횟수|
|**flagSound**  |Boolean|알람 소리 사용 여부|
|**flagVibrate**         |Boolean|알람 진동 사용 여부|
|**flagOffWay**       |Boolean|알람 끄는 방법 사용 여부|
|**flagDelay**    |Boolean|알람 미루기 사용 여부|

### alarm_time_table

|Field           |Type							 |Description                         |
|----------------|-------------------------------|-----------------------------|
|**id(Primary key)** |Int          |알람 시간 아이디|
|**timeInMillis**          |Long |알람이 울릴 시간의 timeInMillis            |
|**alarmId** |Int|해당 알람의 id 값(외래키)|


### alarm_turn_off_table

|Field           |Type							 |Description                         |
|----------------|-------------------------------|-----------------------------|
|**id(Primary key)** |Int          |알람 끄는 방법 아이디|
|**turnOffWay**          |String |알람 끄는 방법|
|**alarmId** |Int|해당 알람의 id 값(외래키)|
