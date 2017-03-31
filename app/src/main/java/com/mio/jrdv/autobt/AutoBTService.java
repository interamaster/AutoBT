package com.mio.jrdv.autobt;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

public class AutoBTService extends Service {


    public static AutoBTService instance;


    //para el intnt Extra info

    public static final String  EXTRA_MESSAGE="mensaje";



    //para el BT

    BluetoothAdapter mBluetoothAdapter;

    //PARA LAS 24H WIFI OFF AUTO CON ALARMAMANGER BROADCASTRECEIVER

    private BroadcastReceiver AlarmBrodCastReceiver;
    private AlarmManager MiAlarmManager;
    private PendingIntent pendingIntentAlarma;


    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("INFO","INICIADO onCreate EN SERVICE!!");


        //intent PARA LA ALARMA RECEIVER:

        /*
        IntentFilter filter2 = new IntentFilter("MiIntentAlarmaReseteoTimers");
        AlarmBrodCastReceiver = new AlarmIntentReceiver();
        registerReceiver(AlarmBrodCastReceiver, filter2);
        */

        //NO LO HAGO COMOM DICE EN TODOS LADDOS CON UN PWENDING INTENT:
        //https://www.sitepoint.com/scheduling-background-tasks-android/

        Intent alarma =new Intent(this,AlarmIntentReceiver.class);
        pendingIntentAlarma=PendingIntent.getBroadcast(this,0,alarma,0);//1º 0=requestcode y 2º 0=flag




    }



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////METODO QUE SE EJECUTA CADA VEZ QUE SE RELANZA ESTE SERVICE//////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // TODO OJO ESTE METODO SE EJECUTA CADA VEZ QUE SE LANZA UN INTENT DE ESTE SERVICE

        //permisos BT:
        //Get bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //You want to check the status when you open the app to change the button status(conncted/disconnected) through this boolean

        boolean bluetoothEnabled = mBluetoothAdapter.isEnabled();



        Log.d("INFO", "REINICIADO onStartCommand EN SERVICE!!");

        instance = this;

        //RECUPERAMOS LAS PREFS


        Boolean WIFIDETECT = Myapplication.preferences.getBoolean(Myapplication.PREF_BOOL_WIFIDETECT,false);//por defecto vale FALSE


        if (intent != null) {


            Log.d("INFO", "intent not null  onStartCommand EN SERVICE!!" + intent.getStringExtra(EXTRA_MESSAGE));


            String intentExtra = intent.getStringExtra(EXTRA_MESSAGE);
            Log.v("TASK", "El mensaje recibido en AutoBTService es  : " + intentExtra);


            //1º)vemos si es del enchufe

            if (intentExtra != null && intentExtra.equals("powerplug_state")) {

                Log.d("INFO", "intent  powerplug_state onStartCommand EN SERVICE!!");

                boolean powerplugON = intent.getBooleanExtra("powerplug_state", true);

                //AQUI YA SABEMOS QUE ES POR UN PLUG/UNPLUG

                if (!powerplugON) {
                    //quitado el enchufe:

                    //1º)si o si quitamos BT si estaba encendido y  sigue sin estar enchufado
                    if (bluetoothEnabled && !isPlugged(instance))//instance=this
                    {

                        //si esta ENCENDIDO lo APAGO
                        mBluetoothAdapter.disable();

                        Log.d("INFO", "BT LO APAGO");


                    }


                } else {

                    //puesto en enchufe!!

                    //primero cheqeuamos wifi:


                    if (isConnectedViaWifi() && WIFIDETECT) {
                        //estamos en WIFI..pasamos no hacemos nada
                        Log.d("INFO", "ESTAMOS EN WIFI y WIFIDETECT MARCADO..NO HAGO NADA");

                    }

                    //PERO primero chequeamos esta enchufado!!
                    else if (isPlugged(instance)) {
                        //lo encendemos el BT
                        mBluetoothAdapter.enable();

                        Log.d("INFO", "BT LO ENCIENDO!!");

                        //TODO SONIDO

                    }


                }


            }


            //2º)vemos si es desdeMain por cambiar WIFITIMER


            if (intentExtra != null && intentExtra.equals("wifitimerChangeFromMain")) {

                Log.d("INFO", "RECIBIDO WIFI TIMER CHANGE ON SERVICE");


                //RECUPERAMOS LAS PREFS


                int newHoraWIFITIMER = Myapplication.preferences.getInt(Myapplication.PREF_HORA_WIFI_OFF,8);//por defecto vale FALSE
                int newMinWIFITIMER = Myapplication.preferences.getInt(Myapplication.PREF_MIN_WIFI_OFF,55);//por defecto vale FALSE


                Log.d("INFO", "VALORES DE HORA Y MIN RECUPERAOS DE PREFS:"+newHoraWIFITIMER +" "+newMinWIFITIMER);

                //emepezamos el timer de cada 24h SEGUN E VALOR RECIBIDO DEL TIMER SCHEDULER EN MAIN
                startAUTOWIFIOFFTIMER(newHoraWIFITIMER,newMinWIFITIMER);


            }




            //3º)ES LA HORA DE APAGAR EL WIFI SEGN EL ALARM PROGRAMADO:

            if (intentExtra != null && intentExtra.equals("Alarma_reseteo_timers")) {

                Log.d("INFO", "RECIBIDO ALARM WIFI MUST BE OFF ");

               WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

               // WifiManager wifiManager = (WifiManager) this.getSystemService(instance.WIFI_SERVICE);
               // wifiManager.setWifiEnabled(true);
                wifiManager.setWifiEnabled(false);
               // Use the following to check if it's enabled or not
               // boolean wifiEnabled = wifiManager.isWifiEnabled();





            }



            //4º)DESHABBILITAMOS EL TIMER PARA QUE NO APAGE EL WIFI PROGRAMADO

            if (intentExtra != null && intentExtra.equals("resetAlarmAUTOWIFIOFF")) {

                Log.d("INFO", "RECIBIDO APAGAR ALARM AUTOWIFIOFF PARA QUE NO SE EJECUTE ");

                //primerom anualmos la alarma anterior si existe

                if (MiAlarmManager!=null){
                    MiAlarmManager.cancel(pendingIntentAlarma);
                    Log.d("INFO"," ANULADA LA ALARMA ");


                }


                //o  alo bestia!!!


                Intent alarmIntent = new Intent(this, AlarmIntentReceiver.class);
                pendingIntentAlarma = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
                MiAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                MiAlarmManager.cancel(pendingIntentAlarma);
                Log.d("INFO"," ANULADA LA ALARMA  A LO BESTIA!! ");




            }
        }


        //return super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;


    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////ALARM DEL AUTOWIFIOFF//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




    private void startAUTOWIFIOFFTIMER(int HORA,int MIN) {

        // Schedule to run every day in midnight

        // Scheduling task at today : 00:00:22 PM
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, HORA);
        calendar.set(Calendar.MINUTE, MIN);
        calendar.set(Calendar.SECOND, 00);

        //este time es en el pasado por eso se ejecuta del tiron
        //asi que le añado 24 horas(1 DIA)

        //calendar.add(Calendar.DATE,1);//TODO VOLVER A PONER EN RELAIDAD

        Date time = calendar.getTime();
        long timeinMilisec=calendar.getTimeInMillis();

        Log.d("INFO"," TIMER WIFI-OFF EMPEZARA EL :  "+time.toString());


        //  int period = 10000;//10secs
        int perioddia= 1000 * 60 * 60 * 24 * 1;//24h


        //primerom anualmos la alarma anterior si existe

        if (MiAlarmManager!=null){
            MiAlarmManager.cancel(pendingIntentAlarma);
            Log.d("INFO"," ANULADA LA ALARMA ");


        }


        //o  alo bestia!!!


        Intent alarmIntent = new Intent(this, AlarmIntentReceiver.class);
        pendingIntentAlarma = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        MiAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        MiAlarmManager.cancel(pendingIntentAlarma);
        Log.d("INFO"," ANULADA LA ALARMA  A LO BESTIA!! ");


        MiAlarmManager=(AlarmManager)  getSystemService(Context.ALARM_SERVICE);
        MiAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeinMilisec, perioddia, pendingIntentAlarma);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////check connected to wifi//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //permiso para saber si esta en WIFI
    // <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

    private boolean isConnectedViaWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) instance.getSystemService(Context.CONNECTIVITY_SERVICE);//instance=this =service!!
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////check power state//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static boolean isPlugged(Context context) {
        boolean isPlugged= false;
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        isPlugged = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            isPlugged = isPlugged || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
        }
        return isPlugged;
    }






/////////////////////NO LO USO PERO ES MANADATORY//////////////////////////////////////////////////
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        return null;
    }




}
