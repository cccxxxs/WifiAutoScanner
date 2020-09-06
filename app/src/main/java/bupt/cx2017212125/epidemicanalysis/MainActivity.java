package bupt.cx2017212125.epidemicanalysis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.Bundle;
import android.os.IBinder;

import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ScanWifiService scanWifiService;
    private boolean isScanning = false;

    private Button btn_start;
    private Button btn_scan;
    private Button btn_debug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btn_start = (Button)findViewById(R.id.btn_start);
        btn_scan = (Button)findViewById(R.id.btn_scan);
        btn_debug = (Button)findViewById(R.id.btn_debug);

        // 服务绑定
        final Intent serviceIntent = new Intent(MainActivity.this, ScanWifiService.class);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);

        // 获取定位权限，否则wifiScanResult为空
        ActivityCompat.requestPermissions(
                this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                1);

        // 持续扫描
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isScanning) {
                    scanWifiService.startAutoScan();
                    isScanning = true;
                }else{
                    scanWifiService.stopAutoScan();
                    isScanning = false;
                }
            }
        });

        // 单次扫描
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanWifiService.scanWifi();
            }
        });

        // 测试窗口
        btn_debug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int SUB_ACTIVITY1 = 1;
                Intent debug = new Intent(MainActivity.this, DBController.class);
                startActivityForResult(debug, SUB_ACTIVITY1);
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unbindService(mConnection);
        scanWifiService = null;
        btn_start = null;
        btn_scan = null;
        btn_debug = null;
    }

    @Override
    public void finish(){
        super.finish(); //activity永远不会自动退出了，而是处于后台。
        moveTaskToBack(true);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            scanWifiService = ((ScanWifiService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            scanWifiService = null;
        }
    };
}
