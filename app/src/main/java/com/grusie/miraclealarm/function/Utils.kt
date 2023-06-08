package com.grusie.miraclealarm.function

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.model.AlarmData
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.util.*
import kotlin.system.exitProcess

class Utils {
    companion object {
        lateinit var receiverIntent: Intent
        lateinit var alarmManager: AlarmManager

        /**
         * 알람 생성하기
         * */
        fun setAlarm(context: Context, alarmTime: Calendar, alarm: AlarmData) {
            val dateRepeat = alarm.dateRepeat
            val alarmId = alarm.id
            val alarmTitle = alarm.title
            val timeMillis = alarmTime.timeInMillis

            receiverIntent = Intent(context, AlarmNotiReceiver::class.java)
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            Log.d("confirm contentValue", "${alarmTime.time}, $dateRepeat, $alarmId 알람 설정 됨")
            receiverIntent.putExtra(
                "alarmId", alarmId
            )
            receiverIntent.putExtra(
                "contentValue", "${alarm.time} 알람"
            )
            receiverIntent.putExtra("title", alarmTitle)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                receiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmClockInfo = AlarmManager.AlarmClockInfo(alarmTime.timeInMillis, pendingIntent)

            if (dateRepeat) {
                // 반복 주기: 주마다
                val intervalMillis = AlarmManager.INTERVAL_DAY * 7

                // 알람 설정
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP, timeMillis, intervalMillis, pendingIntent
                )

                // 알람매니저가 실행되기 전에 해당 요일로 먼저 설정
                alarmManager.setAlarmClock(
                    alarmClockInfo, pendingIntent
                )
            } else {
                // 일회성 알람 설정
                alarmManager.setAlarmClock(
                    alarmClockInfo, pendingIntent
                )
            }
        }
        /**
         * 알람 지우기
         * */
        fun delAlarm(context: Context, alarm: AlarmData) {
            receiverIntent = Intent(context, AlarmNotiReceiver::class.java)
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.id,
                receiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)

            Log.d("confirm contentValue", "${alarm.id} 알람 제거 됨")
        }
        
        /**
         * 권한 체크 및 요청 setting용 권한인 경우 따로 처리
         * **/
        fun createPermission(permission: String) {
            val permissionlistener: PermissionListener = object : PermissionListener {
                override fun onPermissionGranted() {
                }

                override fun onPermissionDenied(deniedPermissions: List<String?>) {
                }
            }
            TedPermission.create()
                .setPermissionListener(permissionlistener)
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
                soundArray[3] -> R.raw.alarm_sound1
                soundArray[4] -> R.raw.alarm_sound2
                soundArray[5] -> R.raw.alarm_sound3
                soundArray[6] -> R.raw.alarm_sound4
                soundArray[7] -> R.raw.school
                soundArray[8] -> R.raw.chicken_sound
                soundArray[9] -> R.raw.ring
                soundArray[10] -> R.raw.alarm_clock
                soundArray[11] -> R.raw.emergency
                soundArray[12] -> R.raw.radiation
                soundArray[13] -> R.raw.mix
                else -> {
                    R.raw.desk_clock
                }
            }

            return returnSound
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