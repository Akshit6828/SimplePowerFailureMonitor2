package com.rsargsyan.simplepowerfailuremonitor.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import com.rsargsyan.simplepowerfailuremonitor.background.SMSReceivedAlarmService;
import com.rsargsyan.simplepowerfailuremonitor.utils.Constants;
import com.rsargsyan.simplepowerfailuremonitor.utils.SMSUtil;
import com.rsargsyan.simplepowerfailuremonitor.viewmodel.SharedPreferenceLiveData;

import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.ALARMING_MESSAGE_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SMS_ALARM_KEY;

public class SMSBroadcastReceiver extends BroadcastReceiver {
    private LiveData<Boolean> shouldAlarm;
    private LiveData<String> alarmingSmsMsg;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constants.ACTION_SMS_RECEIVED.equals(intent.getAction())) {
            if (shouldAlarm == null) init(context);
            final Boolean shouldAlarmValue = shouldAlarm.getValue();
            if (shouldAlarmValue != null && shouldAlarmValue) {
                final String alarmingSmsMsgValue = alarmingSmsMsg.getValue();
                if (alarmingSmsMsgValue != null &&
                        SMSUtil.smsContainsText(intent, alarmingSmsMsgValue)) {
                    startSMSAlarmService(context);
                }
            }
        }
    }

    private void init(Context context) {
        if (shouldAlarm == null) {
            final SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(context);
            shouldAlarm =
                    new SharedPreferenceLiveData<>(Boolean.class, sharedPreferences, SMS_ALARM_KEY);
            shouldAlarm.observeForever(aBoolean -> {});
            alarmingSmsMsg = new SharedPreferenceLiveData<>(String.class,
                    sharedPreferences, ALARMING_MESSAGE_KEY);
            alarmingSmsMsg.observeForever(s -> {});
        }
    }

    private void startSMSAlarmService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, SMSReceivedAlarmService.class));
        } else {
            context.startService(new Intent(context, SMSReceivedAlarmService.class));
        }
    }
}
