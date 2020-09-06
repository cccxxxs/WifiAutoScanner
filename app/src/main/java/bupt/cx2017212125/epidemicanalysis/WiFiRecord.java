package bupt.cx2017212125.epidemicanalysis;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WiFiRecord {
    private int ID = -1;
    private String SSID;
    private String BSSID;
    private Date time;

    // 用于Debug页面的显示
    @Override
    public String toString(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String result = " ";
        result += "ID: " + this.ID + "; ";
        result += "SSID: " + this.SSID + "; ";
        result += "BSSID: " + this.BSSID + "; ";
        if(this.time!=null){
            result += "Time: " + df.format(this.time) + "; ";
        }
        return result;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
