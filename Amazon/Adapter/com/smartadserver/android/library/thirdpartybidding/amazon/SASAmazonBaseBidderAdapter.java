package com.smartadserver.android.library.thirdpartybidding.amazon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.amazon.device.ads.DTBAdResponse;
import com.amazon.device.ads.SDKUtilities;
import com.smartadserver.android.library.thirdpartybidding.SASBidderAdapter;

import java.util.List;
import java.util.Map;

/**
 * This class is an abstract implementation of the {@link SASBidderAdapter} interface for the Amazon Mobile Match Buy SDK
 * It basically wraps an Amazon response to a header bidding requests and passes various details about the
 * bidding outcome to the Smart AdServer ad call.
 * Source : https://github.com/smartadserver/smart-inapp-bidding-android
 */
public abstract class SASAmazonBaseBidderAdapter implements SASBidderAdapter {

    // tag for logging purposes
    private static final String TAG = SASAmazonBaseBidderAdapter.class.getSimpleName();

    //////////////////////////////
    // SASBidderAdapter variables
    /////////////////////////////

    // the name of the winning SSP
    private String winningSSPName;

    // the winning creative ID
    private String winningCreativeId;

    // the winning deal ID
    private String dealId;

    // the keyword representing the pricepoint
    private String keyword;

    //////////////////////////////
    // Amazon creative variables
    /////////////////////////////
    private String amzn_h = "";
    private String amzn_b = "";
    private String amznslots = "";

    // BidInfo string used for creative rendering
    protected String bidInfos = "";


    /**
     * Creates a {@link SASAmazonBaseBidderAdapter} from Amazon ad response
     */
    public SASAmazonBaseBidderAdapter(DTBAdResponse adResponse) throws IllegalArgumentException {

        // If no ads in the adResponse, return nil, there is no winning ad.
        if (adResponse.getAdCount() == 0) {
            throw new IllegalArgumentException("No ad found in Amazon's response.");
        }

        // store ad response attributes
        this.winningSSPName = "Amazon"; // Using 'Amazon' as SSP name since Amazon will never return the real name of the winning ssp
        this.winningCreativeId = adResponse.getBidId(); // TODO deprecated but no info on what we are supposed to use instead :/
        this.dealId = null;

        // extract Amazon creative parameters
        Map<String, List<String>> customParams = adResponse.getDefaultDisplayAdsRequestCustomParams();

        // Keyword
        if (customParams.get("amznslots") != null && customParams.get("amznslots").size() > 0) {
            amznslots = customParams.get("amznslots").get(0);
            keyword = "amznslots=" + customParams.get("amznslots").get(0);
        } else {
            throw new IllegalArgumentException("No pricepoint found for Amazon's response.");
        }

        // Store Amazon creative parameters
        if (customParams.get("amzn_h") != null && customParams.get("amzn_h").size() > 0) {
            amzn_h = customParams.get("amzn_h").get(0);
        }
        if (customParams.get("amzn_b") != null && customParams.get("amzn_b").size() > 0) {
            amzn_b = customParams.get("amzn_b").get(0);
        }

        // Bid infos, used for rendering
        if (SDKUtilities.getBidInfo(adResponse) != null) {
            this.bidInfos = SDKUtilities.getBidInfo(adResponse);
        } else {
            throw new IllegalArgumentException("No bid info found for Amazon's response, we will not be able to render the ad.");
        }

    }

    ///////////////////////////////////////////////////////////
    // ADAPTER INFORMATION
    ///////////////////////////////////////////////////////////

    @NonNull
    @Override
    public String getAdapterName() {
        return "Amazon";
    }

    @NonNull
    @Override
    public CompetitionType getCompetitionType() {
        return CompetitionType.Keyword;
    }

    @NonNull
    @Override
    public RenderingType getRenderingType() {
        return RenderingType.Mediation;
    }


    ///////////////////////////////////////////////////////////
    // WINNING CREATIVE INFORMATION
    ///////////////////////////////////////////////////////////

    @NonNull
    @Override
    public String getWinningSSPName() {
        return winningSSPName;
    }

    @NonNull
    @Override
    public String getWinningCreativeId() {
        return winningCreativeId;
    }

    @Override
    public double getPrice() {
        return 0.0;
    }

    @Nullable
    @Override
    public String getCurrency() {
        return null;
    }

    @Nullable
    @Override
    public String getKeyword() {
        return keyword;
    }

    @Nullable
    @Override
    public String getDealId() {
        return dealId;
    }

    ///////////////////////////////////////////////////////////
    // WIN NOTIFICATION CALLBACK
    ///////////////////////////////////////////////////////////

    @Override
    public void primarySDKLostBidCompetition() {
        Log.i(TAG, "primarySDKLostBidCompetition() called");

        // Nothing to do here unless you want to log some information.
    }

    ///////////////////////////////////////////////////////////
    // SMART DISPLAY SDK CREATIVE RENDERING
    ///////////////////////////////////////////////////////////

    @Nullable
    @Override
    public String getBidderWinningAdMarkup() {
        return null;
    }

    @Override
    public void primarySDKDisplayedBidderAd() {
        Log.i(TAG, "primarySDKDisplayedBidderAd() called");

        // Nothing to do here this method will not be called on Mediation rendering type.
    }

    @Override
    public void primarySDKClickedBidderAd() {
        Log.i(TAG, "primarySDKClickedBidderAd() called");

        // Nothing to do here this method will not be called on Mediation rendering type.
    }

    ///////////////////////////////////////////////////////////
    // THIRD PARTY CREATIVE RENDERING
    ///////////////////////////////////////////////////////////

    @Override
    public void primarySDKRequestedThirdPartyRendering() {
        Log.i(TAG, "primarySDKRequestedThirdPartyRendering() called");

        // Nothing to do here this method will not be called on Mediation rendering type.
    }


}
