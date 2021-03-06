package com.smartadserver.android.library.thirdpartybidding.amazon;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.amazon.device.ads.DTBAdBannerListener;
import com.amazon.device.ads.DTBAdResponse;
import com.amazon.device.ads.DTBAdSize;
import com.amazon.device.ads.DTBAdView;
import com.smartadserver.android.coresdk.util.SCSUtil;
import com.smartadserver.android.library.thirdpartybidding.SASBannerBidderAdapter;
import com.smartadserver.android.library.thirdpartybidding.SASBannerBidderAdapterListener;
import com.smartadserver.android.library.util.SASUtil;

/**
 * This class is an implementation of the {@link SASBannerBidderAdapter} interface for the Amazon Mobile Match Buy SDK
 * It extends the bas class {@link SASAmazonBaseBidderAdapter} in charge of processing the header bidding request
 * and add support for rendering of banner ads (exclusively)
 * Source : https://github.com/smartadserver/smart-inapp-bidding-android
 */
public class SASAmazonBannerBidderAdapter extends SASAmazonBaseBidderAdapter implements SASBannerBidderAdapter {

    // tag for logging purposes
    private static final String TAG = SASAmazonBannerBidderAdapter.class.getSimpleName();

    // Amazon banner AdView
    DTBAdView amazonBannerView = null;

    // banner parent  view group for proper banner sizing
    LinearLayout linearLayout = null;

    // callback to notify Smart SDK of events generated by Amazon banner
    SASBannerBidderAdapterListener bannerBidderAdapterListener;

    /**
     * Creates a {@link SASAmazonBannerBidderAdapter} from Amazon ad response
     */
    public SASAmazonBannerBidderAdapter(DTBAdResponse adResponse, Context context) throws IllegalArgumentException {
        super(adResponse);


        DTBAdSize adSize = null;
        try {
            adSize = adResponse.getDTBAds().get(0);
        } catch (Exception e) {
            // if there is not at least one DTBAdSize for any reason (empty list, null list..)
        }

        // no ad : abort
        if (adSize == null) {
            throw new IllegalArgumentException("No ad size found for Amazon's banner, we will not render the ad.");
        }

        // Create Amazon banner listener than will forward events to the SASBannerBidderAdapterListener instance
        DTBAdBannerListener bannerListener = new DTBAdBannerListener() {

            @Override
            public void onAdLoaded(View view) {
                if (bannerBidderAdapterListener != null) {
                    // pass the linearLayout containing the Amazon banner to the smart SDK
                    bannerBidderAdapterListener.onBannerLoaded(linearLayout);
                }
            }

            @Override
            public void onAdFailed(View view) {
                if (bannerBidderAdapterListener != null) {
                    bannerBidderAdapterListener.adRequestFailed("Amazon bidder banner ad could not be displayed", false);
                }
            }

            @Override
            public void onAdClicked(View view) {
                // ad click to open already handled in onAdOpen, discard any other click
            }

            @Override
            public void onAdLeftApplication(View view) {
                if (bannerBidderAdapterListener != null) {
                    bannerBidderAdapterListener.onAdLeftApplication();
                }
            }

            @Override
            public void onAdOpen(View view) {
                if (bannerBidderAdapterListener != null) {
                    // onAdOpen corresponds to the onAdClicked in the smart SDK
                    bannerBidderAdapterListener.onAdClicked();
                }
            }

            @Override
            public void onAdClosed(View view) {
                if (bannerBidderAdapterListener != null) {
                    bannerBidderAdapterListener.onAdClosed();
                }
            }

            @Override
            public void onImpressionFired(View view) {
                Log.i(TAG, "Amazon bidder banner ad impression fired");
            }
        };

        // create Amazon banner ad view...
        amazonBannerView = new DTBAdView(context, bannerListener);

        // ...and wrap it in the parent Layout with proper size as fetched above
        linearLayout = new LinearLayout(context);
        linearLayout.addView(amazonBannerView,
                new LinearLayout.LayoutParams(SASUtil.getDimensionInPixels(adSize.getWidth(), context.getResources()),
                        SASUtil.getDimensionInPixels(adSize.getHeight(), context.getResources())));

    }


    /**
     * Implementation of the method of the {@link SASBannerBidderAdapter} methods in charge of
     * rendering the Amazon banner ad fetched by this banner bidder adapter
     */
    @Override
    public void loadBidderBanner(SASBannerBidderAdapterListener bannerAdapterListener) {

        // store the SASBannerBidderAdapterListener passed by smart SDK for event forwarding
        this.bannerBidderAdapterListener = bannerAdapterListener;

        SCSUtil.getMainLooperHandler().post(new Runnable() {
            @Override
            public void run() {
                // load creative from bid
                amazonBannerView.fetchAd(bidInfos);
            }
        });


    }
}
