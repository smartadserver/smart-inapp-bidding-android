package com.smartadserver.android.amazonhbsample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.DTBAdCallback;
import com.amazon.device.ads.DTBAdRequest;
import com.amazon.device.ads.DTBAdResponse;
import com.amazon.device.ads.DTBAdSize;
import com.smartadserver.android.library.SASInterstitialView;
import com.smartadserver.android.library.headerbidding.SASAmazonBidderAdapter;
import com.smartadserver.android.library.headerbidding.SASAmazonBidderConfigManager;
import com.smartadserver.android.library.model.SASAdElement;
import com.smartadserver.android.library.ui.SASAdView;
import com.smartadserver.android.library.ui.SASRotatingImageLoader;

import java.util.HashMap;
import java.util.Map;

public class HeaderBiddingInterstitialActivity extends AppCompatActivity {

    /*****************************************
     * Ad Constants
     *****************************************/
    private final static String BASEURL = "https://mobile.smartadserver.com";
    private final static int SITE_ID = 104808;
    private final static String PAGE_ID = "936821";
    private final static int FORMAT_ID = 15140;
    private final static String TARGET = "";

    // Amazon HB configuration
    private static final String AMAZON_APP_KEY = "4852afca9a904e46a680b34b7f0aab8f";
    private static final String AMAZON_INTERSTITIAL_SLOT_ID = "3b7de139-bc75-4502-a9c7-69b496f3be90";

    private static final boolean AMAZON_LOGGING_ENABLED = true;
    private static final boolean AMAZON_TESTING_ENABLED = true;

    /*****************************************
     * Members declarations
     *****************************************/
    // Interstitial view (this view is not part of any xml layout file)
    SASInterstitialView mInterstitialView;

    // Handler classe to be notified of ad loading outcome
    SASAdView.AdResponseHandler interstitialResponseHandler;

    // Button declared in main.xml
    Button mDisplayInterstitialButton;

    // Table containing actual price points (CPM) for Amazon ad formats
    Map<String, Double> amazonPricePointTable;

    /**
     * performs Activity initialization after creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial_hb);

        /*****************************************
         * Now perform Ad related code here
         *****************************************/
        // Enable output to Android Logcat (optional)
        SASAdView.enableLogging();

        // Enable debugging in Webview if available (optional)
        WebView.setWebContentsDebuggingEnabled(true);

        SASAdView.setBaseUrl(BASEURL);

        // Initialize SASInterstitialView
        initInterstitialView();

        // Create button to manually refresh interstitial
        mDisplayInterstitialButton = (Button)this.findViewById(R.id.loadAd);
        mDisplayInterstitialButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loadInterstitialAd();
            }
        });

    }

    /**
     * Overriden to clean up SASAdView instances. This must be done to avoid IntentReceiver leak.
     */
    @Override
    protected void onDestroy() {
        mInterstitialView.onDestroy();
        super.onDestroy();
    }


    /**
     * initialize the SASInterstitialView instance of this Activity
     */
    private void initInterstitialView() {

        // Create SASInterstitialView instance
        mInterstitialView = new SASInterstitialView(this);
        mInterstitialView.setBackgroundColor(Color.WHITE);

        // Add a loader view on the interstitial view. This view is displayed fullscreen, to indicate progress,
        // whenever the interstitial is loading an ad.
        View loader = new SASRotatingImageLoader(this);
        loader.setBackgroundColor(Color.WHITE);
        mInterstitialView.setLoaderView(loader);

        // Add a state change listener on the SASInterstitialView instance to monitor MRAID states changes.
        // Useful for instance to perform some actions as soon as the interstitial disappears.
        mInterstitialView.addStateChangeListener(new SASAdView.OnStateChangeListener() {
            public void onStateChanged(SASAdView.StateChangeEvent stateChangeEvent) {
                switch(stateChangeEvent.getType()) {
                    case SASAdView.StateChangeEvent.VIEW_DEFAULT:
                        // the MRAID Ad View is in default state
                        Log.i("Sample", "Interstitial MRAID state : DEFAULT");
                        break;
                    case SASAdView.StateChangeEvent.VIEW_EXPANDED:
                        // the MRAID Ad View is in expanded state
                        Log.i("Sample", "Interstitial MRAID state : EXPANDED");
                        break;
                    case SASAdView.StateChangeEvent.VIEW_HIDDEN:
                        // the MRAID Ad View is in hidden state
                        Log.i("Sample", "Interstitial MRAID state : HIDDEN");
                        break;
                }
            }
        });

        // Instantiate the response handler used when loading an interstitial ad
        interstitialResponseHandler = new SASAdView.AdResponseHandler() {
            public void adLoadingCompleted(SASAdElement adElement) {
                Log.i("Sample", "Interstitial loading completed");
            }

            public void adLoadingFailed(Exception e) {
                Log.i("Sample", "Interstitial loading failed: " + e.getMessage());
            }
        };
    }

    /**
     * Loads an interstitial ad
     */
    private void loadInterstitialAd() {

        // init Amazon required parameters
        AdRegistration.getInstance(AMAZON_APP_KEY, this);
        AdRegistration.useGeoLocation(true);
        AdRegistration.enableLogging(AMAZON_LOGGING_ENABLED);
        AdRegistration.enableTesting(AMAZON_TESTING_ENABLED);

        // Create an ad size object and pass it to the ad request object
        DTBAdSize adSize = new DTBAdSize.DTBInterstitialAdSize(AMAZON_INTERSTITIAL_SLOT_ID);
        DTBAdRequest adLoader = new DTBAdRequest();
        adLoader.setSizes(adSize);
        adLoader.loadAd(new DTBAdCallback() {

            @Override
            public void onSuccess(DTBAdResponse dtbAdResponse) {
                Log.i("Sample", "Amazon ad request is successful");
                // Amazon returned an ad, wrap it in a SASAmazonBidderAdapter object and pass it to the Smart ad call
                try {
                    // get SASAmazonBidderConfigManager shared instance (url in parameter does not matter here as
                    // it was intialized in MainActivity) and create a SASAmazonBidderAdapter from it
                    SASAmazonBidderAdapter bidderAdapter = SASAmazonBidderConfigManager.getInstance().getBidderAdapter(dtbAdResponse);
                    mInterstitialView.loadAd(SITE_ID, PAGE_ID, FORMAT_ID, true, TARGET, interstitialResponseHandler, bidderAdapter);
                } catch (SASAmazonBidderConfigManager.ConfigurationException ex) {
                    Log.e("Sample", "Amazon bidder can't be created :" + ex.getMessage());
                    // fallback: Smart call without Amazon header bidding
                    mInterstitialView.loadAd(SITE_ID, PAGE_ID, FORMAT_ID, true, TARGET, interstitialResponseHandler, null);
                }
            }

            @Override
            public void onFailure(AdError adError) {
                Log.e("Sample", "Amazon ad request failed with error: " + adError.getMessage());
                // fallback: Smart call without Amazon header bidding
                mInterstitialView.loadAd(SITE_ID, PAGE_ID, FORMAT_ID, true, TARGET, interstitialResponseHandler, null);
            }
        });
    }

}
