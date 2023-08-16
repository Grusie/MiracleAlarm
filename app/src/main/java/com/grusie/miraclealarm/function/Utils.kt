package com.grusie.miraclealarm.function

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.widget.Toast
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.model.AlarmData
import com.grusie.miraclealarm.model.AlarmTimeData
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Utils {
    companion object {
        private lateinit var receiverIntent: Intent
        lateinit var alarmManager: AlarmManager
        private var mp: MediaPlayer? = null
        private var am: AudioManager? = null
        private var vm: VibratorManager? = null
        private var vibrator: Vibrator? = null
        private var initVolume = 0
        var hasAudioFocus = false

        /**
         * 알람 생성하기
         * */
        fun setAlarm(context: Context, alarm: AlarmData): MutableList<AlarmTimeData> {

            receiverIntent = Intent(context, AlarmNotiReceiver::class.java)
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmTimeList = mutableListOf<AlarmTimeData>()

            getAlarmTime(alarm).forEach { alarmTime ->

                val pendingIntentId = generateAlarmId(alarm, alarmTime.timeInMillis)
                val alarmTimeData = AlarmTimeData().apply {
                    id = pendingIntentId
                    timeInMillis = alarmTime.timeInMillis
                    alarmId = alarm.id
                }

                receiverIntent.putExtra("alarmData", alarm)

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    pendingIntentId,
                    receiverIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // 일회성 알람 설정
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmClockInfo =
                        AlarmManager.AlarmClockInfo(alarmTime.timeInMillis, pendingIntent)
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP, alarmTime.timeInMillis, pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, alarmTime.timeInMillis, pendingIntent
                    )
                }

                alarmTimeList.add(alarmTimeData)
                Log.d("confirm contentValue alarmTimeList", "$alarmTimeList")

                Log.d(
                    "confirm contentValue",
                    "${alarm.time}, ${alarm.date}, ${alarm.id}, ${alarm.dateRepeat} 알람 설정 됨"
                )
            }

            return alarmTimeList
        }


        /**
         * 다음 울릴 알람 시간을 리턴 해주는 함수
         */
        fun calculateDaysDiff(currentDate: Calendar, alarmDate: Calendar): Int {
            val currentYear = currentDate.get(Calendar.YEAR)
            val alarmYear = alarmDate.get(Calendar.YEAR)

            if (currentYear == alarmYear) {
                return alarmDate.get(Calendar.DAY_OF_YEAR) - currentDate.get(Calendar.DAY_OF_YEAR)
            } else {
                val daysInCurrentYear = currentDate.getActualMaximum(Calendar.DAY_OF_YEAR) -
                        currentDate.get(Calendar.DAY_OF_YEAR) + 1
                val daysInAlarmYear = alarmDate.get(Calendar.DAY_OF_YEAR)
                return daysInCurrentYear + daysInAlarmYear
            }
        }

        fun createAlarmMessage(toastFlag: Boolean, timeInMillis: Long): String {
            val currentTime = Calendar.getInstance()
            val alarmTime = Calendar.getInstance().apply {
                this.timeInMillis = timeInMillis
            }

            val daysDiff = calculateDaysDiff(currentTime, alarmTime)

            val minutesDiff = (timeInMillis - currentTime.timeInMillis) / (60 * 1000)
            val hoursDiff = (timeInMillis - currentTime.timeInMillis) / (60 * 60 * 1000)
            val remainingMinutes = (minutesDiff % 60)

            fun formatTime(hours: Long, minutes: Long): String {
                return if (minutes + 1 >= 60) {
                    "${hours + 1}시간 후 알람이 울립니다."
                } else if (hours >= 1) {
                    "${hours}시간 ${minutes + 1}분 후 알람이 울립니다."
                } else {
                    "${minutes + 1}분 후 알람이 울립니다."
                }
            }

            return when {
                minutesDiff < 1 -> {
                    "1분 후 알람이 울립니다."
                }

                daysDiff == 0 && (timeInMillis - currentTime.timeInMillis) < 60 * 60 * 1000 -> {
                    formatTime(0, minutesDiff)
                }

                daysDiff == 0 || daysDiff == 1 -> {
                    formatTime(hoursDiff, remainingMinutes)
                }

                toastFlag -> {
                    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분", Locale.KOREAN)
                    val alarmTimeString = dateFormat.format(alarmTime.time)
                    "알람이 ${alarmTimeString}에 설정되었습니다."
                }

                else -> "${daysDiff}일 후 알람이 울립니다."
            }
        }


        /**
         * 알람 개별 생성하기
         * */
        fun setAlarm(context: Context, timeMillis: Long, alarm: AlarmData): AlarmTimeData {

            receiverIntent = Intent(context, AlarmNotiReceiver::class.java)
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntentId = generateAlarmId(alarm, timeMillis)

            receiverIntent.putExtra("alarmData", alarm)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                pendingIntentId,
                receiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 일회성 알람 설정
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(timeMillis, pendingIntent)
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent
                )
            }

            return AlarmTimeData(pendingIntentId, timeMillis, alarm.id)
        }


        /**
         * 알람을 고유하게 식별하기 위한 requestCode 생성 함수
         **/
        fun generateAlarmId(alarm: AlarmData, timeMillis: Long): Int {
            val cal = Calendar.getInstance().apply {
                timeInMillis = timeMillis
            }

            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

            // 요일과 시간, 그리고 알람의 고유한 id를 순차적으로 결합하여 고유한 requestCode 생성
            return (dayOfWeek * 10000) + (cal.get(Calendar.HOUR_OF_DAY) * 100) + cal.get(Calendar.MINUTE) + alarm.id
        }

        /**
         * 알람 지우기
         * */
        fun delAlarm(context: Context, alarmTimeId: Int) {

            receiverIntent = Intent(context, AlarmNotiReceiver::class.java)
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmTimeId,
                receiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }


        /**
         * 알람 울릴 시간 가져오기
         * **/
        private fun getAlarmTime(alarm: AlarmData): ArrayList<Calendar> {
            val inputFormat = SimpleDateFormat("a hh:mm", Locale.KOREAN)
            val calList = ArrayList<Calendar>()

            alarm.apply {
                val dateList = alarm.date.split(",")
                if (!dateRepeat) {
                    calList.add(dateToCal(date, time))
                } else {
                    try {
                        val inputCal = Calendar.getInstance()
                        inputCal.time = inputFormat.parse(time) as Date // 입력 시간을 설정

                        for (i in dateList) {
                            val cal = Calendar.getInstance()
                            val dayOfWeek = when (i) {
                                "월" -> Calendar.MONDAY
                                "화" -> Calendar.TUESDAY
                                "수" -> Calendar.WEDNESDAY
                                "목" -> Calendar.THURSDAY
                                "금" -> Calendar.FRIDAY
                                "토" -> Calendar.SATURDAY
                                else -> Calendar.SUNDAY // 요일 정보가 없는 경우 기본값으로 일요일(Sunday)을 설정합니다.
                            }
                            cal.set(Calendar.DAY_OF_WEEK, dayOfWeek)

                            // 시간 설정
                            cal.set(Calendar.HOUR_OF_DAY, inputCal.get(Calendar.HOUR_OF_DAY))
                            cal.set(Calendar.MINUTE, inputCal.get(Calendar.MINUTE))
                            cal.set(Calendar.SECOND, 0)

                            // 현재 날짜와 비교하여 이미 지난 날짜라면 다음 주의 동일한 요일로 설정
                            if (cal.before(Calendar.getInstance())) {
                                cal.add(Calendar.WEEK_OF_YEAR, 1)
                            }
                            calList.add(cal)
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "confirm getAlarmTime",
                            "getAlarmTime try-catch2 ${e.stackTraceToString()}"
                        )
                    }
                }
                for (i in calList)
                    Log.d("confirm getAlarmTime", "getAlarmTime ${i.time}")
            }

            return calList
        }

        /**
         * date가 요일이 아닌 날짜일 경우 Calendar를 반환해주는 함수
         * */
        fun dateToCal(date: String, time: String): Calendar {
            var returnCal = Calendar.getInstance()
            try {
                val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 (E) a hh:mm", Locale.KOREAN)
                val dateTime: Date = if (date.split(" ").size >= 4) {
                    dateFormat.parse("$date $time") as Date
                } else {
                    val tempDate = returnCal.get(Calendar.YEAR).toString() + "년 " + date
                    dateFormat.parse("$tempDate $time") as Date
                }
                returnCal.time = dateTime
            } catch (e: java.lang.Exception) {
                returnCal = Calendar.getInstance()
                returnCal.add(Calendar.DAY_OF_YEAR, 1)
                Log.e(
                    "confirm getAlarmTime",
                    "getAlarmTime try-catch1 $date, ${returnCal.time} ${Log.getStackTraceString(e)}"
                )
            }
            return returnCal
        }


        /**
         * 권한 체크 및 요청 setting용 권한인 경우 따로 처리
         * **/
        fun createPermission(permission: String) {
            val permissionListener: PermissionListener = object : PermissionListener {
                override fun onPermissionGranted() {
                }

                override fun onPermissionDenied(deniedPermissions: List<String?>) {
                }
            }
            TedPermission.create()
                .setPermissionListener(permissionListener)
                .setDeniedMessage("알람을 사용하려면 권한을 허용하여 주셔야 합니다.")
                .setPermissions(permission)
                .check()
        }

        /**
         * 알람 소리 선택
         * */
        fun getAlarmSound(context: Context, sound: String): Int {
            val soundArray = context.resources.getStringArray(R.array.sound_array)
            val returnSound = when (sound) {
                soundArray[0] -> R.raw.desk_clock
                soundArray[1] -> R.raw.clock
                soundArray[2] -> R.raw.beep_beep
                soundArray[3] -> R.raw.army
                soundArray[4] -> R.raw.alarm_sound1
                soundArray[5] -> R.raw.alarm_sound2
                soundArray[6] -> R.raw.alarm_sound3
                soundArray[7] -> R.raw.alarm_sound4
                soundArray[8] -> R.raw.school
                soundArray[9] -> R.raw.chicken_sound
                soundArray[10] -> R.raw.ring
                soundArray[11] -> R.raw.alarm_clock
                soundArray[12] -> R.raw.emergency
                soundArray[13] -> R.raw.radiation
                soundArray[14] -> R.raw.mix
                else -> {
                    R.raw.desk_clock
                }
            }

            return returnSound
        }

        fun startAlarm(context: Context, alarm: AlarmData) {
            if (alarm.flagSound) {
                val sound = getAlarmSound(context, alarm.sound)
                playAlarmSound(context, sound)
            }
            if (alarm.flagVibrate) {
                startVibrator(context, getVibrationEffect(context, alarm.vibrate), 0)
            }
        }

        /**
         * 알람 종료
         **/
        fun stopAlarm(context: Context) {
            val intent = Intent(context, ForegroundAlarmService::class.java)
            context.stopService(intent)
            stopAlarmSound(context)
            stopVibrator()
            changeVolume(context, null, false)
        }

        /**
         * 알람 포커스 요청
         **/
        fun playAlarmSound(context: Context, sound: Int) {
            audioFocus(context)
            startMusic(context, sound)
        }

        fun audioFocus(context: Context) {
            // 오디오 포커스 요청
            val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
            val result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
            )

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                hasAudioFocus = true
            } else {
                Toast.makeText(context, "오디오 포커스 요청 실패", Toast.LENGTH_SHORT).show()
            }
        }

        /**
         * 사운드 플레이
         **/
        private fun startMusic(context: Context, sound: Int) {
            mp = MediaPlayer.create(context, sound)
            mp?.isLooping = true
            mp?.start()
        }


        /**
         * 오디오 포커스 리스너
         **/
        private val audioFocusChangeListener =
            AudioManager.OnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        // 오디오 포커스를 잃은 경우 노래를 일시적으로 정지
                        mp?.pause()
                    }

                    AudioManager.AUDIOFOCUS_GAIN -> {
                        // 오디오 포커스를 얻은 경우 다시 노래 재생
                        mp?.start()
                    }
                }
            }

        /**
         * 알람 사운드 정지
         **/
        fun stopAlarmSound(context: Context) {
            Log.d("confirm stopAlarm", "$mp")

            if (hasAudioFocus) {
                val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
                audioManager.abandonAudioFocus(audioFocusChangeListener)
            }
            mp?.stop()
            mp?.release()
            mp = null
        }

        /**
         * 알람 볼륨 조절
         **/
        fun changeVolume(context: Context, volume: Int?, isConnected: Boolean) {
            if (am == null)
                am = context.getSystemService(AUDIO_SERVICE) as AudioManager
            Log.d("confirm volume", "$volume")

            val changeVolume = if (volume != null) {
                var tempVolume =
                    if (!isConnected)
                        volume
                    else {
                        var maxVolume = 70
                        if (volume > maxVolume) maxVolume else volume
                    }
                am!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * tempVolume / 100
            } else {
                initVolume
            }

            if (changeVolume != am!!.getStreamVolume(AudioManager.STREAM_MUSIC)) {
                am!!.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    changeVolume,
                    AudioManager.FLAG_SHOW_UI
                )
            }
        }

        /**
         * 볼륨 조절 전 볼륨 값 가져오기
         **/
        fun initVolume(context: Context) {
            if (am == null)
                am = context.getSystemService(AUDIO_SERVICE) as AudioManager
            initVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC)
        }

        /**
         * 진동 패턴 얻기
         **/
        fun getVibrationEffect(context: Context, vibration: String): Pair<LongArray, IntArray> {
            val vibrationArray = context.resources.getStringArray(R.array.vibration_array)
            return when (vibration) {
                vibrationArray[0] -> Pair(
                    longArrayOf(2000, 1000, 2000, 2000),
                    intArrayOf(0, 100, 0, 200)
                )

                vibrationArray[1] -> Pair(
                    longArrayOf(500, 1000, 500),
                    intArrayOf(100, 0, 100)
                )

                vibrationArray[2] -> Pair(
                    longArrayOf(500, 500, 500, 500, 500),
                    intArrayOf(100, 0, 150, 0, 200)
                )

                vibrationArray[3] -> Pair(
                    longArrayOf(1000, 500, 1000, 500, 1000, 2000),
                    intArrayOf(100, 0, 100, 0, 100, 0)
                )

                vibrationArray[4] -> Pair(
                    longArrayOf(400, 600, 400, 2000), intArrayOf(100, 0, 100, 0)
                )

                vibrationArray[5] -> Pair(
                    longArrayOf(100, 200, 300, 400), intArrayOf(50, 0, 100, 0)
                )

                vibrationArray[6] -> Pair(
                    longArrayOf(
                        100, 200, 100, 200, 100, 600,  // dot
                        400, 200, 400, 200, 400, 600,  // dash
                        // Add more dots and dashes as per Morse Code sequence.
                    ), intArrayOf(
                        100, 0, 100, 0, 100, 0,         // dot
                        150, 0, 150, 0, 150, 0,         // dash
                        // Add corresponding amplitudes for dots and dashes.
                    )
                )

                vibrationArray[7] -> Pair(
                    longArrayOf(300, 200, 300, 1000),
                    intArrayOf(100, 0, 100, 0)
                )

                vibrationArray[8] -> Pair(
                    longArrayOf(200, 100, 200, 100, 600, 300, 100, 300, 100, 1000),
                    intArrayOf(100, 0, 100, 0, 100, 0, 100, 0, 100, 0)
                )

                vibrationArray[9] -> Pair(
                    longArrayOf(1000, 500, 1000),
                    intArrayOf(100, 0, 100)
                )

                vibrationArray[10] -> Pair(
                    longArrayOf(100, 200, 300, 400, 500, 400, 300, 200),
                    intArrayOf(50, 0, 100, 0, 150, 0, 100, 0)
                )

                vibrationArray[11] -> Pair(
                    longArrayOf(200, 200, 200), intArrayOf(100, 0, 100)
                )

                vibrationArray[12] -> Pair(
                    longArrayOf(1000, 1000, 1000),
                    intArrayOf(100, 0, 100)
                )

                vibrationArray[13] -> Pair(
                    longArrayOf(500, 500, 500, 500, 500, 500),
                    intArrayOf(100, 0, 150, 0, 200, 0)
                )

                else -> Pair(longArrayOf(2000, 1000, 2000, 2000), intArrayOf(0, 100, 0, 200))
            }
        }


        /**
         * 알람 진동 시작
         **/
        fun startVibrator(context: Context, vibrationPair: Pair<LongArray, IntArray>, loop: Int) {
            val vibrationEffect =
                VibrationEffect.createWaveform(vibrationPair.first, vibrationPair.second, loop)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (vm == null) vm =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager

                vm?.cancel()
                vm?.vibrate(CombinedVibration.createParallel(vibrationEffect))
            } else {
                if (vibrator == null) context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator?.cancel()

                vibrator?.vibrate(vibrationEffect)
            }
        }


        /**
         * 알람 진동 정지
         **/
        fun stopVibrator() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                vm?.cancel()
                vm = null
            } else {
                vibrator?.cancel()
                vibrator = null
            }
        }


        /**
         * confirm 다이얼로그 만들기
         * */
        fun createConfirm(activity: Activity, permission: String, title: String, message: String) {
            val builder = AlertDialog.Builder(activity).apply {
                setTitle(title)
                setMessage(message)
                setCancelable(false)
                setNegativeButton("취소") { dialog, _ ->
                    dialog.dismiss()
                }
                setPositiveButton("수락") { _, _ ->
                    createPermission(permission)
                }
            }
            builder.create().show()
        }
    }
}