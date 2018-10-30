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

    // tag for logging purposes
    private static final String TAG = SASAmazonBidderAdapter.class.getSimpleName();

    // the name of the winning SSP
    private String winningSSPName;

    // the winning creative ID
    private String winningCreativeId;

    // the winning deal ID
    private String dealId;

    // the winning creative CPM
    private double price;


    // ad view width & height
    private String adViewWidth = "";
    private String adViewHeight = "";

    // amazon creative variables
    private String amzn_h = "";
    private String amzn_b = "";
    private String amznslots = "";


    /**
     * Creates a {@link SASAmazonBidderAdapter} from Amazon ad response
     */
    /*package*/ SASAmazonBidderAdapter(DTBAdResponse adResponse) throws IllegalArgumentException {

        // If no ads in the adResponse, return nil, there is no winning ad.
        if (adResponse.getAdCount() == 0) {
            throw new IllegalArgumentException(" no ad in Amazon response");
        }

        // store winning creative attributes
        this.winningSSPName = "Amazon"; // Using 'Amazon' as SSP name since Amazon will never return the real name of the winning ssp
        this.winningCreativeId = adResponse.getBidId(); // TODO deprecated but no info on what we are supposed to use instead :/
        this.dealId = null;

        // retrievd the ad size of the first ad in Amazon's response
        DTBAdSize adSize = adResponse.getDTBAds().get(0);

        // Get associated pricepoint
        String pricePoint = adResponse.getPricePoints(adSize);

        //get price (if any) associated with pricepoint from SASAmazonBidderConfigManager
        Double convertedCPM = SASAmazonBidderConfigManager.getInstance().getPriceForPricePoint(pricePoint);
        if (convertedCPM == null) {
            throw new IllegalArgumentException(" no CPM found for selected Amazon ad");
        }
        this.price = convertedCPM;

        // extract and store Amazon ad view width and height
        if (adSize != null) {
            if (adSize.getWidth() >= 9999 && adSize.getHeight() >= 9999) { // Interstitial
                this.adViewWidth = "100%";
                this.adViewHeight = "auto";
            } else { // Banner
                this.adViewWidth = adSize.getWidth() + "px";
                this.adViewHeight = adSize.getHeight() + "px";
            }
        }

        // extract and store Amazon custom parameters
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
        return SASAmazonBidderConfigManager.getInstance().getCurrencyCode();
    }

    @Nullable
    @Override
    public String getDealId() {
        return dealId;
    }

    @Nullable
    @Override
    public String getBidderWinningAdMarkup() {
        String creativeTemplate = SASAmazonBidderConfigManager.getInstance().getCreativeTag();
        return creativeTemplate.replace("%%KEYWORD:adWidth%%", adViewWidth)
                .replace("%%KEYWORD:adWidth%%", adViewWidth)
                .replace("%%KEYWORD:adHeight%%", adViewHeight)
                .replace("%%KEYWORD:amzn_b%%", amzn_b)
                .replace("%%KEYWORD:amzn_h%%", amzn_h)
                .replace("%%KEYWORD:amznslots%%", amznslots);
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
