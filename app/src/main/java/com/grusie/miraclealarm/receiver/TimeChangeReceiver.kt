package com.grusie.miraclealarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.grusie.domain.usecase.alarmtime.AlarmTimeUseCases
import com.grusie.miraclealarm.interfaces.MessageUpdateListener
import com.grusie.miraclealarm.mapper.toUiModel
import com.grusie.miraclealarm.util.Utils.Companion.createAlarmMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class TimeChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmTimeUseCases: AlarmTimeUseCases

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(Intent.ACTION_TIME_TICK)) {
            val job = Job()
            val scope = CoroutineScope(Dispatchers.Main + job)

            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    alarmTimeUseCases.getMinAlarmTimeUseCase()
                }

                result.onSuccess { alarmTimeDomainModel ->
                    val alarmTimeUiModel = alarmTimeDomainModel?.toUiModel()
                    alarmTimeUiModel?.let {
                        (context as? MessageUpdateListener)?.onMessageUpdated(
                            createAlarmMessage(false, it.timeInMillis)
                        )
                    }
                }.onFailure { exception ->
                    Log.e(
                        "${this@TimeChangeReceiver::class.simpleName}",
                        "time Change Receiver Error : ${exception.message}"
                    )
                }
            }
        }
    }
}