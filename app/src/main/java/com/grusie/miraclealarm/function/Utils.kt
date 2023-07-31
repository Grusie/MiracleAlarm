package com.grusie.miraclealarm.function

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore.Audio
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.MutableLiveData
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.model.AlarmData
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger
import kotlin.math.log
import kotlin.system.exitProcess

class Utils {
    companion object {
        lateinit var receiverIntent: Intent
        lateinit var alarmManager: AlarmManager
        private var mp: MediaPlayer? = null
        private var am : AudioManager? = null
        var initVolume = 0
        var hasAudioFocus = false

        fun updateAlarm(context: Context, exist : Boolean, oldAlarm: AlarmData, newAlarm: AlarmData){
            Log.d("confirm oldAlarm", "confirm oldAlarm : $oldAlarm, $newAlarm")
            if(exist){
                delAlarm(context, oldAlarm)
            }
            setAlarm(context, newAlarm)
        }
        /**
         * 알람 생성하기
         * */
        fun setAlarm(context: Context, alarm: AlarmData) {

            receiverIntent = Intent(context, AlarmNotiReceiver::class.java)
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            getAlarmTime(alarm).forEach { alarmTime ->

                val pendingIntentId = generateAlarmId(alarm, alarmTime.timeInMillis)

                receiverIntent.putExtra("alarmData", alarm)

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    pendingIntentId,
                    receiverIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // 일회성 알람 설정
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, alarmTime.timeInMillis, pendingIntent
                )

                Log.d(
                    "confirm contentValue",
                    "${alarm.time}, ${alarm.date}, ${alarm.id}, ${alarm.dateRepeat} 알람 설정 됨"
                )
            }
        }

        /**
         * 알람 개별 생성하기
         * */
        fun setAlarm(context: Context, timeMillis: Long, alarm: AlarmData) {

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
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent
                )
                val asd = Calendar.getInstance()
                asd.timeInMillis = timeMillis
                Log.d(
                    "confirm contentValue",
                    "${asd.time} ${alarm.time}, ${alarm.date}, ${alarm.id}, ${alarm.dateRepeat} 알람 설정 됨"
                )
        }


        // 알람을 고유하게 식별하기 위한 requestCode 생성 함수
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
        fun delAlarm(context: Context, alarm: AlarmData) {

            receiverIntent = Intent(context, AlarmNotiReceiver::class.java)
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


            getAlarmTime(alarm).forEach { alarmTime ->
                val pendingIntentId = generateAlarmId(alarm, alarmTime.timeInMillis)

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    pendingIntentId,
                    receiverIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }

            Log.d("confirm contentValue", "${alarm.id} 알람 제거 됨")
        }


        /**
         * 알람 울릴 시간 가져오기
         * **/
        fun getAlarmTime(alarm :AlarmData): ArrayList<Calendar> {
            val inputFormat = SimpleDateFormat("a hh:mm")
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
                        Log.e("confirm getAlarmTime", "getAlarmTime try-catch2 ${e.stackTrace}")
                    }
                }
                for(i in calList)
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
                Log.e("confirm getAlarmTime", "getAlarmTime try-catch1 $date, ${returnCal.time} ${Log.getStackTraceString(e)}")
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


        /**
         * 알람 종료
         **/
        fun stopAlarm(context: Context){
            val intent = Intent(context, ForegroundAlarmService::class.java)
            context.stopService(intent)
            stopAlarmSound(context)
            changeVolume(context, null, false)
        }

        /**
         * 알람 포커스 요청
         **/
        fun playAlarmSound(context: Context, sound: Int){
            audioFocus(context)
            startMusic(context, sound)
        }

        fun audioFocus(context: Context){
            // 오디오 포커스 요청
            val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
            val result = audioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                hasAudioFocus = true
            } else {
                Toast.makeText(context, "오디오 포커스 요청 실패", Toast.LENGTH_SHORT).show()
            }
        }

        /**
         * 사운드 플레이
         **/
        private fun startMusic(context: Context, sound: Int){
            mp = MediaPlayer.create(context, sound)
            mp?.isLooping = true
            mp?.start()
        }


        /**
         * 오디오 포커스 리스너
         **/
        private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
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
         * 알람 사운드 스탑
         **/
        fun stopAlarmSound(context: Context){
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
            if(am == null)
                am = context.getSystemService(AUDIO_SERVICE) as AudioManager
            Log.d("confirm volume", "$volume")

            val changeVolume = if(volume != null){
                var tempVolume =
                    if(!isConnected)
                        am!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * volume / 100
                    else{
                        var maxVolume = 70
                        if(volume > maxVolume) maxVolume else volume
                    }
                am!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * tempVolume / 100
            }else{
                initVolume
            }

            if(changeVolume != am!!.getStreamVolume(AudioManager.STREAM_MUSIC)) {
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
        fun initVolume(context: Context){
            if(am == null)
                am = context.getSystemService(AUDIO_SERVICE) as AudioManager
            initVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC)
        }

        /**
         * confirm 다이얼로그 만들기
         * */
        fun createConfirm(activity: Activity,permission: String, title:String, message:String){
            val builder = AlertDialog.Builder(activity).apply{
                setTitle(title)
                setMessage(message)
                setCancelable(false)
                setNegativeButton("취소"){dialog, _ ->
                    dialog.dismiss()
                }
                setPositiveButton("수락"){_, _ ->
                    createPermission(permission)
                }
            }
            builder.create().show()
        }
    }
}