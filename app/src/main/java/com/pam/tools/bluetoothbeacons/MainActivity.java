package com.pam.tools.bluetoothbeacons;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pam.tools.customviews.UbuntuButton;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;


public class MainActivity extends AppCompatActivity {


    private UbuntuButton ubtnControlScan;
    private UbuntuButton ubtnOpenFile;
    private RecyclerView rvPairedDevices;


    private ProgressBar progressBar;
    private BluetoothAdapter bluetoothAdapter;

    private DeviceAdapter deviceAdapter;
    private Button ubtnExportToFile;

    private SharedPreferences sharedPreferences;
    private Handler handler;
    private Runnable runnable;
    private static final String FILE_NAME = "DeviceData.xls";
    private static final String DIRECTORY_NAME = "BluetoothBeacons";



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ubtnControlScan = findViewById(R.id.btn_control_scan);
        ubtnOpenFile = findViewById(R.id.ubtn_open_file);
        rvPairedDevices = findViewById(R.id.rv_paired_devices);
        progressBar = findViewById(R.id.progress_bar);
        ubtnExportToFile = findViewById(R.id.btn_export_to_file);
        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);


        try {

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(bluetoothReceiver, filter);

            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }


            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("row", 1);
            editor.apply();


            deviceAdapter = new DeviceAdapter();
            DeviceAdapter.deviceDetails = new ArrayList<>();
            rvPairedDevices.setLayoutManager(new LinearLayoutManager(this));
            rvPairedDevices.setAdapter(deviceAdapter);


            getPermissions();


            startDiscovery();

            ubtnControlScan.setOnClickListener(v -> {

                if (ubtnControlScan.getText().toString().equalsIgnoreCase("stop scan")) {
                    progressBar.setVisibility(View.INVISIBLE);
                    ubtnControlScan.setText("Start Scan");
                    handler.removeCallbacks(runnable);
                } else if (ubtnControlScan.getText().toString().equalsIgnoreCase("start scan")) {
                    ubtnControlScan.setText("Stop Scan");
                    handler.postDelayed(runnable, 0);
                }
            });


            ubtnExportToFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Saving file in external storage
                    File sdCard = Environment.getExternalStorageDirectory();
                    File directory = new File(sdCard.getAbsolutePath() + "/" + DIRECTORY_NAME);

                    //create directory if not exist
                    if (!directory.isDirectory()) {
                        directory.mkdirs();
                    }
                    //file path
                    File file = new File(directory, FILE_NAME);


                    WorkbookSettings wbSettings = new WorkbookSettings();
                    wbSettings.setLocale(new Locale("en", "EN"));
                    WritableWorkbook workbook;

                    try {
                        WritableSheet sheet;
                        workbook = Workbook.createWorkbook(file, wbSettings);
                        sheet = workbook.createSheet("DeviceList", 0);

                        try {
                            sheet.addCell(new Label(0, 0, "Name"));
                            sheet.addCell(new Label(1, 0, "Address"));// column and row
                            sheet.addCell(new Label(2, 0, "RSSI"));
                            sheet.addCell(new Label(3, 0, "Time Stamp"));


                            ArrayList<DeviceDetails> dd = DatabaseActivity.getData(MainActivity.this,
                                    DatabaseActivity.TABLE_NAME_CURRENT_DATA);

                            DatabaseActivity.removeTable(MainActivity.this,
                                    DatabaseActivity.TABLE_NAME_CURRENT_DATA);
                            DatabaseActivity.createTable(DatabaseActivity.TABLE_NAME_COMPLETE_DATA);
                            DatabaseActivity.insertData(dd, DatabaseActivity.TABLE_NAME_COMPLETE_DATA);

                            ArrayList<DeviceDetails> deviceDetails = DatabaseActivity.getData(MainActivity.this,
                                    DatabaseActivity.TABLE_NAME_COMPLETE_DATA);

                            int size = deviceDetails.size();
                            if (size > 0) {

                                for (int i = 0; i < size; i++) {
                                    sheet.addCell(new Label(0, i + 1, deviceDetails.get(i).getName()));
                                    sheet.addCell(new Label(1, i + 1, deviceDetails.get(i).getAddress()));
                                    sheet.addCell(new Label(2, i + 1, deviceDetails.get(i).getRssi() + " dBm"));
                                    sheet.addCell(new Label(3, i + 1, deviceDetails.get(i).getTimeStamp()));

                                }
                            }
                        } catch (RowsExceededException e) {
                            e.printStackTrace();
                        } catch (WriteException e) {
                            e.printStackTrace();
                        }
                        workbook.write();
                        try {
                            workbook.close();
                            Toast.makeText(getApplicationContext(), "Successfully written to file", Toast.LENGTH_LONG).show();
                        } catch (WriteException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }

                }
            });
        } catch (Exception e) {
            Log.e("Error- onCreate", e.toString());
        }



        ubtnOpenFile.setOnClickListener(v -> {
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                openFile(sdCard.getAbsolutePath() + "/" + DIRECTORY_NAME + "/" + FILE_NAME, MainActivity.this);
            } catch (Exception e){
                Log.e("Error- ubtnOpenFile", e.toString());
            }
        });
    }



    private void startDiscovery() {
        try{

            handler = new Handler();
            runnable = () -> {

                progressBar.setVisibility(View.VISIBLE);

                bluetoothAdapter.startDiscovery();
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                bluetoothAdapter.startDiscovery();
                handler.postDelayed(runnable, 5000);
            };
            runnable.run();

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }


    private void getPermissions() {

        if ( ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {

                progressBar.setVisibility(View.VISIBLE);
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                    progressBar.setVisibility(View.INVISIBLE);

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    String name = device.getName();
                    String timeStamp = getTimeStamp();
                    String address = device.getAddress();


                    boolean found = false;
                    for (int i = 0; i < DeviceAdapter.deviceDetails.size(); i++) {
                        if (address.equals(DeviceAdapter.deviceDetails.get(i).getAddress())) {
                            DeviceAdapter.deviceDetails.get(i).setRssi(rssi);
                            found = true;
                        }
                    }
                    if (!found) {
                        DeviceAdapter.deviceDetails.add(new DeviceDetails(device.getName(),
                                device.getAddress(), rssi, getTimeStamp()));
                    }
                    deviceAdapter.notifyDataSetChanged();


                    DatabaseActivity.openDataBase(MainActivity.this);
                    DatabaseActivity.createTable(DatabaseActivity.TABLE_NAME_CURRENT_DATA);
                    DatabaseActivity.insertSingleData(new DeviceDetails(name,
                            address, rssi, timeStamp), DatabaseActivity.TABLE_NAME_CURRENT_DATA);
                    DatabaseActivity.closeDataBase();

                }
            } catch (Exception e ){
                Log.e("Error-toothReceiver", e.toString());
            }
        }
    };



    public String getTimeStamp() {
        return new Timestamp( System.currentTimeMillis() ).toString();
    }


    private void openFile(final String path, Context context) {
        File file = new File(path);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        } else {
            uri = Uri.fromFile(file);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            Log.e("Err- openFile", e.toString());
        }
    }


    @Override
    protected void onDestroy() {

        if ( bluetoothAdapter != null ){
            bluetoothAdapter.disable();
        }
        unregisterReceiver( bluetoothReceiver );
        super.onDestroy();
    }
}
