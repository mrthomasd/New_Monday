package www.faehse.de.tester;

import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class tester extends FragmentActivity implements OnMapReadyCallback {
    /*
    // Google key URL

    // ============== YOU SHOULD MAKE NEW KEYS ====================//
    final String GOOGLE_KEY = "AIzaSyBOy5KtLlnqgdSvDn7QFlZGJv_NA02GrP8 ";
    LocationManager m;
    // we will need to take the latitude and the logntitude from a certain point
    // this is the center of New York
    final String latitude = "50.0618540";
    final String longtitude = "8.2127270";
    */
    // make Call to the url

    String temp = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=50.0618540,8.2127270&radius=10000&name=rewe&key=AIzaSyBOy5KtLlnqgdSvDn7QFlZGJv_NA02GrP8";

    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tester);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        new MarkerTask().execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.is
        // Sets the map type to be "hybrid"
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);


    }

    private class MarkerTask extends AsyncTask<Void, Void, String> {

        private static final String LOG_TAG = "ExampleApp";
        HttpURLConnection conn = null;
        // Invoked by execute() method of this object

        @Override
        protected String doInBackground(Void... args) {

            //HttpURLConnection conn = null;
            final StringBuilder json = new StringBuilder();
            try {
                // Connect to the web service
                URL url = new URL(temp);

                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                // Read the JSON data into the StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    json.append(buff, 0, read);
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error connecting to service", e);
                //throw new IOException("Error connecting to service", e); //uncaught
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return json.toString();
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String json) {

            try {
                // De-serialize the JSON string into an array of city objects
                JSONObject mainObj= new JSONObject(json);
                JSONArray jArray = mainObj.optJSONArray("results");
                for (int i = 0; i < jArray.length(); i++) {
                    //JSONObject jsonObj = jArray.optJSONObject(i);

                    JSONObject temp = jArray.optJSONObject(i);
                    JSONObject loc = temp.optJSONObject("geometry").optJSONObject("location");
                    double lat = Double.parseDouble(loc.getString("lat"));
                    double lng = Double.parseDouble(loc.getString("lng"));
                    LatLng latLng = new LatLng(lat,lng);

                    //move CameraPosition on first result
                    if (i == 0) {
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(latLng).zoom(13).build();

                        map.animateCamera(CameraUpdateFactory
                                .newCameraPosition(cameraPosition));
                    }

                    // Create a marker for each city in the JSON data.
                    JSONObject attr = temp.optJSONObject("geometry");
                    map.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .title(jArray.getJSONObject(i).optString("name"))
                            .snippet(jArray.getJSONObject(i).optString("vicinity"))
                            .position(latLng));
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error processing JSON", e);
            }

        }
    }


}