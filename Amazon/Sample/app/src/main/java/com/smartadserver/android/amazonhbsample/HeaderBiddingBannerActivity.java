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
import com.smartadserver.android.library.thirdpartybidding.amazon.SASAmazonBannerBidderAdapter;
import com.smartadserver.android.library.ui.SASBannerView;
import com.smartadserver.android.library.ui.SASRotatingImageLoader;

/**
 * Simple activity featuring a banner with Amazon Header Bidding integration
 * <p>
 * To keep things simple, the Amazon price table is filled with AMAZON_FAKE_PRICE CPM value for all price points (see #initAmazonPriceTable())
 * You can tweak this price to values above or below 0.5, which is the floor value set for the Smart competing ad.
 */

public class HeaderBiddingBannerActivity extends AppCompatActivity {

    private static final String TAG = HeaderBiddingBannerActivity.class.getSimpleName();

    /*****************************************
     * Ad Constants
     *****************************************/

    private final static int SITE_ID = 351387;
    private final static String PAGE_ID = "1231281";
    private final static int FORMAT_ID = 90738;
    private final static String TARGET = "";

    // Amazon HB banner slot ID
    public static final String AMAZON_BANNER_SLOT_ID = "b9cdd7a6-b2f4-4af9-b77d-1008aa1ea9d4";

    /*****************************************
     * Members declarations
     *****************************************/
    // Banner view (as declared in the activity_banner_hb.xml layout file, in res/layout)
    SASBannerView bannerView;

    // Button declared in main.xml
    Button refreshBannerButton;


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
                Log.i(TAG, "Banner loading completed.");
            }

            @Override
            public void onBannerAdFailedToLoad(SASBannerView sasBannerView, Exception e) {
                Log.i(TAG, "Banner loading failed: " + e.getMessage());
            }

            @Override
            public void onBannerAdClicked(SASBannerView sasBannerView) {
                Log.i(TAG, "Banner clicked.");
            }

            @Override
            public void onBannerAdExpanded(SASBannerView sasBannerView) {
                Log.i(TAG, "Banner expanded.");
            }

            @Override
            public void onBannerAdCollapsed(SASBannerView sasBannerView) {
                Log.i(TAG, "Banner collapsed.");
            }

            @Override
            public void onBannerAdResized(SASBannerView sasBannerView) {
                Log.i(TAG, "Banner resized.");
            }

            @Override
            public void onBannerAdClosed(SASBannerView sasBannerView) {
                Log.i(TAG, "Banner closed.");
            }

            @Override
            public void onBannerAdVideoEvent(SASBannerView sasBannerView, int i) {
                Log.i(TAG, "Banner video event: " + i);
            }
        });
    }

    /**
     * Loads an ad on the banner
     */
    private void loadBannerAd() {

        // Create Smart ad placement
        final SASAdPlacement adPlacement = new SASAdPlacement(SITE_ID, PAGE_ID, FORMAT_ID, TARGET);

        // Create an ad size object and pass it to the ad request object
        DTBAdSize adSize = new DTBAdSize(320, 50, AMAZON_BANNER_SLOT_ID);
        DTBAdRequest adLoader = new DTBAdRequest();
        adLoader.setSizes(adSize);
        adLoader.loadAd(new DTBAdCallback() {

            @Override
            public void onSuccess(DTBAdResponse dtbAdResponse) {
                Log.i(TAG, "Amazon ad request is successful");
                // Amazon returned an ad, wrap it in a SASAmazonBannerBidderAdapter object and pass it to the Smart ad call
                SASAmazonBannerBidderAdapter bidderAdapter =
                        new SASAmazonBannerBidderAdapter(dtbAdResponse, HeaderBiddingBannerActivity.this);
                bannerView.loadAd(adPlacement, bidderAdapter);
            }

            @Override
            public void onFailure(AdError adError) {
                Log.e(TAG, "Amazon ad request failed with error: " + adError.getMessage());
                // fallback: Smart call without Amazon header bidding object
                bannerView.loadAd(adPlacement);
            }
        });
    }
}
