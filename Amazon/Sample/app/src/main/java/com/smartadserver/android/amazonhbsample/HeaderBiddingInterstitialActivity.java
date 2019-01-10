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
import com.smartadserver.android.library.headerbidding.SASAmazonBidderAdapter;
import com.smartadserver.android.library.headerbidding.SASAmazonBidderConfigManager;
import com.smartadserver.android.library.model.SASAdElement;
import com.smartadserver.android.library.model.SASAdPlacement;
import com.smartadserver.android.library.ui.SASInterstitialManager;

import java.util.Map;

public class HeaderBiddingInterstitialActivity extends AppCompatActivity {

    /*****************************************
     * Ad Constants
     *****************************************/
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
    // Interstitial manager
    SASInterstitialManager interstitialManager;

    // Button declared in main.xml
    Button displayInterstitialButton;

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
        // Enable debugging in Webview if available (optional)
        WebView.setWebContentsDebuggingEnabled(true);

        // Initialize SASInterstitialView
        initInterstitialView();

        // Create button to manually refresh interstitial
        displayInterstitialButton = this.findViewById(R.id.loadAd);
        displayInterstitialButton.setOnClickListener(new View.OnClickListener() {
            @Override
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
        interstitialManager.onDestroy();
        super.onDestroy();
    }


    /**
     * initialize the SASInterstitialView instance of this Activity
     */
    private void initInterstitialView() {

        // Create your ad placement
        SASAdPlacement adPlacement = new SASAdPlacement(SITE_ID, PAGE_ID, FORMAT_ID, TARGET);

        // Create the interstitial manager instance
        interstitialManager = new SASInterstitialManager(this, adPlacement);

        // Set the interstitial manager listener.
        // Useful for instance to perform some actions as soon as the interstitial disappears.
        interstitialManager.setInterstitialListener(new SASInterstitialManager.InterstitialListener() {
            @Override
            public void onInterstitialAdLoaded(SASInterstitialManager sasInterstitialManager, SASAdElement sasAdElement) {
                Log.i("Sample", "Interstitial loading completed.");
                // We display the interstitial as soon as it is loaded.
                interstitialManager.show();
            }

            @Override
            public void onInterstitialAdFailedToLoad(SASInterstitialManager sasInterstitialManager, Exception e) {
                Log.i("Sample", "Interstitial loading failed: " + e.getMessage());
            }

            @Override
            public void onInterstitialAdShown(SASInterstitialManager sasInterstitialManager) {
                Log.i("Sample", "Interstitial shown.");
            }

            @Override
            public void onInterstitialAdFailedToShow(SASInterstitialManager sasInterstitialManager, Exception e) {
                Log.i("Sample", "Interstitial failed to show: " + e.getMessage());
            }

            @Override
            public void onInterstitialAdClicked(SASInterstitialManager sasInterstitialManager) {
                Log.i("Sample", "Interstitial clicked.");
            }

            @Override
            public void onInterstitialAdDismissed(SASInterstitialManager sasInterstitialManager) {
                Log.i("Sample", "Interstitial dismissed.");
            }

            @Override
            public void onInterstitialAdVideoEvent(SASInterstitialManager sasInterstitialManager, int i) {
                Log.i("Sample", "Interstitial video event: " + i);
            }
        });
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
                    // it was initialized in MainActivity) and create a SASAmazonBidderAdapter from it
                    SASAmazonBidderAdapter bidderAdapter = SASAmazonBidderConfigManager.getInstance().getBidderAdapter(dtbAdResponse);
                    interstitialManager.loadAd(bidderAdapter);

                } catch (SASAmazonBidderConfigManager.ConfigurationException ex) {
                    Log.e("Sample", "Amazon bidder can't be created :" + ex.getMessage());
                    // fallback: Smart call without Amazon header bidding
                    interstitialManager.loadAd();
                }
            }

            @Override
            public void onFailure(AdError adError) {
                Log.e("Sample", "Amazon ad request failed with error: " + adError.getMessage());
                // fallback: Smart call without Amazon header bidding
                interstitialManager.loadAd();
            }
        });
    }

}
