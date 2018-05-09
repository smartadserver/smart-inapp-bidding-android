package com.smartadserver.android.library.headerbidding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amazon.device.ads.DTBAdResponse;
import com.amazon.device.ads.DTBAdSize;
import com.smartadserver.android.library.headerbidding.SASBidderAdapter;

import java.util.List;
import java.util.Map;

/**
 * This class is an implementation of the {@link SASBidderAdapter} interface for the Amazon Mobile Match Buy SDK
 * It basically wraps an Amazon response to a header bidding requests and passes various details about the
 * bidding outcome to the Smart AdServer ad call.
 * Source : https://github.com/smartadserver/smart-inapp-bidding-android
 */
public class SASAmazonBidderAdapter implements SASBidderAdapter {

    private static final String TAG = SASAmazonBidderAdapter.class.getSimpleName();

    private String winningSSPName;
    private String winningCreativeId;
    private String dealId;
    private double price;
    private String currency;

    private String adViewWidth = "";
    private String adViewHeight = "";

    private String amzn_h = "";
    private String amzn_b = "";
    private String amznslots = "";


    public SASAmazonBidderAdapter(DTBAdResponse adResponse, Map<String, Double> prices, String currency) throws IllegalArgumentException {

        // If no adSizes in the adResponse, return nil, there is no winning ad.
        if (adResponse.getAdCount() == 0) {
            throw new IllegalArgumentException(" no ad in Amazon response");
        }

        this.winningSSPName = "Amazon"; // Using 'Amazon' as SSP name since Amazon will never return the real name of the winning ssp
        this.winningCreativeId = adResponse.getBidId(); // TODO deprecated but no info on what we are supposed to use instead :/
        this.dealId = null;

        DTBAdSize adSize = adResponse.getDTBAds().get(0);
        String pricePoint = adResponse.getPricePoints(adSize);

        Double convertedCPM = prices.get(pricePoint);
        if (convertedCPM == null) {
            throw new IllegalArgumentException(" no CPM found for selected Amazon ad");
        }
        this.price = convertedCPM;
        this.currency = currency;

        if (adSize != null) {
            if (adSize.getWidth() >= 9999 && adSize.getHeight() >= 9999) { // Interstitial
                this.adViewWidth = "100%";
                this.adViewHeight = "auto";
            } else { // Banner
                this.adViewWidth = adSize.getWidth() + "px";
                this.adViewHeight = adSize.getHeight() + "px";
            }
        }

        Map<String, List<String>> customParams = adResponse.getDefaultDisplayAdsRequestCustomParams();
        if (customParams.get("amzn_h") != null && customParams.get("amzn_h").size() > 0) {
            amzn_h = customParams.get("amzn_h").get(0);
        }
        if (customParams.get("amzn_b") != null && customParams.get("amzn_b").size() > 0) {
            amzn_b = customParams.get("amzn_b").get(0);
        }
        if (customParams.get("amznslots") != null && customParams.get("amznslots").size() > 0) {
            amznslots = customParams.get("amznslots").get(0);
        }
    }

    @NonNull
    @Override
    public String getAdapterName() {
        return "SASAmazonBidderAdapter";
    }

    @NonNull
    @Override
    public String getWinningSSPName() {
        return winningSSPName;
    }

    @NonNull
    @Override
    public RenderingType getRenderingType() {
        return RenderingType.PrimarySDK;
    }

    @NonNull
    @Override
    public String getWinningCreativeId() {
        return winningCreativeId;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @NonNull
    @Override
    public String getCurrency() {
        return currency;
    }

    @Nullable
    @Override
    public String getDealId() {
        return dealId;
    }

    @Nullable
    @Override
    public String getBidderWinningAdMarkup() {
        String markup = "<div style=\"display:inline-block\">\r\n    <div id=\"__dtbAd__\" style=\"width:%%PATTERN:adWidth%%; height:%%PATTERN:adHeight%%; overflow:hidden;\">\r\n        <!--Placeholder for the Ad -->\r\n    </div>\r\n    <script type=\"text/javascript\" src=\"mraid.js\"></script>\r\n    <script type=\"text/javascript\" src=\"https://c.amazon-adsystem.com/dtb-m.js\"></script>\r\n    <script type=\"text/javascript\">\r\n        amzn.dtb.loadAd(\"%%PATTERN:amznslots%%\", \"%%PATTERN:amzn_b%%\",\"%%PATTERN:amzn_h%%\");\r\n    </script>\r\n</div>";

        markup = markup.replace("%%PATTERN:adWidth%%", adViewWidth);
        markup = markup.replace("%%PATTERN:adHeight%%", adViewHeight);

        markup = markup.replace("%%PATTERN:amzn_b%%", amzn_b);
        markup = markup.replace("%%PATTERN:amzn_h%%", amzn_h);
        markup = markup.replace("%%PATTERN:amznslots%%", amznslots);

        return markup;
    }

    @Override
    public void primarySDKDisplayedBidderAd() {
        Log.i(TAG, "primarySDKDisplayedBidderAd() called");

        // Nothing to do here unless you want to count impressions on your side
    }

    @Override
    public void primarySDKClickedBidderAd() {
        Log.i(TAG, "primarySDKClickedBidderAd() called");

        // Nothing to do here unless you want to count clicks on your side
    }

    @Override
    public void primarySDKLostBidCompetition() {
        Log.i(TAG, "primarySDKLostBidCompetition() called");

        // Nothing to do here, primary SDK is responsible for creative display
    }
}
