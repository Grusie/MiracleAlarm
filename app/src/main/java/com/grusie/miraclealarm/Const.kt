package com.grusie.miraclealarm

class Const {
    companion object {
        const val RESULT_CODE_SOUND = 1001
        const val RESULT_CODE_VIBRATION = 1002

        const val INSERT_ALARM_TURN_OFF = 1100
        const val DELETE_ALARM_TURN_OFF = 1101

        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        const val ACTION_NOTIFICATION = "ACTION_NOTIFICATION"
        const val ACTION_MISSED_ALARM = "ACTION_MISSED_ALARM"
        const val ACTION_START_ACTIVITY = "ACTION_START_ACTIVITY"

        const val SHAKE_SKIP_TIME = 300
        const val SHAKE_THRESHOLD_GRAVITY = 1.7F
    }
}