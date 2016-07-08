package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * Created by abhishek.dixit on 1/5/2016.
 * A ForcastFragment fragment containing a simple view.
 */

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

   // private ArrayAdapter<String> mForecastAdapter;
    private ForecastAdapter mForecastAdapter;

    private static final int FORECAST_LOADER = 0;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // Dixit-Imp:In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Dixit:Add this line inorder for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


//        String[] forecast = {"Monday-Sunny-88/63",
//                "Tuesday-Windy-78/63",
//                "Wednesday-Foggy-68/63",
//                "Thursday-Rainy-58/63",
//                "Friday-Stromy-48/63",
//                "Saturday-Sunny-98/63"};

//        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecast));//converting forecast to an Array list data

//        mForecastAdapter = new ArrayAdapter<String>(
//                getActivity(),//Current Context:this fragmant's parent activity
//                R.layout.list_item_forecast,//ID of List item layout
//                R.id.list_item_forecast_textview,//ID of text View
//                new ArrayList<String >());
                //weekForecast); //forecast data


//        mForecastAdapter = new ForecastAdapter(getActivity(), cur, 0);
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //Get a refrence to the ListView, and attach this adapter to it
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                String forecast = mForecastAdapter.getItem(position);
//                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
//                Intent detailAct = new Intent(getActivity(), DetailActivity.class);
//                detailAct.putExtra(Intent.EXTRA_TEXT, forecast);
//                //if (detailAct.resolveActivity(getPackageManager()) != null) {
//                startActivity(detailAct);

                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Intent detailAct = new Intent(getActivity(), DetailActivity.class);
                    detailAct.setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                        locationSetting,
                                        cursor.getLong(COL_WEATHER_DATE)
                                        ));
                    startActivity(detailAct);
                        }
                    }
                });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);

    }

    //Called when Loader completes & our data is Ready , to use data in Our Cursor Adapter we just call SwapCursor
    // and it is here we perform anyother UI
    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        mForecastAdapter.swapCursor(cursor);
    }


    // called when our loader is being destroyed, it means we need to remove all refrences to loader data
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mForecastAdapter.swapCursor(null);
    }

    public void onLocationChanged(){

        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER,null,this);

    }

    //Dixit:Function Creating FetchWeatherTask & reading data from SharedPrefer on Refresh & AppStart
    public void updateWeather(){

//        FetchWeatherTask weatherTask = new FetchWeatherTask(0;)

    //  commented in lesson4c-loaders
    // FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity(),mForecastAdapter);
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());

    //  commented in lesson4c-loaders
    //        //Dixit::Fetching Location value from Shared Prefernces
    //        SharedPreferences locPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    //        String location = locPref.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        String location = Utility.getPreferredLocation(getActivity());

        weatherTask.execute(location);
    }

    //Dixit:Commented in 4c Loaders after fixing location changed update, as
    //we have DB now so no need to fecth n/w data continuoualsy.
//    @Override
//    //Dixit::Calling UpdateWeather in OnStart so that when Application Start it fetches the Latest Data & Display
//    public void onStart() {
//        super.onStart();
//        updateWeather();
//    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

