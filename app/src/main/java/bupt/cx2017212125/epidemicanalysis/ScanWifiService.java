package bupt.cx2017212125.epidemicanalysis;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ScanWifiService extends Service {

    private DBAdapter dbAdapter;
    WiFiRecord wifi;

    private static final String TAG = "Scan Wifi";
    private final IBinder mBinder = new LocalBinder();

    List<ScanResult> wifiScanResult;
    private WifiManager mWifiManager;

    private Timer timer;
    private TimerTask timerTask;

    final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final int NOTIFICATION_ID = 1000;

    class LocalBinder extends Binder {
        ScanWifiService getService() {
            return ScanWifiService.this;
        }
    }

    // 服务绑定
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this, "本地绑定：ScanWifiService", Toast.LENGTH_SHORT).show();
        // 初始化WifiManager，否则闪退
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiScanResult = new ArrayList<>();
        // 初始化DBAdapter
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();
        wifi = new WiFiRecord();

        Log.i(TAG,"binded");

        // 获取服务通知
        Notification notification = createForegroundNotification();
        //将服务置于启动状态 ,NOTIFICATION_ID指的是创建的通知的ID
        startForeground(NOTIFICATION_ID, notification);

        return mBinder;
    }

    // 服务解绑
    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "取消绑定：ScanWifiService", Toast.LENGTH_SHORT).show();
        mWifiManager = null;
        wifiScanResult = null;
        return false;
    }

    public void startAutoScan(){
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run(){
                // 扫描Wi-Fi
                scanWifi();
            }
        };
        // 定时运行
        timer.schedule(timerTask, 0, 1000);
    }

    // 定时运行停止
    public void stopAutoScan(){
        timer.cancel();
        timer = null;
    }

    public void scanWifi(){
        mWifiManager.startScan();
        wifiScanResult = mWifiManager.getScanResults();
        if(wifiScanResult != null){
            String msg = "";
            for(ScanResult oneLine:wifiScanResult){
                Date date = new Date(System.currentTimeMillis());
                msg += oneLine.SSID + "\t" + oneLine.BSSID + "\t" + "当前日期时间"+df.format(date) + "\n";
                addData(oneLine.SSID, oneLine.BSSID, date);
            }
//            display.setText(msg);
            // 打log
            Log.i(TAG, msg);
        }
    }

    private void addData(String ssid, String bssid, Date time){
        wifi.setSSID(ssid);
        wifi.setBSSID(bssid);
        wifi.setTime(time);
        long column = dbAdapter.insert(wifi);
        if(column == -1){
            Log.i(TAG, "添加数据失败!");
        }else{
            Log.i(TAG, "已添加至数据库!");
        }
    }

    private Notification createForegroundNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 唯一的通知通道的id.
        String notificationChannelId = "notification_channel_id_01";

        // Android8.0以上的系统，新建消息通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //用户可见的通道名称
            String channelName = "Foreground Service Notification";
            //通道的重要程度
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, channelName, importance);
            notificationChannel.setDescription("Channel description");
            //LED灯
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            //震动
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId);
        //通知小图标
        builder.setSmallIcon(R.mipmap.ic_launcher);
        //通知标题
        builder.setContentTitle("ContentTitle");
        //通知内容
        builder.setContentText("ContentText");
        //设定通知显示的时间
        builder.setWhen(System.currentTimeMillis());
        //设定启动的内容
        Intent activityIntent = new Intent(this, NotificationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        //创建通知并返回
        return builder.build();
    }
}
