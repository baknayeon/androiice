package com.example.dahae.myandroiice.MainFunction;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.example.dahae.myandroiice.MainActivity;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class CheckPlan extends Service {

    String BrodcastInfo = "";
    String OrigNumber = "";
    String Message = "";

    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if( intent != null) {
            BrodcastInfo = intent.getStringExtra("BrodcastInfo");
            OrigNumber = intent.getStringExtra("OrigNumber");
            Message = intent.getStringExtra("Message");

            Log.d(MainActivity.TAG, "*CheckPlan "+BrodcastInfo+ " /"+ OrigNumber+ " /" +Message);
            checkPlan();
        }

        Log.d(MainActivity.TAG, "*CheckPlan Stop");
        this.stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    //checking Table name
    public void checkPlan(){

        try {
            Cursor cursorT = MainActivity.database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            Cursor cursorActivation = MainActivity.databaseForRecordTime.rawQuery("SELECT * FROM ActivationInfoTable", null);

            try{
                if(cursorT != null) {
                    if (cursorT.moveToFirst()) {
                        while ( !cursorT.isAfterLast() ) {
                            String tableNameDB = cursorT.getString(0);
                            if (tableNameDB != null) {
                                if (!tableNameDB.equals("android_metadata") && !tableNameDB.equals("sqlite_sequence")) {
                                    for (int i = 0; i < cursorActivation.getCount(); i++) {
                                        if (cursorActivation != null) {
                                            if (cursorActivation.moveToNext()) {
                                                String planNameInfo = cursorActivation.getString(1);
                                                String activationInfo = cursorActivation.getString(2);
                                                if(planNameInfo.equals(tableNameDB) && activationInfo.equals("true")){
                                                    String query = "SELECT * FROM " + tableNameDB + " where Keyword_level = 0";
                                                    Log.d(MainActivity.TAG, "TableNameDB" + tableNameDB);
                                                    ComplexPlan(0, query, "empty", tableNameDB);
                                                }}}}
                                    cursorActivation.moveToFirst();
                                }
                                cursorT.moveToNext();
                            }}}}
            }finally {
                if(cursorT != null) cursorT.close();
                if(cursorActivation != null) cursorActivation.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean ComplexPlan(int i, String query , String Complex, String tableName) {

        List listDone = new ArrayList();

        boolean result = false;
        try {
            Cursor cursorForComplex =  MainActivity.database.rawQuery(query, null);
            Cursor cursorForComplex2 =  MainActivity.database.rawQuery(query, null);
            cursorForComplex2.moveToFirst();

            try {
                if (cursorForComplex != null && cursorForComplex2 != null) {
                    for (int j = 0; j < cursorForComplex.getCount(); j++) {
                        if (!cursorForComplex2.moveToNext())
                            cursorForComplex2.moveToLast();

                        if (cursorForComplex.moveToNext()) {
                            int idNumberForComplex = cursorForComplex.getInt(0);
                            String triggerNameForComplex = cursorForComplex.getString(1);
                            int level_idForComplex = cursorForComplex.getInt(3);
                            int idNumberForComplex_Next = cursorForComplex2.getInt(0);

                            if (triggerNameForComplex.equals("And")) {

                                i++;
                                query = "SELECT * FROM " + tableName + " where _id between " + idNumberForComplex + " and " + idNumberForComplex_Next + " and keyword_level = " + i; //두 커서 사이의 levelid가 i인것을 찾아라
                                result = ComplexPlan(i, query, "And", tableName);
                                i--;
                                listDone.add(result);
                            } else if (triggerNameForComplex.equals("Or")) {

                                i++;
                                query = "SELECT * FROM " + tableName + " where _id between " + idNumberForComplex + " and " + idNumberForComplex_Next + " and keyword_level = " + i;
                                result = ComplexPlan(i, query, "Or",tableName);
                                i--;
                                listDone.add(result);
                            } else if (triggerNameForComplex.equals("Done")) {
                                Iterator iterator = listDone.iterator();
                                if (Complex.equals("And")) {
                                    while (iterator.hasNext()){
                                        Boolean and = (Boolean) iterator.next();

                                        if (and)
                                            result = true;
                                        else {
                                            result = false;
                                            break;
                                        }
                                    }
                                    if (i != 0)
                                       return result;

                                } else if (Complex.equals("Or")) {
                                    while (iterator.hasNext()){

                                        Boolean or = (Boolean) iterator.next();
                                        if (or) {
                                            result = true;
                                            break;
                                        }else
                                            result = false;
                                    }
                                    if (i != 0)
                                        return result;
                                } else {
                                    if (iterator.hasNext()) {

                                        if ((Boolean) iterator.next())
                                            result = true;
                                        else
                                            result = false;
                                    }
                                }
                            } else if (triggerNameForComplex.equals("End")) {
                                Iterator iterator = listDone.iterator();
                                Boolean finalresult = (Boolean) iterator.next();

                                listDone.clear();
                                Log.d(MainActivity.TAG, "***FINAL End Result : " + finalresult);

                                if (finalresult == true) {

                                    query = "SELECT * FROM " + tableName + " where keyword_level = -1";
                                    Cursor c =  MainActivity.database.rawQuery(query, null);
                                    for(int k =0; k < c.getCount(); k++) {
                                        c.moveToNext();
                                        String actionName = c.getString(1);
                                        if (actionName.equals("TellPhoneNum"))
                                            MainActivity.databaseForRecordTime.execSQL("UPDATE " + tableName +
                                                    " SET Keyword_Info = '" + String.valueOf(OrigNumber) + "'" +
                                                    " WHERE Keyword2 = 'TellPhoneNum'");

                                        else if (actionName.equals("TellSMS"))
                                            MainActivity.databaseForRecordTime.execSQL("UPDATE " + tableName +
                                                    " SET Keyword_Info = '" + Message + "'" +
                                                    " WHERE Keyword2 = 'TellPhoneNum'");

                                    }
                                    if (c.getCount() != 0) {
                                        getCurrentTime(tableName);
                                        Intent action = new Intent(this, MyAction.class);
                                        action.putExtra("tableName", tableName);
                                        startService(action);
                                    } else
                                        Log.e(MainActivity.TAG, "%ERROR");
                                }
                                return result;

                            } else {
                                if (checkTrigger(idNumberForComplex, tableName)) {
                                    Log.d(MainActivity.TAG, "*trigger true " + triggerNameForComplex + " level is " + level_idForComplex);
                                    result = true;
                                } else {
                                    Log.d(MainActivity.TAG, "*trigger false " + triggerNameForComplex + " level is " + level_idForComplex);
                                    result = false;
                                }
                                listDone.add(result);
                            }
                        }
                    }
                }
            }finally {
                if( cursorForComplex != null)
                    cursorForComplex.close();
                if( cursorForComplex2 != null)
                    cursorForComplex2.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public boolean checkTrigger(int num, String tableNameDB){

        float result_Brightness ;
        boolean result = false;

        try {

            if( MainActivity.database != null) {

                if (!tableNameDB.equals("android_metadata") && !tableNameDB.equals("sqlite_sequence")) {
                    Cursor PreviousCursorForComplex =   MainActivity.database .rawQuery("SELECT * FROM " + tableNameDB + " where _id =" + num, null);

                    if (PreviousCursorForComplex != null) {
                        for (int j = 0; j < PreviousCursorForComplex.getCount(); j++) {
                            if (PreviousCursorForComplex.moveToNext()) {
                                String triggerName = PreviousCursorForComplex.getString(1);
                                String triggerNInfo = PreviousCursorForComplex.getString(2);
                                if (triggerName != null) {

                                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                    //Sound and Vibration and Silence  detecting and setting
                                    AudioManager aManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                                    try {
                                        //wifi and data detecting
                                        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                                        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                                        //data on off setting
                                        ConnectivityManager conman = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                        Class conmanClass = Class.forName(conman.getClass().getName());
                                        Field connectivityManagerField = conmanClass.getDeclaredField("mService");
                                        connectivityManagerField.setAccessible(true);

                                        Intent batteryStatus = this.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

                                        if (triggerName.equals("WifiOn")) {
                                            if (wifi.isConnected())
                                                result = true;
                                        } else if (triggerName.equals("WifiOff")) {
                                            if (!wifi.isConnected())
                                                result = true;
                                        } else if (triggerName.equals("Sound")) {
                                            if (aManager.getRingerMode() == 2)
                                                result = true;
                                        } else if (triggerName.equals("Vibration")) {
                                            if (aManager.getRingerMode() == 1)
                                                result = true;
                                        } else if (triggerName.equals("Silence")) {
                                            if (aManager.getRingerMode() == 0)
                                                result = true;
                                        } else if (triggerName.equals("DataOn")) {
                                            if (mobile.isConnected())
                                                result = true;
                                        } else if (triggerName.equals("DataOff")) {
                                            if (!mobile.isConnected())
                                                result = true;
                                        } else if (triggerName.equals("BluetoothOn")) {
                                            if (mBluetoothAdapter.isEnabled())
                                                result = true;
                                        } else if (triggerName.equals("BluetoothOff")) {
                                            if (!mBluetoothAdapter.isEnabled())
                                                result = true;
                                        } else if (triggerName.equals("BrightnessUp")) {
                                            result_Brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                                            if (result_Brightness > Integer.parseInt(triggerNInfo))
                                                result = true;
                                        } else if (triggerName.equals("BrightnessDown")) {
                                            result_Brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                                            if (result_Brightness < Integer.parseInt(triggerNInfo))
                                                result = true;
                                        } else if (triggerName.equals("AirplaneModeOn")) {
                                            boolean isEnabled = Settings.System.getInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
                                            if (isEnabled)
                                                result = true;
                                        } else if (triggerName.equals("AirplaneModeOff")) {
                                            boolean isEnabled = Settings.System.getInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
                                            if (!isEnabled)
                                                result = true;
                                        } else if (triggerName.equals("LowBattery")) {
                                            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                                            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                                            float batteryPct = level / (float) scale;
                                            int battery = (int) (100 * batteryPct);

                                            if (battery < Integer.parseInt(triggerNInfo))
                                                result = true;
                                        } else if (triggerName.equals("FullBattery")) {
                                            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                                            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                                            float batteryPct = level / (float) scale;
                                            int battery = (int) (100 * batteryPct);

                                            if (battery > Integer.parseInt(triggerNInfo))
                                                result = true;

                                        } else if (triggerName.equals("Location")) {
                                            Log.d(MainActivity.TAG, "Location");
                                        } else if (triggerName.equals("PowerConnected")) {
                                            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                                            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                                    status == BatteryManager.BATTERY_STATUS_FULL;
                                            if (isCharging)
                                                result = true;
                                        } else if (triggerName.equals("PowerDisConnected")) {
                                            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                                            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                                    status == BatteryManager.BATTERY_STATUS_FULL;
                                            if (!isCharging)
                                                result = true;
                                        } else if (triggerName.equals("EarphoneIn")) {
                                            if (aManager.isWiredHeadsetOn())
                                                result = true;
                                        } else if (triggerName.equals("EarphoneOut")) {
                                            if (!aManager.isWiredHeadsetOn())
                                                result = true;
                                        } else if (triggerName.equals("Time")) {
                                            if (BrodcastInfo.contains("Time")) {
                                                StringTokenizer st = new StringTokenizer(triggerNInfo, "+");
                                                st.nextToken();

                                                Long currentTime = System.currentTimeMillis();
                                                Log.d(MainActivity.TAG, "현제 시간 " +currentTime );

                                                Long alarm = Long.valueOf(st.nextToken()).longValue();
                                                if( alarm < currentTime && currentTime < alarm+900 )
                                                    result = true;
                                            }
                                        } else if (triggerName.equals("ScreenOff")) {
                                            if (BrodcastInfo.contains("ScreenOff"))
                                                result = true;
                                        } else if (triggerName.equals("ScreenOn")) {
                                            if (BrodcastInfo.contains("ScreenOn"))
                                                result = true;
                                        } else if (triggerName.equals("SMSreceiver")) {
                                            if (BrodcastInfo.contains("SMSreceiver"))
                                                result = true;
                                        } else if (triggerName.equals("NewOutgoingCall")) {
                                            if (BrodcastInfo.contains("NewOutgoingCall"))
                                                result = true;
                                        } else if (triggerName.equals("CallReception")) {
                                            if (BrodcastInfo.contains("CallReception"))
                                                result = true;
                                        } else if (triggerName.equals("SensorLR")) {
                                            if (BrodcastInfo.contains("SensorLR"))
                                                result = true;
                                        } else if (triggerName.equals("UpsideDown")) {
                                            if (BrodcastInfo.contains("UpsideDown"))
                                                result = true;
                                        } else if (triggerName.equals("SensorBright")) {
                                            if (BrodcastInfo.contains("SensorBright"))
                                                result = true;

                                        } else if (triggerName.equals("SensorUPDOWN")) {
                                            if (BrodcastInfo.contains("SensorUPDOWN"))
                                                result = true;
                                        } else if (triggerName.equals("SensorClose")) {
                                            if (BrodcastInfo.contains("SensorClose"))
                                                result = true;
                                        } else if (triggerName.equals("SensorBright")) {
                                            if (BrodcastInfo.contains("SensorBright"))
                                                result = true;
                                        }
                                    } catch (Exception e) {
                                        e.getStackTrace();
                                    }}}}}}}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void getCurrentTime(String planName){

        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String strNow = sdfNow.format(date);

        try {
            if( MainActivity.databaseForRecordTime != null) {
                MainActivity.databaseForRecordTime.execSQL("INSERT INTO " +
                        "RecordTimeTable(planName, recordTime)" +
                        " VALUES ('" + planName + "', '" + strNow + "');");
                Cursor cursor = MainActivity.databaseForRecordTime.rawQuery("SELECT * FROM RecordTimeTable", null);
                try {
                    if (cursor != null) {
                        for (int i = 0; i < cursor.getCount(); i++) {
                            if (cursor.moveToNext()) {
                                int _idDB = cursor.getInt(0);
                                String planNameDBInfo = cursor.getString(1);
                                String recordTimeDBInfo = cursor.getString(2);
                                Log.d("Record", /*기록된게 많아지면 로그캣이 너무 많아지니까 태그 바꿈*/
                                        "INSERTED INTO TABLE \'RecordTimeTable\' - " +
                                                " ID: " + _idDB +
                                                " PLANNAME: " + planNameDBInfo +
                                                " RECORDTIME: " + recordTimeDBInfo);
                            }}}
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}