//    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>
//    {
//        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
//        /* The date/time conversion code is going to be moved outside the asynctask later,
//        * so for convenience we're breaking it out into its own method now.
//        */
//        private String getReadableDateString(long time){
//            // Because the API returns a unix timestamp (measured in seconds),
//            // it must be converted to milliseconds in order to be converted to valid date.
//            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
//            return shortenedDateFormat.format(time);
//        }
//
//        /**
//         * Prepare the weather high/lows for presentation.
//         */
//        private String formatHighLows(double high, double low, String unitType) {
//
//            if (unitType.equals(getString(R.string.pref_temp_units_imperial)))
//            {
//                high = (high * 1.8) + 32;
//                low = (low * 1.8) + 32;
//            }
//            else if (!unitType.equals(getString(R.string.pref_temp_units_metrics)))
//            {
//                Log.d(LOG_TAG, "Unit type not found: " + unitType);
//            }
//            // For presentation, assume the user doesn't care about tenths of a degree.
//            long roundedHigh = Math.round(high);
//            long roundedLow = Math.round(low);
//
//            String highLowStr = roundedHigh + "/" + roundedLow;
//            return highLowStr;
//        }
//
//        /**
//         * Take the String representing the complete forecast in JSON Format and
//         * pull out the data we need to construct the Strings needed for the wireframes.
//         *
//         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
//         * into an Object hierarchy for us.
//         */
//        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
//                throws JSONException {
//
//            // These are the names of the JSON objects that need to be extracted.
//            final String OWM_LIST = "list";
//            final String OWM_WEATHER = "weather";
//            final String OWM_TEMPERATURE = "temp";
//            final String OWM_MAX = "max";
//            final String OWM_MIN = "min";
//            final String OWM_DESCRIPTION = "main";
//
//            JSONObject forecastJson = new JSONObject(forecastJsonStr);
//            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
//
//            // OWM returns daily forecasts based upon the local time of the city that is being
//            // asked for, which means that we need to know the GMT offset to translate this data
//            // properly.
//
//            // Since this data is also sent in-order and the first day is always the
//            // current day, we're going to take advantage of that to get a nice
//            // normalized UTC date for all of our weather.
//
//            Time dayTime = new Time();
//            dayTime.setToNow();
//
//            // we start at the day returned by local time. Otherwise this is a mess.
//            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
//
//            // now we work exclusively in UTC
//            dayTime = new Time();
//
//            String[] resultStrs = new String[numDays];
//
//
//            //Dixit: Data is fetched in Celsius by default.
//            // If user prefers to see in Fahrenheit, convert the values here.
//            // We do this rather than fetching in Fahrenheit so that the user can
//            // change this option without us having to re-fetch the data once
//            // we start storing the values in a database.
//            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//            String unitType = sharedPrefs.getString(getString(R.string.pref_temp_units_key), getString(R.string.pref_temp_units_metrics));
//
//            for(int i = 0; i < weatherArray.length(); i++) {
//                // For now, using the format "Day, description, hi/low"
//                String day;
//                String description;
//                String highAndLow;
//
//                // Get the JSON object representing the day
//                JSONObject dayForecast = weatherArray.getJSONObject(i);
//
//                // The date/time is returned as a long.  We need to convert that
//                // into something human-readable, since most people won't read "1400356800" as
//                // "this saturday".
//                long dateTime;
//                // Cheating to convert this to UTC time, which is what we want anyhow
//                dateTime = dayTime.setJulianDay(julianStartDay+i);
//                day = getReadableDateString(dateTime);
//
//                // description is in a child array called "weather", which is 1 element long.
//                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
//                description = weatherObject.getString(OWM_DESCRIPTION);
//
//                // Temperatures are in a child object called "temp".  Try not to name variables
//                // "temp" when working with temperature.  It confuses everybody.
//                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
//                double high = temperatureObject.getDouble(OWM_MAX);
//                double low = temperatureObject.getDouble(OWM_MIN);
//
//                highAndLow = formatHighLows(high, low,unitType);
//                resultStrs[i] = day + " - " + description + " - " + highAndLow;
//            }
//
//            for (String s : resultStrs) {
//                Log.v(LOG_TAG, "Forecast entry: " + s);
//            }
//            return resultStrs;
//
//        }
//
//        @Override
//        protected String[] doInBackground(String... params) {
//            if(params.length==0)
//            {
//                return null;
//            }
//            //Dixit: These two need to be declared outside the try/catch
//            // so that they can be closed in the finally block.
//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//
//// Will contain the raw JSON response as a string.
//            String forecastJsonStr = null;
//            try {
//                // Construct the URL for the OpenWeatherMap query
//                // Possible parameters are available at OWM's forecast API page, at
//                // http://openweathermap.org/API#forecast
//                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&APPID=e8f8e55e165af32fafa39ad2c3837fc0");
//
//                String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=";
//                //String cityId= "201204";
//                String mode = "json";
//                String units = "metric";
//                String count = "7";
//                String apiKey = "e8f8e55e165af32fafa39ad2c3837fc0";
//
//                Uri.Builder nUrl = new Uri.Builder();
//                nUrl.scheme("http").authority("api.openweathermap.org");
//                nUrl.appendPath("data");
//                nUrl.appendPath("2.5");
//                nUrl.appendPath("forecast");
//                nUrl.appendPath("daily");
//                nUrl.appendQueryParameter("q", params[0]);
//                nUrl.appendQueryParameter("mode",mode);
//                nUrl.appendQueryParameter("units",units);
//                nUrl.appendQueryParameter("cnt",count);
//                nUrl.appendQueryParameter("APPID",BuildConfig.OPEN_WEATHER_MAP_API_KEY);
//
//                URL url = new URL(nUrl.build().toString());
//
//                Log.v("ForecastFragment","URL: " + url);
//                // Create the request to OpenWeatherMap, and open the connection
//
//                urlConnection = (HttpURLConnection) url.openConnection();
//
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                // Read the input stream into a String
//
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuffer buffer = new StringBuffer();
//                if (inputStream == null) {
//                    // Nothing to do.
//                   // forecastJsonStr = null;
//                    return null;
//                }
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
//                    // But it does make debugging a *lot* easier if you print out the completed
//                    // buffer for debugging.
//                    buffer.append(line + "\n");
//                }
//
//                if (buffer.length() == 0) {
//                    // Stream was empty.  No point in parsing.
//                    forecastJsonStr = null;
//                }
//                forecastJsonStr = buffer.toString();
//
//                Log.v(LOG_TAG,"Forecast JSON String: " + forecastJsonStr);
//
//
//            } catch (IOException e) {
//                Log.e(LOG_TAG, "Error ", e);
//                // If the code didn't successfully get the weather data, there's no point in attempting
//                // to parse it.
//                forecastJsonStr = null;
//            }
//
//            try{
//                //Dixit: to parse(as required) response data from server we call below function
//                if(forecastJsonStr != null)
//                    return getWeatherDataFromJson(forecastJsonStr, 7);
//            }
//            catch (JSONException e) {
//                Log.e(LOG_TAG, e.getMessage(), e);
//                e.printStackTrace();
//            }
//            finally{
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e(LOG_TAG, "Error closing stream", e);
//                    }
//                }
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String[] result) {
//
//           if(result!=null){
//               mForecastAdapter.clear();
//               for(String dayForeCastStr : result ) {
//                   mForecastAdapter.addAll(dayForeCastStr);
//               }
//           }
//            else
//               Toast.makeText(getActivity(),"Please check Network Connection",Toast.LENGTH_SHORT).show();
//
//        }
//    }

}
