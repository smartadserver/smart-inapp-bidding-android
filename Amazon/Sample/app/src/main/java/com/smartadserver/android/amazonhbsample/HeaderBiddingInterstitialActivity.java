package com.smartadserver.android.amazonhbsample;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
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
import com.smartadserver.android.library.model.SASAdElement;
import com.smartadserver.android.library.model.SASAdPlacement;
import com.smartadserver.android.library.thirdpartybidding.amazon.SASAmazonInterstitialBidderAdapter;
import com.smartadserver.android.library.ui.SASInterstitialManager;
import com.smartadserver.android.library.util.SASUtil;

public class HeaderBiddingInterstitialActivity extends AppCompatActivity {

    private static final String TAG = HeaderBiddingInterstitialActivity.class.getSimpleName();

    /*****************************************
     * Ad Constants
     *****************************************/
    private final static int SITE_ID = 351387;
    private final static String PAGE_ID = "1231282";
    private final static int FORMAT_ID = 90739;
    private final static String TARGET = "";

    // Amazon HB Interstitial slot ID
    public static final String AMAZON_INTERSTITIAL_SLOT_ID = "6b964bfb-6c2c-4589-a049-23ecaada4f52";

    /*****************************************
     * Members declarations
     *****************************************/
    // Interstitial manager
    SASInterstitialManager interstitialManager;

    // Button declared in main.xml
    Button displayInterstitialButton;

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
                if (interstitialManager.isShowable()){
                    interstitialManager.show();
                } else {
                    displayInterstitialButton.setEnabled(false);
                    loadInterstitialAd();
                }
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
                Log.i(TAG, "Interstitial loading completed.");
                // We display the interstitial as soon as it is loaded.
                setLoadShowButtonText(getString(R.string.activity_interstitial_show_btn));
            }

            @Override
            public void onInterstitialAdFailedToLoad(SASInterstitialManager sasInterstitialManager, Exception e) {
                Log.i(TAG, "Interstitial loading failed: " + e.getMessage());
            }

            @Override
            public void onInterstitialAdShown(SASInterstitialManager sasInterstitialManager) {
                Log.i(TAG, "Interstitial shown.");
            }

            @Override
            public void onInterstitialAdFailedToShow(SASInterstitialManager sasInterstitialManager, Exception e) {
                Log.i(TAG, "Interstitial failed to show: " + e.getMessage());
                setLoadShowButtonText(getString(R.string.activity_interstitial_load_btn));
            }

            @Override
            public void onInterstitialAdClicked(SASInterstitialManager sasInterstitialManager) {
                Log.i(TAG, "Interstitial clicked.");
            }

            @Override
            public void onInterstitialAdDismissed(SASInterstitialManager sasInterstitialManager) {
                Log.i(TAG, "Interstitial dismissed.");
                setLoadShowButtonText(getString(R.string.activity_interstitial_load_btn));
            }

            @Override
            public void onInterstitialAdVideoEvent(SASInterstitialManager sasInterstitialManager, int i) {
                Log.i(TAG, "Interstitial video event: " + i);
            }
        });
    }

    private void setLoadShowButtonText(final String text) {
        SASUtil.getMainLooperHandler().post(new Runnable() {
            @Override
            public void run() {
                displayInterstitialButton.setText(text);
                displayInterstitialButton.setEnabled(true);
            }
        });
    }

    /**
     * Loads an interstitial ad
     */
    private void loadInterstitialAd() {

        // Create an ad size object and pass it to the ad request object
        DTBAdSize adSize = new DTBAdSize.DTBInterstitialAdSize(AMAZON_INTERSTITIAL_SLOT_ID);
        DTBAdRequest adLoader = new DTBAdRequest();
        adLoader.setSizes(adSize);
        adLoader.loadAd(new DTBAdCallback() {

            @Override
            public void onSuccess(DTBAdResponse dtbAdResponse) {
                Log.i(TAG, "Amazon ad request is successful");
                // Amazon returned an ad, wrap it in a SASAmazonBidderAdapter object and pass it to the Smart ad call
                // Amazon returned an ad, wrap it in a SASAmazonBannerBidderAdapter object and pass it to the Smart ad call
                SASAmazonInterstitialBidderAdapter bidderAdapter =
                        new SASAmazonInterstitialBidderAdapter(dtbAdResponse, HeaderBiddingInterstitialActivity.this);
                interstitialManager.loadAd(bidderAdapter);
            }

            @Override
            public void onFailure(AdError adError) {
                Log.e(TAG, "Amazon ad request failed with error: " + adError.getMessage());
                // fallback: Smart call without Amazon header bidding
                interstitialManager.loadAd();
            }
        });
    }
}
