package com.smartadserver.android.amazonhbsample;

import android.content.res.Resources;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.content.Intent;

import com.amazon.device.ads.AdRegistration;
import com.smartadserver.android.library.util.SASConfiguration;

public class MainActivity extends AppCompatActivity {

    public static final String AMAZON_APP_KEY = "a9_onboarding_app_id";
    public static final boolean AMAZON_LOGGING_ENABLED = true;
    public static final boolean AMAZON_TESTING_ENABLED = true;
    public static final boolean AMAZON_GEOLOCATION_ENABLED = true;

    static private final int SITE_ID = 1337; // Your SITE_ID

    private ListView mListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // init Amazon required parameters
        AdRegistration.getInstance(AMAZON_APP_KEY, this);
        AdRegistration.useGeoLocation(AMAZON_GEOLOCATION_ENABLED);
        AdRegistration.enableLogging(AMAZON_LOGGING_ENABLED);
        AdRegistration.enableTesting(AMAZON_TESTING_ENABLED);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // First of all, configure the SDK
        try {
            SASConfiguration.getSharedInstance().configure(this, SITE_ID);
        } catch (SASConfiguration.ConfigurationException e) {
            Log.w("Sample", "Smart SDK configuration failed: " + e.getMessage());
        }

        // Enable output to Android Logcat (optional)
        SASConfiguration.getSharedInstance().setLoggingEnabled(true);

        //Set Title
        setTitle(R.string.title_activity_main);

        //Prepare listView
        mListView = createListView();

        //Setup clickListener
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        startHeaderBiddingBannerActivity();
                        break;
                    case 2:
                        startHeaderBiddingInterstitialActivity();
                        break;
                    default:
                        break;
                }
            }

        });

        /**
         * GDPR Consent String manual setting.
         *
         * By uncommenting the following code, you will set the GDPR consent string manually.
         * Note: the Smart Display SDK will use retrieve the consent string from the SharedPreferences using the official IAB key "IABConsent_ConsentString".
         * If using the SmartCMP SDK, you will not have to do this because the SmartCMP already stores the consent string
         * using the official key.
         * If you are using any other CMP that do not store the consent string in the SharedPreferences using the official
         * IAB key, please store it yourself with the official key.
         */
        // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // SharedPreferences.Editor editor = prefs.edit();
        // editor.putString(SASConstants.IABCONSENT_CONSENT_STRING, "YourConsentString");
        // editor.apply();
    }

    //////////////////////////
    // ListView Init
    //////////////////////////

    private ListView createListView() {

        //Create data to display
        Resources res = getResources();
        String[] itemList = res.getStringArray(R.array.activity_main_implementations_array);

        //Find listView and set Adapter
        ListView listView = (ListView) findViewById(R.id.list_view);

        //Create adapter
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, itemList);

        //Create header and footer
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.activity_main_header, mListView, false);
        ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.activity_main_footer, mListView, false);

        //Add Header and footer
        listView.addHeaderView(header, null, false);
        listView.addFooterView(footer, null, false);

        //Set ListView Adapter
        listView.setAdapter(adapter);

        return listView;
    }

    //////////////////////////
    // Starting Activities
    //////////////////////////

    private void startHeaderBiddingBannerActivity() {
        Intent intent = new Intent(this, HeaderBiddingBannerActivity.class);
        startActivity(intent);
    }

    private void startHeaderBiddingInterstitialActivity() {
        Intent intent = new Intent(this, HeaderBiddingInterstitialActivity.class);
        startActivity(intent);
    }

}
