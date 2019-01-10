package com.smartadserver.android.amazonhbsample;

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
import com.smartadserver.android.library.ui.SASBannerView;
import com.smartadserver.android.library.ui.SASRotatingImageLoader;

import java.util.Map;

/**
 * Simple activity featuring a banner with Amazon Header Bidding integration
 * <p>
 * To keep things simple, the Amazon price table is filled with AMAZON_FAKE_PRICE CPM value for all price points (see #initAmazonPriceTable())
 * You can tweak this price to values above or below 0.5, which is the floor value set for the Smart competing ad.
 */

public class HeaderBiddingBannerActivity extends AppCompatActivity {

    /*****************************************
     * Ad Constants
     *****************************************/
    private final static int SITE_ID = 104808;
    private final static String PAGE_ID = "936820";
    private final static int FORMAT_ID = 15140;
    private final static String TARGET = "";

    // Amazon HB configuration
    private static final String AMAZON_APP_KEY = "4852afca9a904e46a680b34b7f0aab8f";
    private static final String AMAZON_BANNER_SLOT_ID = "591e251f-3854-4777-89bb-d545fb71e341";

    private static final boolean AMAZON_LOGGING_ENABLED = true;
    private static final boolean AMAZON_TESTING_ENABLED = true;


    /*****************************************
     * Members declarations
     *****************************************/
    // Banner view (as declared in the activity_banner_hb.xml layout file, in res/layout)
    SASBannerView bannerView;

    // Button declared in main.xml
    Button refreshBannerButton;

    // Table containing actual price points  (CPM) for Amazon ad formats
    Map<String, Double> amazonPricePointTable;


    /**
     * performs Activity initialization after creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_hb);

        //Set Title
        setTitle(R.string.title_activity_banner_hb);

        /*****************************************
         * now perform Ad related code here
         *****************************************/

        // Enable debugging in Webview if available (optional)
        WebView.setWebContentsDebuggingEnabled(true);

        // Initialize SASBannerView
        initBannerView();

        // Create button to manually refresh the ad
        refreshBannerButton = this.findViewById(R.id.reloadButton);
        refreshBannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadBannerAd();
            }
        });

        // Load Banner ad
        loadBannerAd();
    }

    /**
     * Overriden to clean up SASAdView instances. This must be done to avoid IntentReceiver leak.
     */
    @Override
    protected void onDestroy() {
        bannerView.onDestroy();
        super.onDestroy();
    }

    /**
     * Initialize the SASBannerView instance of this Activity
     */
    private void initBannerView() {
        // Fetch the SASBannerView inflated from the main.xml layout file
        bannerView = (SASBannerView) this.findViewById(R.id.header_bidding_banner);

        // Add a loader view on the banner. This view covers the banner placement, to indicate progress, whenever the banner is loading an ad.
        // This is optional
        View loader = new SASRotatingImageLoader(this);
        loader.setBackgroundColor(getResources().getColor(R.color.colorLoaderBackground));
        bannerView.setLoaderView(loader);

        // Set the listener used when load an ad on the banner
        bannerView.setBannerListener(new SASBannerView.BannerListener() {
            @Override
            public void onBannerAdLoaded(SASBannerView sasBannerView, SASAdElement sasAdElement) {
                Log.i("Sample", "Banner loading completed.");
            }

            @Override
            public void onBannerAdFailedToLoad(SASBannerView sasBannerView, Exception e) {
                Log.i("Sample", "Banner loading failed: " + e.getMessage());
            }

            @Override
            public void onBannerAdClicked(SASBannerView sasBannerView) {
                Log.i("Sample", "Banner clicked.");
            }

            @Override
            public void onBannerAdExpanded(SASBannerView sasBannerView) {
                Log.i("Sample", "Banner expanded.");
            }

            @Override
            public void onBannerAdCollapsed(SASBannerView sasBannerView) {
                Log.i("Sample", "Banner collapsed.");
            }

            @Override
            public void onBannerAdResized(SASBannerView sasBannerView) {
                Log.i("Sample", "Banner resized.");
            }

            @Override
            public void onBannerAdClosed(SASBannerView sasBannerView) {
                Log.i("Sample", "Banner closed.");
            }

            @Override
            public void onBannerAdVideoEvent(SASBannerView sasBannerView, int i) {
                Log.i("Sample", "Banner video event: " + i);
            }
        });
    }

    /**
     * Loads an ad on the banner
     */
    private void loadBannerAd() {

        // init Amazon required parameters
        AdRegistration.getInstance(AMAZON_APP_KEY, this);
        AdRegistration.useGeoLocation(true);
        AdRegistration.enableLogging(AMAZON_LOGGING_ENABLED);
        AdRegistration.enableTesting(AMAZON_TESTING_ENABLED);

        // Create Smart ad placement
        final SASAdPlacement adPlacement = new SASAdPlacement(SITE_ID, PAGE_ID, FORMAT_ID, TARGET);

        // Create an ad size object and pass it to the ad request object
        DTBAdSize adSize = new DTBAdSize(300, 250, AMAZON_BANNER_SLOT_ID);
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
                    bannerView.loadAd(adPlacement, bidderAdapter);
                } catch (SASAmazonBidderConfigManager.ConfigurationException ex) {
                    Log.e("Sample", "Amazon bidder can't be created :" + ex.getMessage());
                    bannerView.loadAd(adPlacement);
                }
            }

            @Override
            public void onFailure(AdError adError) {
                Log.e("Sample", "Amazon ad request failed with error: " + adError.getMessage());
                // fallback: Smart call without Amazon header bidding
                bannerView.loadAd(adPlacement);
            }
        });
    }
}
