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
import com.smartadserver.android.library.SASBannerView;
import com.smartadserver.android.library.headerbidding.SASAmazonBidderAdapter;
import com.smartadserver.android.library.model.SASAdElement;
import com.smartadserver.android.library.ui.SASAdView;
import com.smartadserver.android.library.ui.SASRotatingImageLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple activity featuring a banner with Amazon Header Bidding integration
 *
 * To keep things simple, the Amazon price table is filled with AMAZON_FAKE_PRICE CPM value for all price points (see #initAmazonPriceTable())
 * You can tweak this price to values above or below 0.5, which is the floor value set for the Smart competing ad.
 */

public class HeaderBiddingBannerActivity extends AppCompatActivity {

    /*****************************************
     * Ad Constants
     *****************************************/
    private final static String BASEURL = "http://mobile.smartadserver.com";
    private final static int SITE_ID = 104808;
    private final static String PAGE_ID = "936820";
    private final static int FORMAT_ID = 15140;
    private final static String TARGET = "";

    // Amazon HB configuration
    private static final String AMAZON_APP_KEY = "4852afca9a904e46a680b34b7f0aab8f";
    private static final String AMAZON_BANNER_SLOT_ID = "591e251f-3854-4777-89bb-d545fb71e341";

    private static final double AMAZON_FAKE_PRICE = 0.70;
    private static final boolean AMAZON_LOGGING_ENABLED = true;
    private static final boolean AMAZON_TESTING_ENABLED = true;
    private static final String AMAZON_CURRENCY = "EUR";


    /*****************************************
     * Members declarations
     *****************************************/
    // Banner view (as declared in the activity_banner_hb.xml layout file, in res/layout)
    SASBannerView mBannerView;

    // Handler class to be notified of ad loading outcome
    SASAdView.AdResponseHandler bannerResponseHandler;

    // Button declared in main.xml
    Button mRefreshBannerButton;

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

        // Enable output to Android Logcat (optional)
        SASAdView.enableLogging();

        // Enable debugging in Webview if available (optional)
        WebView.setWebContentsDebuggingEnabled(true);

        SASAdView.setBaseUrl(BASEURL);

        // Initialize SASBannerView
        initBannerView();

        // Create button to manually refresh the ad
        mRefreshBannerButton = (Button)this.findViewById(R.id.reloadButton);
        mRefreshBannerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loadBannerAd();
            }
        });

        // init Amazon price point table
        initAmazonPriceTable();

        // Load Banner ad
        loadBannerAd();

    }

    /**
     * Creates and populates a conversion table between Amazon ad formats and their corresponding mean CPM
     * This is the publisher's responsability to fill this table with the CPMs given by Amazon
     * They will be passed to the Smart AdServer's delivery engine to determine if an ad with a higher CPM can
     * be served or not.
     * Here, we set a fake price for all 300x250 price points for the sake of demonstration.
     *
     */
    private void initAmazonPriceTable() {
        amazonPricePointTable = new HashMap<>();

        amazonPricePointTable.put("t300x250p10", AMAZON_FAKE_PRICE);
        amazonPricePointTable.put("t300x250p20", AMAZON_FAKE_PRICE);
        amazonPricePointTable.put("t300x250p30", AMAZON_FAKE_PRICE);
        amazonPricePointTable.put("t300x250p40", AMAZON_FAKE_PRICE);

    }

    /**
     * Overriden to clean up SASAdView instances. This must be done to avoid IntentReceiver leak.
     */
    @Override
    protected void onDestroy() {
        mBannerView.onDestroy();
        super.onDestroy();
    }

    /**
     * Initialize the SASBannerView instance of this Activity
     */
    private void initBannerView() {
        // Fetch the SASBannerView inflated from the main.xml layout file
        mBannerView = (SASBannerView)this.findViewById(R.id.header_bidding_banner);

        // Add a loader view on the banner. This view covers the banner placement, to indicate progress, whenever the banner is loading an ad.
        // This is optional
        View loader = new SASRotatingImageLoader(this);
        loader.setBackgroundColor(getResources().getColor(R.color.colorLoaderBackground));
        mBannerView.setLoaderView(loader);

        // Instantiate the response handler used when loading an ad on the banner
        bannerResponseHandler = new SASAdView.AdResponseHandler() {
            public void adLoadingCompleted(SASAdElement adElement) {
                Log.i("Sample", "Banner loading completed");
            }

            public void adLoadingFailed(Exception e) {
                Log.i("Sample", "Banner loading failed: " + e.getMessage());
            }
        };
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

        // Create an ad size object and pass it to the ad request object
        DTBAdSize adSize = new DTBAdSize(300, 250, AMAZON_BANNER_SLOT_ID); // TODO don't hardcode this?
        DTBAdRequest adLoader = new DTBAdRequest();
        adLoader.setSizes(adSize);
        adLoader.loadAd(new DTBAdCallback() {

            @Override
            public void onSuccess(DTBAdResponse dtbAdResponse) {
                Log.i("Sample", "Amazon ad request is successful");
                // Amazon returned an ad, wrap it in a SASAmazonBidderAdapter object and pass it to the Smart ad call
                try {
                    SASAmazonBidderAdapter bidderAdapter = new SASAmazonBidderAdapter(dtbAdResponse, amazonPricePointTable, AMAZON_CURRENCY);
                    mBannerView.loadAd(SITE_ID, PAGE_ID, FORMAT_ID, true, TARGET, bannerResponseHandler, bidderAdapter);
                } catch (IllegalArgumentException ex) {
                    Log.e("Sample", "Amazon bidder can't be created :" + ex.getMessage());
                    mBannerView.loadAd(SITE_ID, PAGE_ID, FORMAT_ID, true, TARGET, bannerResponseHandler, null);
                }
            }

            @Override
            public void onFailure(AdError adError) {
                Log.e("Sample", "Amazon ad request failed with error: " + adError.getMessage());
                // fallback: Smart call without Amazon header bidding
                mBannerView.loadAd(SITE_ID, PAGE_ID, FORMAT_ID, true, TARGET, bannerResponseHandler, null);
            }
        });
    }
}
