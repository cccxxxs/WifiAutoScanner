package bupt.cx2017212125.epidemicanalysis;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DBAdapter {
    private static final String DB_NAME = "wifi.db";
    private static final String DB_TABLE = "wifiRecord";
    private static final int DB_VERSION = 1;

    private static final String KEY_ID = "_id";
    private static final String KEY_SSID = "ssid";
    private static final String KEY_BSSID = "bssid";
    private static final String KEY_TIME = "time";

    private SQLiteDatabase db;
    private final Context context;

    // 用于util.date转为datetime
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    DBAdapter(Context _context){
        this.context = _context;
    }

    // 打开数据库
    void open()throws SQLiteException{
        DBOpenHelper dbOpenHelper = new DBOpenHelper(context, DB_NAME, null, DB_VERSION);
        try {
            /*取得的实例是以读写的方式打开数据库，如果打开的数据库磁盘满了，此时只能读不能写.
            此时调用了getWritableDatabase的实例，那么将会发生错误*/
            db = dbOpenHelper.getWritableDatabase();
        } catch (SQLiteException ex) {
            /*取得的实例是先调用getWritableDatabase以读写的方式打开数据库，如果数据库的磁盘满了，
            此时返回打开失败，继而用getReadableDatabase的实例以只读的方式去打开数据库*/
            db = dbOpenHelper.getReadableDatabase();
        }
    }

    // Close the database
    public void close() {
        if (db != null) {
            db.close();
            db = null;
        }
    }

    long insert(WiFiRecord wifi) {
        ContentValues newValues = new ContentValues();

        newValues.put(KEY_SSID, wifi.getSSID());
        newValues.put(KEY_BSSID, wifi.getBSSID());
        // date格式转换
        String time = df.format(wifi.getTime());
        newValues.put(KEY_TIME, time);

        return db.insert(DB_TABLE, null, newValues);
    }

    public WiFiRecord[] queryAllData(){
        Cursor results = db.query(DB_TABLE, new String[]{KEY_ID, KEY_SSID, KEY_BSSID, KEY_TIME},
                null, null, null, null, null);
        return ConvertToWifiRecord(results);
    }

    public WiFiRecord[] queryOneData(long id){
        Cursor results = db.query(DB_TABLE, new String[]{KEY_ID, KEY_SSID, KEY_BSSID, KEY_TIME},
                KEY_ID + "=" + id, null, null, null, null);
        return ConvertToWifiRecord(results);
    }

    public WiFiRecord[] queryByTime(Date date){
        Cursor results = db.query(DB_TABLE, new String[]{KEY_ID, KEY_SSID, KEY_BSSID, KEY_TIME},
                "REPLACE(REPLACE(REPLACE(" + KEY_TIME + ",' ',''),':',''),'-','')+0" + ">=" + df.format(date).replace(" ", "").replace(":","").replace("-","") + "+0", null, null, null, null);
        return ConvertToWifiRecord(results);
    }

    private WiFiRecord[] ConvertToWifiRecord(Cursor cursor){
        int resultCounts = cursor.getCount();
        if(resultCounts == 0 || !cursor.moveToFirst()){
            return null;
        }
        WiFiRecord[] wiFiRecords = new WiFiRecord[resultCounts];
        for(int  i = 0; i < resultCounts; i++){
            wiFiRecords[i] = new WiFiRecord();
            wiFiRecords[i].setID(cursor.getInt(0));
            wiFiRecords[i].setSSID(cursor.getString(cursor.getColumnIndex(KEY_SSID)));
            wiFiRecords[i].setBSSID(cursor.getString(cursor.getColumnIndex(KEY_BSSID)));
            try {
                Date time = df.parse(cursor.getString(cursor.getColumnIndex(KEY_TIME)));
                wiFiRecords[i].setTime(time);
            }catch (ParseException e){
                e.printStackTrace();
            }
            cursor.moveToNext();
        }

        return wiFiRecords;
    }

    public long deleteAllData() {
        return db.delete(DB_TABLE, null, null);
    }

    public long deleteOneData(long id) {
        return db.delete(DB_TABLE, KEY_ID + "=" + id, null);
    }

    public long updateOneData(long id, WiFiRecord wifi) {
        ContentValues updateValues = new ContentValues();
        updateValues.put(KEY_SSID, wifi.getSSID());
        updateValues.put(KEY_BSSID, wifi.getBSSID());
        // date格式转换
        String time = df.format(wifi.getTime());
        updateValues.put(KEY_TIME, time);

        return db.update(DB_TABLE, updateValues, KEY_ID + "=" + id, null);
    }

    /* 静态Helper类，用于建立、更新和打开数据库 */
    private static class DBOpenHelper extends SQLiteOpenHelper {

        DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        /**
         * 手动创建表的SQL命令
         */
        private static final String DB_CREATE = "create table " + DB_TABLE + "(" + KEY_ID
                + " integer primary key autoincrement, " + KEY_SSID + " text not null, " + KEY_BSSID + " text not null, "
                + KEY_TIME + " datetime);";

        /**
         * 创建数据库中的表，并进行初始化工作
         */
        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DB_CREATE);
        }

        /**
         * 更新表，为了简单起见，并没有做任何的的数据转移，而仅仅删除原有的表后建立新的数据库表
         */
        @Override
        public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
            _db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            onCreate(_db);
        }
    }
}
