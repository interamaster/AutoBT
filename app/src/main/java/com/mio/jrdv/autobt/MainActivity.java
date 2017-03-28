package com.mio.jrdv.autobt;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
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
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    //para el device manager

    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;

    //para el BT

    BluetoothAdapter mBluetoothAdapter;

    //para los switchs

    Switch switcADMINButton,swtichRUNNING;


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


        //1º)si ya se HIZO ADMIN empieza del tiron

        if ( ADMINYAOK) {


            if (!isMyServiceRunning(AutoBTService.class)) {

                Log.d("INFO", "ARAANCANDO SERVICE");
                startServiceYA();
            }
        }



        //2º)si no al lio

        setContentView(R.layout.activity_main);
        // For first switch button
        switcADMINButton = (Switch) findViewById(R.id.switch1);

        swtichRUNNING = (Switch) findViewById(R.id.switch3);


        if (!isMyServiceRunning(AutoBTService.class)) {

            swtichRUNNING.setChecked(false);
        }
        else {
            swtichRUNNING.setChecked(true);
        }


        //To hide AppBar for fullscreen.
        ActionBar ab = getSupportActionBar();
        ab.hide();


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



        switcADMINButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                if (bChecked) {

                    //habilitmaos admin


                    final Dialog dialog = new Dialog(MainActivity.this);
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




                } else {

                }
            }
        });


    }




    private void startServiceYA() {

        Intent intent =new Intent(this,AutoBTService.class);
        intent.putExtra(AutoBTService.EXTRA_MESSAGE,"DesdeMain");

        startService(intent);

        finish();


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
            }
            else
            {
                // cancle it.
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
                //TODO asi se puede bloquear!!! : mDPM.lockNow();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void start(View view) {

        //empezamos


        if (!isMyServiceRunning(AutoBTService.class)) {

            Log.d("INFO", "ARAANCANDO SERVICE");
            startServiceYA();
        }


    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////device manager//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
