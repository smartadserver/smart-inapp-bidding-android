package com.smartadserver.android.library.thirdpartybidding.amazon;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.amazon.device.ads.DTBAdInterstitial;
import com.amazon.device.ads.DTBAdInterstitialListener;
import com.amazon.device.ads.DTBAdResponse;
import com.smartadserver.android.coresdk.util.SCSUtil;
import com.smartadserver.android.library.thirdpartybidding.SASBidderAdapter;
import com.smartadserver.android.library.thirdpartybidding.SASInterstitialBidderAdapter;
import com.smartadserver.android.library.thirdpartybidding.SASInterstitialBidderAdapterListener;

/**
 * This class is an implementation of the {@link SASBidderAdapter} interface for the Amazon Mobile Match Buy SDK
 * This class is an implementation of the {@link SASInterstitialBidderAdapter} interface for the Amazon Mobile Match Buy SDK
 * It extends the bas class {@link SASAmazonBaseBidderAdapter} in charge of processing the header bidding request
 * and add support for rendering of interstitial ads (exclusively)
 * Source : https://github.com/smartadserver/smart-inapp-bidding-android
 */
public class SASAmazonInterstitialBidderAdapter extends SASAmazonBaseBidderAdapter implements SASInterstitialBidderAdapter {

    // tag for logging purposes
    private static final String TAG = SASAmazonInterstitialBidderAdapter.class.getSimpleName();

    // Amazon interstitial
    DTBAdInterstitial amazonInterstitial;

    // callback to notify Smart SDK of events generated by Amazon interstitial
    SASInterstitialBidderAdapterListener interstitialBidderAdapterListener;

    /**
     * Creates a {@link SASAmazonInterstitialBidderAdapter} from Amazon ad response
     */
    public SASAmazonInterstitialBidderAdapter(DTBAdResponse adResponse, Context context) throws IllegalArgumentException {
        super(adResponse);

        // Create Amazon banner listener than will forward events to the SASInterstitialBidderAdapterListener instance
        DTBAdInterstitialListener interstitialListener = new DTBAdInterstitialListener() {
            @Override
            public void onAdLoaded(View view) {
                if (interstitialBidderAdapterListener != null) {
                    interstitialBidderAdapterListener.onInterstitialLoaded();
                }
            }

            @Override
            public void onAdFailed(View view) {
                if (interstitialBidderAdapterListener != null) {
                    interstitialBidderAdapterListener.adRequestFailed("Amazon bidder interstitial ad could not be displayed", false);
                }
            }

            @Override
            public void onAdClicked(View view) {
                // ad click to open already handled in onAdLeftApplication, discard any other click
            }

            @Override
            public void onAdLeftApplication(View view) {
                if (interstitialBidderAdapterListener != null) {
                    // consider onAdLeftApplication as the "post click view opened" event
                    interstitialBidderAdapterListener.onAdClicked();
                    interstitialBidderAdapterListener.onAdLeftApplication();
                }
            }

            @Override
            public void onAdOpen(View view) {
                if (interstitialBidderAdapterListener != null) {
                    // onAdOpen corresponds to the interstital shown event in the smart SDK
                    interstitialBidderAdapterListener.onInterstitialShown();
                }
            }

            @Override
            public void onAdClosed(View view) {
                if (interstitialBidderAdapterListener != null) {
                    interstitialBidderAdapterListener.onAdClosed();
                }
            }

            @Override
            public void onImpressionFired(View view) {
                Log.i(TAG, "Amazon bidder interstitial ad impression fired");
            }
        };

        // create Amazon interstitial instance
        amazonInterstitial = new DTBAdInterstitial(context, interstitialListener);
    }

    /**
     * Implementation of the method of the {@link SASInterstitialBidderAdapter} methods in charge of
     * loading the Amazon interstitial ad fetched by this interstitial bidder adapter
     */
    @Override
    public void loadBidderInterstitial(SASInterstitialBidderAdapterListener interstitialBidderAdapterListener) {

        this.interstitialBidderAdapterListener = interstitialBidderAdapterListener;

        SCSUtil.getMainLooperHandler().post(new Runnable() {
            @Override
            public void run() {
                // load creative from bid
                amazonInterstitial.fetchAd(bidInfos);
            }
        });

    }

    /**
     * Implementation of the method of the {@link SASInterstitialBidderAdapter} methods in charge of
     * displaying the Amazon interstitial ad fetched by this interstitial bidder adapter
     */
    @Override
    public void showBidderInterstitial() {

        SCSUtil.getMainLooperHandler().post(new Runnable() {
            @Override
            public void run() {
                amazonInterstitial.show();
            }
        });
    }
}
