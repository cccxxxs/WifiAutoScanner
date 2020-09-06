package bupt.cx2017212125.epidemicanalysis;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DBController extends AppCompatActivity {

    private DBAdapter dbAdapter;

    private EditText idText;
    private EditText ssidText;
    private EditText bssidText;
    private EditText timeText;

    private TextView labelView;
    private TextView displayView;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);

        // 用于util.date转为datetime
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        idText = (EditText)findViewById(R.id.tx_id);
        ssidText = (EditText)findViewById(R.id.tx_SSID);
        bssidText = (EditText)findViewById(R.id.tx_BSSID);
        timeText = (EditText)findViewById(R.id.tx_time);

        labelView = (TextView)findViewById(R.id.label);
        displayView = (TextView)findViewById(R.id.display);

        Button addButton = (Button) findViewById(R.id.btn_add);
        Button showAllButton = (Button) findViewById(R.id.btn_show_all);
        Button clearButton = (Button) findViewById(R.id.btn_clear);
        Button delAllButton = (Button) findViewById(R.id.btn_delete_all);

        Button queryButton = (Button) findViewById(R.id.btn_query);
        Button deleteButton = (Button) findViewById(R.id.btn_delete);
        Button updateButton = (Button) findViewById(R.id.btn_update);

        Button queryTimeButton = (Button)findViewById(R.id.btn_query_time);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WiFiRecord wifi = new WiFiRecord();
                wifi.setSSID(ssidText.getText().toString());
                wifi.setBSSID(bssidText.getText().toString());
                try {
                    Date time = df.parse(timeText.getText().toString());
                    wifi.setTime(time);
                }catch (ParseException e){
                    e.printStackTrace();
                }
                long column = dbAdapter.insert(wifi);
                if (column == -1) {
                    Toast.makeText(getApplicationContext(), "添加数据失败!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),"插入数据成功! ID为: "+String.valueOf(column),Toast.LENGTH_SHORT).show();
                }
            }
        });

        showAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WiFiRecord[] wifi = dbAdapter.queryAllData();
                if(wifi==null){
                    Toast.makeText(getApplicationContext(), "查询数据失败!", Toast.LENGTH_SHORT).show();
                    return;
                }
                labelView.setText("数据库: ");
                String msg = "";
                for(int i=0;i<wifi.length;i++){
                    msg+=wifi[i].toString()+"\n";
                }
                displayView.setText(msg);
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayView.setText("");
            }
        });

        delAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbAdapter.deleteAllData();
                String msg = "数据全部删除! ";
                labelView.setText(msg);
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long id = Integer.parseInt(idText.getText().toString());
                long result = dbAdapter.deleteOneData(id);
                String msg = "删除ID为: "+idText.getText().toString()+"的数据";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = Integer.parseInt(idText.getText().toString().trim());
                WiFiRecord[] wifi = dbAdapter.queryOneData(id);

                if(wifi == null){
                    String msg = "数据库中没有ID为:"+String.valueOf(id)+"的数据";
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    return;
                }
                labelView.setText("数据库: ");
                displayView.setText(wifi[0].toString());
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WiFiRecord wifi = new WiFiRecord();
                wifi.setSSID(ssidText.getText().toString());
                wifi.setBSSID(bssidText.getText().toString());
                try {
                    Date time = df.parse(timeText.getText().toString());
                    wifi.setTime(time);
                }catch (ParseException e){
                    e.printStackTrace();
                }
                long id = Integer.parseInt(idText.getText().toString());
                long count = dbAdapter.updateOneData(id, wifi);
                if(count==-1){
                    Toast.makeText(getApplicationContext(), "更新错误! ", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "成功更新 "+String.valueOf(count)+" 条数据", Toast.LENGTH_SHORT).show();
                }
            }
        });

        queryTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String time = timeText.getText().toString();
                try {
                    Date date = df.parse(time);
                    WiFiRecord[] wifi = dbAdapter.queryByTime(date);
                    if(wifi == null){
                        String msg = "数据库中没有数据";
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    labelView.setText("数据库: ");
                    String msg = "";
                    for(int i=0;i<wifi.length;i++){
                        msg+=wifi[i].toString()+"\n";
                    }
                    displayView.setText(msg);
                }catch (ParseException e){
                    e.printStackTrace();
                }
            }
        });
    }
}
