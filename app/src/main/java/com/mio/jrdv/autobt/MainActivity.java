package com.mio.jrdv.autobt;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity  {

    //para el device manager

    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;

    //para el BT

    BluetoothAdapter mBluetoothAdapter;

    //para los switchs

    Switch switcADMINButton,swtichRUNNING,switchWIFIDETECT,switchAutoWIFIOFF;

    //para el log

    public static final String DEBUG_TAG = "AUTOBT";

    //para el  TIMER

    Button   btnTimePicker;
    TextView txtTime;
    private int   mHour, mMinute;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setContentView(R.layout.activity_main);



/*
        //Get bluetooth adapter
          mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //You want to check the status when you open the app to change the button status(conncted/disconnected) through this boolean

        boolean bluetoothEnabled = mBluetoothAdapter.isEnabled();

        Log.d("INFO","BT is:"+bluetoothEnabled);

        Log.d("INFO","WIFI is:"+isConnectedViaWifi());

        Log.d("INFO","ENCHUFADO  is:"+isPlugged(this));

        if (!bluetoothEnabled)
        {

            //si esta apagado lo enciendo de momento

            //If you want to enable bluetooth

            mBluetoothAdapter.enable();

            Log.d("INFO","BT LO ENCENDIDO");


        }


        else {

            //si esta encendido lo apago de momento

            //If you want to enable bluetooth

            mBluetoothAdapter.disable();

            Log.d("INFO","BT LO APAGO");
        }
       */



        Boolean ADMINYAOK = Myapplication.preferences.getBoolean(Myapplication.PREF_BOOL_ADMINYAOK,false);//por defecto vale 0){



        Log.d("INFO","PREF_BOOL_ADMINYAOKo: "+ADMINYAOK);


        Boolean WIFIDETECT = Myapplication.preferences.getBoolean(Myapplication.PREF_BOOL_WIFIDETECT,false);//por defecto vale 0){

        Log.d("INFO","PREF_BOOL_WIFIDETECT: "+WIFIDETECT);


        Boolean AUTOWIFIOFF = Myapplication.preferences.getBoolean(Myapplication.PREF_BOOL_AUTOWIFIOFF,false);//por defecto vale 0){

        Log.d("INFO","PREF_BOOL_AUTOWIFIOFF: "+AUTOWIFIOFF);


        setContentView(R.layout.activity_main);
        // For first switch button
        switcADMINButton = (Switch) findViewById(R.id.switch1);

        swtichRUNNING = (Switch) findViewById(R.id.switch3);

        switchWIFIDETECT=(Switch) findViewById(R.id.switch2);

        switchAutoWIFIOFF=(Switch) findViewById(R.id.switch4);

        //para el timer

        btnTimePicker=(Button)findViewById(R.id.btn_time);

        txtTime=(TextView) findViewById(R.id.in_time);

        //RECUPERAMOS LAS PREFS


        int newHoraWIFITIMER = Myapplication.preferences.getInt(Myapplication.PREF_HORA_WIFI_OFF,8);//por defecto vale FALSE
        int newMinWIFITIMER = Myapplication.preferences.getInt(Myapplication.PREF_MIN_WIFI_OFF,55);//por defecto vale FALSE

        txtTime.setText(newHoraWIFITIMER+":"+newMinWIFITIMER);



        //1ºA)si ya se HIZO ADMIN empieza del tiron

        if ( ADMINYAOK) {


            if (!isMyServiceRunning(AutoBTService.class)) {

                Log.d("INFO", "ARAANCANDO SERVICE");
               // startServiceYA();//lo quitamos o se cierra del tiron!!!

                switcADMINButton.setChecked(true);
            }
        }

        else {

            switcADMINButton.setChecked(false);
        }



        //1ºB)si ya se HABILITO WIFI

        if ( WIFIDETECT) {

            switchWIFIDETECT.setChecked(true);

        }
        else{
            switchWIFIDETECT.setChecked(false);
        }


        //1ºC)si ya eata RUNNNING

        if (!isMyServiceRunning(AutoBTService.class)) {

            swtichRUNNING.setChecked(false);
        }
        else {
            swtichRUNNING.setChecked(true);
        }




        //1ºD)si ya eata AUTOWIFIOFF

        if (AUTOWIFIOFF) {

            switchAutoWIFIOFF.setChecked(true);
        }
        else {
            switchAutoWIFIOFF.setChecked(false);
        }


        //To hide AppBar for fullscreen.
        ActionBar ab = getSupportActionBar();
        ab.hide();



        //1ºE)ACTUALZAIMOS SI YA ESTA RUNNIG


        if (!isMyServiceRunning(AutoBTService.class)) {


            swtichRUNNING.setChecked(true);
        }
        //2º)si no al lio

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////device manager//////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        try
        {
            // Initiate DevicePolicyManager.
            mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
            // Set DeviceAdminDemo Receiver for active the component with different option
            mAdminName = new ComponentName(this, DeviceAdmin.class);

            if (!mDPM.isAdminActive(mAdminName)) {
                switcADMINButton.setChecked(false);

            }
            else
            {
                switcADMINButton.setChecked(true);
                switcADMINButton.setClickable(false);
                // Already is a device administrator, can do security operations now.
                //TODO asi se puede bloquear!!! : mDPM.lockNow();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }


        if (!switcADMINButton.isChecked()){

            //si no es admin..que lo pregunte dle tiron


        final Dialog dialog = new Dialog(this);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.dialogalertlayout);
        dialog.setTitle("INFO");


        ImageButton btnExit = (ImageButton) dialog.findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {



                dialog.dismiss();

                EnableAdmin();

                //decimos que ya se eligio


                Myapplication.preferences.edit().putBoolean(Myapplication.PREF_BOOL_ADMINYAOK,true).commit();



                //empezamos

               // startServiceYA();//No,,cuando le demos






            }
        });
        // show dialog on screen
        dialog.show();

        }


    //LISTENER DEL SWITCH ADMIN

            switcADMINButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                    if (bChecked) {

                        //habilitmaos admin


                        final Dialog dialog = new Dialog(MainActivity.this);
                        dialog.setContentView(R.layout.dialogalertlayout);
                        dialog.setCanceledOnTouchOutside(true);
                        dialog.setTitle("INFO");


                        dialog.setOnCancelListener(
                                new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        //http://stackoverflow.com/questions/9516287/android-click-event-outside-a-dialog
                                        //TODO When you touch outside of dialog bounds,
                                        //TODO the dialog gets canceled and this method executes.

                                        switcADMINButton.setChecked(false);
                                    }
                                }
                        );


                        ImageButton btnExit = (ImageButton) dialog.findViewById(R.id.btnExit);
                        btnExit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                                dialog.dismiss();

                                EnableAdmin();

                                //decimos que ya se eligio


                                Myapplication.preferences.edit().putBoolean(Myapplication.PREF_BOOL_ADMINYAOK, true).commit();


                                //empezamos

                                // startServiceYA();//No,,cuando le demos


                            }
                        });
                        // show dialog on screen
                        dialog.show();


                    } else {

                    }
                }
            });





        //LISTENER DEL SWITCH WIFI

        switchWIFIDETECT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                if (bChecked) {

                    //habilitmaos wifi


                            //decimos que ya se eligioPREF que si al wifi


                            Myapplication.preferences.edit().putBoolean(Myapplication.PREF_BOOL_WIFIDETECT, true).commit();



                } else {


                    //decimos que ya se eligioPREF que NO al wifi


                    Myapplication.preferences.edit().putBoolean(Myapplication.PREF_BOOL_WIFIDETECT, false).commit();

                }
            }
        });




        //LISTENER DEL SWITCH AUTOWIFIOFF

        switchAutoWIFIOFF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                if (bChecked) {

                    //habilitmaos AUTOWIFIOFF


                    //decimos que ya se eligioPREF que si al wifi


                    Myapplication.preferences.edit().putBoolean(Myapplication.PREF_BOOL_AUTOWIFIOFF, true).commit();

                    //y LANZAMOS EL TIMERSCHEDULER
                    TiemrChoose(null);



                } else {


                    //decimos que ya se eligioPREF que NO al wifi


                    Myapplication.preferences.edit().putBoolean(Myapplication.PREF_BOOL_AUTOWIFIOFF, false).commit();
                    //borramos LA ALARM DEL SERVICE!!

                    RESETALarmEnService();

                }
            }
        });



    }

    private void RESETALarmEnService() {

        //reinicimoa el SERVICe con EXTRA="resetAlarmAUTOWIFIOFF" para que borre la alarma
        Intent intent =new Intent(this,AutoBTService.class);
        intent.putExtra(AutoBTService.EXTRA_MESSAGE,"resetAlarmAUTOWIFIOFF");

        startService(intent);

    }


    private void startServiceYA() {

        Intent intent =new Intent(this,AutoBTService.class);
        intent.putExtra(AutoBTService.EXTRA_MESSAGE,"DesdeMain");

        startService(intent);

        finish();


    }



    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////PARA DISMISS AL TOCAR FUERA//////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // MotionEvent object holds X-Y values



        if(event.getAction() == MotionEvent.ACTION_DOWN) {

            Log.d(DEBUG_TAG, "La accion ha sido ABAJO 1");

            //actualzimos el valor del admin
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////device manager//////////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            try
            {
                // Initiate DevicePolicyManager.
                mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
                // Set DeviceAdminDemo Receiver for active the component with different option
                mAdminName = new ComponentName(this, DeviceAdmin.class);

                if (!mDPM.isAdminActive(mAdminName)) {
                    switcADMINButton.setChecked(false);

                }
                else
                {
                    switcADMINButton.setChecked(true);
                    switcADMINButton.setClickable(false);
                    // Already is a device administrator, can do security operations now.
                    //TODO asi se puede bloquear!!! : mDPM.lockNow();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }


        }

        return super.onTouchEvent(event);
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////saber si mi service esat runnig/////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////check connected to wifi//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //permiso para saber si esta en WIFI
   // <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

    private boolean isConnectedViaWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
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


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////device manager//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(REQUEST_CODE == requestCode)
        {
            if(requestCode == Activity.RESULT_OK)
            {
                // done with activate to Device Admin
                switcADMINButton.setChecked(true);
            }
            else
            {
                // cancle it.
                switcADMINButton.setChecked(false);

            }
        }
    }



    private void EnableAdmin() {



        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////device manager//////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        try
        {
            // Initiate DevicePolicyManager.
            mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
            // Set DeviceAdminDemo Receiver for active the component with different option
            mAdminName = new ComponentName(this, DeviceAdmin.class);

            if (!mDPM.isAdminActive(mAdminName)) {
                // try to become active
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Pulsa activar app!!");
                startActivityForResult(intent, REQUEST_CODE);
            }
            else
            {
                // Already is a device administrator, can do security operations now.
                switcADMINButton.setClickable(false);
                //TODO asi se puede bloquear!!! : mDPM.lockNow();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }





    public void start(View view) {

        //empezamos si no esta el admin avisamos


        if (!switcADMINButton.isChecked()){


            Toast.makeText(this, "YOU SHOULD ENABLE ADMIN...UP 2 YOU IF ANDROID STOP ME",  Toast.LENGTH_LONG).show();
    }





        if (!isMyServiceRunning(AutoBTService.class)) {

            Log.d("INFO", "ARRANCANDO SERVICE");

            //decimos que ya se eligio


            Myapplication.preferences.edit().putBoolean(Myapplication.PREF_BOOL_ADMINYAOK,true).commit();


            startServiceYA();
        }
        else {


            //ya estaba el service running
            //pero aun asi lo relanzamos por si se cambio algo..
            startServiceYA();

            finish();
        }


    }



    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////poner HORA AUTOWIFIOFF//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void TiemrChoose(View view) {

        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {

                        txtTime.setText(hourOfDay + ":" + minute);


                        //guardamos esos valores


                        Myapplication.preferences.edit().putInt(Myapplication.PREF_HORA_WIFI_OFF,hourOfDay).commit();

                        Myapplication.preferences.edit().putInt(Myapplication.PREF_MIN_WIFI_OFF,minute).commit();


                        //reiniciamos el service con esos valores nuevos sin finish y  con EXTRA:wifitimerChangeFromMain!!!

                            restartServiceMio();


                    }
                }, mHour, mMinute, false);


        timePickerDialog.show();

    }

    private void restartServiceMio() {

        //solo si esta habilitado el AUTOWIFIOFF

        Boolean AUTOWIFIOFF = Myapplication.preferences.getBoolean(Myapplication.PREF_BOOL_AUTOWIFIOFF,false);//por defecto vale 0){

        Log.d("INFO","PREF_BOOL_AUTOWIFIOFF esta en: "+AUTOWIFIOFF);


        if(AUTOWIFIOFF){



            int newHoraWIFITIMER = Myapplication.preferences.getInt(Myapplication.PREF_HORA_WIFI_OFF,8);//por defecto vale FALSE
            int newMinWIFITIMER = Myapplication.preferences.getInt(Myapplication.PREF_MIN_WIFI_OFF,55);//por defecto vale FALSE


            Log.d("INFO", "VALORES DE HORA Y MIN RECUPERAOS DE PREFS:"+newHoraWIFITIMER +" "+newMinWIFITIMER);


            Toast.makeText(this, "WIFI WILL BE OFF EVERY DAY AT:"+newHoraWIFITIMER +":"+newMinWIFITIMER,  Toast.LENGTH_LONG).show();

            Intent intent =new Intent(this,AutoBTService.class);
            intent.putExtra(AutoBTService.EXTRA_MESSAGE,"wifitimerChangeFromMain");

            startService(intent);

        }

       else {

            Toast.makeText(this, "AUTO WIFI OFF IS DISABLED...WILL NOT DO IT...",  Toast.LENGTH_LONG).show();
        }

    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////device manager//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
