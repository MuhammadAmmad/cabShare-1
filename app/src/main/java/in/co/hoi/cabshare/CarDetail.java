package in.co.hoi.cabshare;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by lenovo pc on 25-05-2015.
 */
public class CarDetail {
    int id;
    String regNumber;
    String model;
    String make;
    String color;

    public CarDetail(JSONObject carInfo){
        try {
            this.id = carInfo.getInt("id");
            this.regNumber = carInfo.getString("regnumber");
            this.model = carInfo.getString("model");
            this.make = carInfo.getString("make");
            this.color = carInfo.getString("color");
        }
        catch(Exception e){
            Log.d("Exception", "Car Details");
        }
    }
}
