package com.example.test.test.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.test.test.service.AppLockService;

/**
 * Created by hhj on 2017/08/31.
 */

public class TestReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("hhh", "接收到广播:"+intent.getAction());
        Intent lockIntent = new Intent(context, AppLockService.class);
        context.startService(lockIntent);
    }
}
