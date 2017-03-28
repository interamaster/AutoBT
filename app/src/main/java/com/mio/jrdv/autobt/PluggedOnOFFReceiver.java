package com.mio.jrdv.autobt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PluggedOnOFFReceiver extends BroadcastReceiver {

    private boolean powerplugON;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
            powerplugON = false;
        } else if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            powerplugON = true;
        }
        Intent i = new Intent(context, AutoBTService.class);
        i.putExtra("powerplug_state",powerplugON);
        i.putExtra(AutoBTService.EXTRA_MESSAGE,"powerplug_state");
        context.startService(i);
    }
}
