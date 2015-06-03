package in.co.hoi.cabshare;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchActivity extends Activity {

    LatLng locationCoordinate;
    LatLng currentCoordinate;
    CustomAutoCompleteTextView locationToSearch;
    List<SavedPlaceItem> savedPlaces = new ArrayList<SavedPlaceItem>();
    ListView list;
    CustomListAdapter adapter;

    String l;

    DownloadTask placesDownloadTask;
    DownloadTask placeDetailsDownloadTask;
    ParserTask placesParserTask;
    ParserTask placeDetailsParserTask;

    final int PLACES=0;
    final int PLACES_DETAILS=1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_search);

        // Getting a reference to the AutoCompleteTextView
        locationToSearch = (CustomAutoCompleteTextView) findViewById(R.id.search_location);
        locationToSearch.setThreshold(1);

        l = getIntent().getExtras().getString("Location");
        currentCoordinate = new LatLng(Double.parseDouble(getIntent().getExtras().getString("CurrentLatitude"))
                ,Double.parseDouble(getIntent().getExtras().getString("CurrentLongitude")));
        System.out.println(l);

        //To show the favorite places of user saved by him : current count 5
        populateSavedPlaces();
        list = (ListView)findViewById(R.id.saved_location);
        adapter=new CustomListAdapter(this, savedPlaces, getResources());
        list.setAdapter( adapter );


        // Adding textchange listener
        locationToSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Creating a DownloadTask to download Google Places matching "s"
                placesDownloadTask = new DownloadTask(PLACES);

                // Getting url to the Google Places Autocomplete api
                String url = getAutoCompleteUrl(s.toString());

                // Start downloading Google Places
                // This causes to execute doInBackground() of DownloadTask class
                placesDownloadTask.execute(url);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        // Setting an item click listener for the AutoCompleteTextView dropdown list
        locationToSearch.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                    long id) {

                ListView lv = (ListView) arg0;
                SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();

                HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);

                // Creating a DownloadTask to download Places details of the selected place
                placeDetailsDownloadTask = new DownloadTask(PLACES_DETAILS);

                // Getting url to the Google Places details api
                String url = getPlaceDetailsUrl(hm.get("reference"));

                // Start downloading Google Place Details
                // This causes to execute doInBackground() of DownloadTask class
                placeDetailsDownloadTask.execute(url);

            }
        });
    }

    private void populateSavedPlaces(){
        savedPlaces.add(new SavedPlaceItem(28.5549, 77.0842 ,"Indira Gandhi International Airport", "New Delhi"));
    }

    public void sendMessage(View view)
    {
        //System.out.println("Check Child 1");
        if(locationCoordinate == null)
            Toast.makeText(getApplicationContext(), "Please wait!! Searching location",
                    Toast.LENGTH_LONG).show();
        else {
            Intent intent = new Intent();
            intent.putExtra("latitude", Double.toString(locationCoordinate.latitude));
            intent.putExtra("longitude", Double.toString(locationCoordinate.longitude));
            //System.out.println("Check Child 2");
            if (l.contentEquals("S")) {
                //System.out.println("Check Child 3");
                setResult(2, intent);
            } else if (l.contentEquals("D")) {
                setResult(3, intent);
            }

            finish();
        }
    }

    /*****************  This function used by adapter ****************/
    public void onItemClick(int mPosition)
    {
        SavedPlaceItem tempValues = (SavedPlaceItem) savedPlaces.get(mPosition);
        locationToSearch.setText(tempValues.getAddress1()+", "+tempValues.getAddress2());
        locationCoordinate = new LatLng(tempValues.getCoordinates().latitude, tempValues.getCoordinates().longitude);
        // SHOW ALERT


    }

    private String getAutoCompleteUrl(String place){

        // Obtain browser key from https://code.google.com/apis/console
        String key = "key=AIzaSyBrl-RRzrwGisYJpFI2QhpcCeknXg_bSmw";

        // place to be be searched
        String input = "input="+place;

        //place around where results to be shown
        String location = "location="+Double.toString(currentCoordinate.latitude)+","+Double.toString(currentCoordinate.longitude);

        //biased result radius constraint
        String radius = "radius=50000";

        // place type to be searched
        String types = "types=geocode";

        // Sensor enabled
        String sensor = "sensor=false";

        //country for autocomplete results
        String components = "components=country:india";


        // Building the parameters to the web service
        String parameters = input+"&"+radius+"&"+location+"&"+types+"&"+sensor+"&"+key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

        return url;
    }

    private String getPlaceDetailsUrl(String ref){

        // Obtain browser key from https://code.google.com/apis/console
        String key = "key=AIzaSyBrl-RRzrwGisYJpFI2QhpcCeknXg_bSmw";

        //place around where results to be shown
        String location = "location="+Double.toString(currentCoordinate.latitude)+","+Double.toString(currentCoordinate.longitude);

        //biased result radius constraint
        String radius = "radius=50000";

        // place type to be searched
        String types = "types=geocode";

        // Sensor enabled
        String sensor = "sensor=false";

        //country for autocomplete results
        // String components = "components=country:in";

        // Output format
        String output = "json";

        // reference of place
        String reference = "reference="+ref;

        // Building the parameters to the web service
        String parameters = reference+"&"+radius+"&"+location+"&"+types+"&"+sensor+"&"+key;

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/details/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        private int downloadType=0;

        // Constructor
        public DownloadTask(int type){
            this.downloadType = type;
        }

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            switch(downloadType){
                case PLACES:
                    // Creating ParserTask for parsing Google Places
                    placesParserTask = new ParserTask(PLACES);

                    // Start parsing google places json data
                    // This causes to execute doInBackground() of ParserTask class
                    placesParserTask.execute(result);

                    break;

                case PLACES_DETAILS :
                    // Creating ParserTask for parsing Google Places
                    placeDetailsParserTask = new ParserTask(PLACES_DETAILS);

                    // Starting Parsing the JSON string
                    // This causes to execute doInBackground() of ParserTask class
                    placeDetailsParserTask.execute(result);
            }
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        int parserType = 0;

        public ParserTask(int type){
            this.parserType = type;
        }

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<HashMap<String, String>> list = null;

            try{
                jObject = new JSONObject(jsonData[0]);

                switch(parserType){
                    case PLACES :
                        PlaceJSONParser placeJsonParser = new PlaceJSONParser();
                        // Getting the parsed data as a List construct
                        list = placeJsonParser.parse(jObject);
                        break;
                    case PLACES_DETAILS :
                        PlaceDetailsJSONParser placeDetailsJsonParser = new PlaceDetailsJSONParser();
                        // Getting the parsed data as a List construct
                        list = placeDetailsJsonParser.parse(jObject);
                }

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            switch(parserType){
                case PLACES :
                    String[] from = new String[] { "description"};
                    int[] to = new int[] { android.R.id.text1 };

                    // Creating a SimpleAdapter for the AutoCompleteTextView
                    SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

                    // Setting the adapter
                    locationToSearch.setAdapter(adapter);
                    break;
                case PLACES_DETAILS :
                    HashMap<String, String> hm = result.get(0);

                    // Getting latitude from the parsed data
                    locationCoordinate = new LatLng(Double.parseDouble(hm.get("lat")),Double.parseDouble(hm.get("lng")));

                    // Getting reference to the SupportMapFragment of the activity_main.xml
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}