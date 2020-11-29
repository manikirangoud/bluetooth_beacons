package com.pam.tools.bluetoothbeacons;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseActivity {



    private static final String DATABASE_NAME = "Rithish_DB";
    public static final String TABLE_NAME_CURRENT_DATA = "currentData";
    public static final String TABLE_NAME_COMPLETE_DATA = "completeData";
    private static SQLiteDatabase sqLiteDatabase;



    private static final String CURRENT_DATA_TC_1 = "name",
            CURRENT_DATA_TC_2 = "address",
            CURRENT_DATA_TC_3 = "rssi",
            CURRENT_DATA_TC_4 = "timestamp";


    private static final String COMPLETE_DATA_TC_1 = "name",
            COMPLETE_DATA_TC_2 = "address",
            COMPLETE_DATA_TC_3 = "rssi",
            COMPLETE_DATA_TC_4 = "timestamp";



    private static final String CREATE_TABLE_CURRENT_DATA = "CREATE TABLE IF NOT EXISTS " +
            TABLE_NAME_CURRENT_DATA + "(" + CURRENT_DATA_TC_1 + " TEXT," + CURRENT_DATA_TC_2 +
            " TEXT," + CURRENT_DATA_TC_3 + " TEXT," + CURRENT_DATA_TC_4 + " TEXT" + ");" ;


    private static final String CREATE_TABLE_COMPLETE_DATA = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_COMPLETE_DATA +
            "(" + COMPLETE_DATA_TC_1 + " TEXT," + COMPLETE_DATA_TC_2
            + " TEXT," + COMPLETE_DATA_TC_3 + " TEXT," + COMPLETE_DATA_TC_4 + " TEXT);";



    private static final String DROP_TABLE_CURRENT_DATA = "DROP TABLE IF EXISTS " + TABLE_NAME_CURRENT_DATA;
    private static final String DROP_TABLE_COMPLETE_DATA = "DROP TABLE IF EXISTS " + TABLE_NAME_COMPLETE_DATA;




    public static void openDataBase(Context context) {

        sqLiteDatabase = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        Log.i("DataBase", "Opened!");
    }


    public static void closeDataBase() {

        if ( sqLiteDatabase != null && sqLiteDatabase.isOpen() ) {
            sqLiteDatabase.close();
        }
        Log.i("DataBase", "Closed!");
    }


    public static void createTable(String tableName){

        try {

            if (tableName.equals( TABLE_NAME_CURRENT_DATA )) {
                sqLiteDatabase.execSQL( CREATE_TABLE_CURRENT_DATA );
            } else if ( tableName.equals( TABLE_NAME_COMPLETE_DATA )){
                sqLiteDatabase.execSQL(CREATE_TABLE_COMPLETE_DATA);
            } else {
                Log.e("Create_table", "No match occurred!");
            }

            Log.i("Create_table", "Created! " + tableName );

        } catch (Exception e) {
            Log.e("err_create_table", e.toString());
        }
    }


    public static ArrayList<DeviceDetails> getData(Context context, String tableName ){


        ArrayList<DeviceDetails> deviceDetails = new ArrayList<>();

        try{
            openDataBase( context );

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + tableName , null);

            if( cursor != null && cursor.moveToFirst() ) {

                do {

                    DeviceDetails device = new DeviceDetails(
                            cursor.getString(cursor.getColumnIndexOrThrow(CURRENT_DATA_TC_1)),
                            cursor.getString(cursor.getColumnIndexOrThrow(CURRENT_DATA_TC_2)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(CURRENT_DATA_TC_3)),
                            cursor.getString(cursor.getColumnIndexOrThrow(CURRENT_DATA_TC_4)));

                    deviceDetails.add(device);

                } while (cursor.moveToNext());
                cursor.close();

                Log.i("Get data", "Got");
            }

        } catch (Exception e) {
            Log.e("Get Data", e.toString() );
        }

        return deviceDetails;
    }



    public static void removeTable(Context context, String tableName ) {

        try{

            if ( sqLiteDatabase != null ) {

                if ( !sqLiteDatabase.isOpen() ) {
                    openDataBase(context);
                }

                if (tableName.equals( TABLE_NAME_CURRENT_DATA )) {
                    sqLiteDatabase.execSQL( DROP_TABLE_CURRENT_DATA );
                    sqLiteDatabase.execSQL( CREATE_TABLE_CURRENT_DATA );
                } else if (tableName.equals( TABLE_NAME_COMPLETE_DATA )){
                    sqLiteDatabase.execSQL( DROP_TABLE_COMPLETE_DATA );
                    sqLiteDatabase.execSQL( CREATE_TABLE_COMPLETE_DATA );
                }
            }

        } catch (Exception e) {
            Log.e("Remove Table", e.toString() );
        }
    }


    public static void insertData(ArrayList<DeviceDetails> deviceDetails, String tableName) {

        try {

            for ( DeviceDetails details : deviceDetails ) {

                sqLiteDatabase.execSQL("INSERT INTO " + tableName + " VALUES('" + details.getName() +
                        "','" + details.getAddress() + "','" + details.getRssi() + "','" +
                        details.getTimeStamp() + "');");

            }

            Log.e("INSERT_ DATABASE", "SUCCESSFULLY INSERTED data");

        } catch ( Exception e ){
            Log.e("ERROR_INSERT_DATABASE", e.toString());
        }
    }



    public static void insertSingleData(DeviceDetails deviceDetails, String tableName) {
        try {

            sqLiteDatabase.execSQL("INSERT INTO " + tableName + " VALUES('" + deviceDetails.getName() +
                    "','" + deviceDetails.getAddress() + "','" + deviceDetails.getRssi() + "','" +
                    deviceDetails.getTimeStamp() + "');");

        } catch (Exception e) {
            if (!(e.toString().contains("no such table"))) {

                Log.e("ERROR_PLAY_NEXT_TRACK", e.toString());
            }
        }
    }






}