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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

public class DetailActivity extends ActionBarActivity {

    private ShareActionProvider mShareActionProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //Dixit: lesson-5.39(2 Pane Ui), updating the detail container id is, this is solely for the one
        // pane case, since in the two pane case, you won’t have a DetailActivity, just a MainActivity
        // with a DetailFragment inside of it.
        if (savedInstanceState == null) {

            //Dixit:start added in lesson-5.40(2 Pane Ui)-Handling List Item Click

            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());

            DetailFragment fragment = new DetailFragment();

            fragment.setArguments(arguments);
            //Dixit:end

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);

        //Dixit: Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Dixit: Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        //Dixit: If onLoadFinished happens before this, we can go ahead & set the Share Intent
        if (mShareActionProvider != null ) {
                Intent shareAct = new Intent(Intent.ACTION_SEND);
                //Dixit: added below flag so to prevent the activity we r sharing to
                //from being placed on to activity stack
                shareAct.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                shareAct.setType("text/plain");
                TextView weatherText = (TextView) findViewById(R.id.textView);
                String wetaherMsg = weatherText.getText().toString();
                shareAct.putExtra(Intent.EXTRA_TEXT, wetaherMsg+" #Sunshine");
                mShareActionProvider.setShareIntent(shareAct);
            }
        else {
            Log.d("DetailActivity", "Share Action Provider is null?");
            }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent detailAct = new Intent(this,SettingsActivity.class);
            startActivity(detailAct);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        //Dixit: added in lesson-5.40(2 Pane Ui)-Handling List Item Click
        static final String DETAIL_URI = "URI";
        private Uri mUri;

        private String mForecast;

        private static final int DETAIL_FORECAST_LOADER = 0;

        private static final String[] DETAIL_FORECAST_COLUMNS = {
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATE,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
        };

        // these constants correspond to the projection defined above, and must change if the
        // projection changes
        private static final int COL_WEATHER_ID = 0;
        private static final int COL_WEATHER_DATE = 1;
        private static final int COL_WEATHER_DESC = 2;
        private static final int COL_WEATHER_MAX_TEMP = 3;
        private static final int COL_WEATHER_MIN_TEMP = 4;

        public DetailFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            //Dixit-imp:start: added in lesson-5.40(2 Pane Ui)-Handling List Item Click
            //Reading the saved bundle arguments i.e clicked uri/item, if the activity was killed/started
            Bundle args = getArguments();
            if (args!= null) {
               mUri = args.getParcelable(DetailFragment.DETAIL_URI);
             }
            //Dixit:end

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            return rootView;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            getLoaderManager().initLoader(DETAIL_FORECAST_LOADER, null, this);

            super.onActivityCreated(savedInstanceState);
        }

        //Dixit:start:Added in lesson-5.40(2 Pane Ui)-Handling List Item Click
        void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
            Uri uri = mUri;
            if (null != uri) {
               long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
                Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
                mUri = updatedUri;
                getLoaderManager().restartLoader(DETAIL_FORECAST_LOADER, null, this);
               }
            }
        //Dixit:end

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            Log.v(LOG_TAG, "In onCreateLoader");

            //Dixit-imp:start:Added in lesson-5.40(2 Pane Ui)-Handling List Item Click
            //so as to remove belwo code which relied upon the incoming intent & switch to
            //mUri instead. As weather this DetailFragment is in Main or Detail Activity, this
           // mUri should be set

//            //Dixit: DetailActivity called via intent we sent from ForecatFragement onClicking the weather data
//            Intent intent = getActivity().getIntent();
//
//            //Dixit: update din lesson-5.39(2 Pane UI), If DetailFragment is created without a uri (as in intent.data() == null),
//            // it should not try to create a loader.
//            if(intent == null || intent.getData() == null) {
//                return null;
//            }


            if(mUri != null)
            {
                // Now create and return a CursorLoader that will take care of
                // creating a Cursor for the data being displayed.
                return new CursorLoader(getActivity(),
                        mUri,
                        DETAIL_FORECAST_COLUMNS,
                        null,
                        null,
                        null);

            }
            return null;
            //Dixit:end
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

            Log.v(LOG_TAG, "In onLoadFinished");
            if (!cursor.moveToFirst()) { return; }

            String dateString = Utility.formatDate(cursor.getLong(COL_WEATHER_DATE));

            String weatherDescription = cursor.getString(COL_WEATHER_DESC);

            boolean isMetric = Utility.isMetric(getActivity());

            String high = Utility.formatTemperature(cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);

            String low = Utility.formatTemperature(cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

            mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

            TextView detailTextView = (TextView)getView().findViewById(R.id.textView);

            detailTextView.setText(mForecast);

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}