package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity implements ForecastFragment.ListItemClickCallback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    //Dixit: commented in lesson-5:39 (2 Pane UI)
//    private final String FORECASTFRAGMENT_TAG = "FFTAG";

    //Dixit: added in Lesson-5.39(2 Pane Ui), it will be used in onResume
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;

    private String mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mLocation = Utility.getPreferredLocation(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Dixit: commented in lesson-5:39 (2 Pane UI), as the fragment is now statically added in the activity_main.xml file
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.container,new ForecastFragment(),FORECASTFRAGMENT_TAG)
//                    .commit();
//        }
       // Dixit: added in Lesson-5.39(2 Pane Ui)
        if(findViewById(R.id.weather_detail_container) != null){

            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane =true;

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                //Dixit-imp: we check if the saved instance state is null, coz if we rotate the phone,
                // the system saves the fragment state in the saved state bundle and is smart enough to restore this state.
                //Therefore, if the saved state bundle is not null, the system already has the fragment it needs and you
                // shouldn’t go adding another one.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailActivity.DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Dixit: Added in 4c for updating screen with new Location
        String loc = Utility.getPreferredLocation(this);
        if(loc != null && !loc.equals(mLocation) )
        {
            //Dixit: change in lesson-5.39(2 Pane UI)
            // ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if(ff != null)
                ff.onLocationChanged();

            //Dixit:start:Added in lesson-5.40(2 Pane Ui)-Handling List Item Click
            //As the DetailFragment is a dynamically create one, we need to make sure we find the right instance in the
            // fragmnat by manger, we do this by find it by TAG, this TAG was created when we first added to the screen i.e
            // in MainActivity onCreate()
            DetailActivity.DetailFragment df = (DetailActivity.DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if(df != null){
                df.onLocationChanged(loc);
            }
            //Dixit:End
            mLocation = loc;
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingAct = new Intent(this,SettingsActivity.class);
             startActivity(settingAct);

            return true;
        }

        if (id == R.id.action_locate) {
            //Commneted in Lesson 4c
            ////Dixit::Fetching Location value from Shared Prefernces
            //SharedPreferences locPref = PreferenceManager.getDefaultSharedPreferences(this);
            //String location = locPref.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
            String location = Utility.getPreferredLocation(this);

            Uri geoUri = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q",location).build();

            //Dixit:creating intent for showing the preferred location on MAP
            Intent locateAct = new Intent(Intent.ACTION_VIEW);
            locateAct.setData(geoUri);

            //Dixit this is for graceful exit if no app is there to handle this intent
            if (locateAct.resolveActivity(getPackageManager()) != null)
            {
                startActivity(locateAct);
            }
            else {
                Log.d(LOG_TAG, "Couldn't locate " + location + ", no receiving apps installed!");
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Dixit:start:Added in lesson-5.40(2 Pane Ui)-Handling List Item Click
    //Callback to notify the Activity that the List item is selected
    @Override
    public void onItemSelected(Uri dateUri) {
        if(mTwoPane){

            //on tablet, means, it will launch DetailFragment

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailActivity.DetailFragment.DETAIL_URI,dateUri);
            //Dixit-imp:Saving the clicked Uri as Bundle argument & setting it on to fragment
            //so that if the device  is rotated or activity starts again, it will
            //restore the clickuri/index form that point itself, as its in Bundle
            DetailActivity.DetailFragment fragment = new DetailActivity.DetailFragment();
            //now the bundled arguments are set & passed on by call to empty fragmant constructor
            //& once teh fragmen tis intialised we can't chnage teh arguments , we can only read form them(getArguments())
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment,DETAILFRAGMENT_TAG)
                    .commit();
        }
        else{
            //on phone, means, it will launch DetailActivity
            Intent detailAct = new Intent(this, DetailActivity.class);
                    detailAct.setData(dateUri);
            startActivity(detailAct);
        }

    }
    //Dixit:End
}
