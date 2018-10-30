package com.smartadserver.android.library.headerbidding;

import android.support.annotation.Nullable;
import android.util.Log;

import com.amazon.device.ads.DTBAdResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Class that will fetch an Amazon configuration file and act as a {@link SASAmazonBidderAdapter} factory
 */
public class SASAmazonBidderConfigManager {

    // tag for logging purposes
    private static final String TAG = SASAmazonBidderAdapter.class.getSimpleName();

    // constant parameters keys for configuration file parsing
    private static final String PRICES_CURRENCY_CODE = "currencyCode";
    private static final String CREATIVE_TAG = "creativeTag";
    private static final String PRICE_POINTS = "pricePoints";

    // singleton instance reference
    private static SASAmazonBidderConfigManager instance;


    /**
     * Class describing configuration exception occuring when trying to fetch Amazon bidder configuration
     */
    public static class ConfigurationException extends Exception {

        public ConfigurationException(String message) {
            super(message);
        }

        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }


    // currency code for prices currency
    private String currencyCode;

    // Amazon price point to value Map
    private Map<String,Double> prices = new HashMap<>();

    // the Amazon creative script
    private String creativeTag;

    // internal flags describing config manager state
    private boolean isReady = false;
    private boolean isFetchingConfig = false;

    // last (unrecoverable) ConfigurationException that happened when fetching configuration
    private ConfigurationException configException = null;

    // url to fetch the configuration from
    private String configUrl = null;

    private OkHttpClient okHttpClient;

    /**
     * Returns the shared instance of {@link SASAmazonBidderConfigManager}
     * @return
     */
    public static synchronized SASAmazonBidderConfigManager getInstance() throws IllegalArgumentException {

        // create singleton instance
        if (instance == null) {
            instance = new SASAmazonBidderConfigManager();
        }

        return instance;
    }

    /**
     * Retrieve and parse the configuration JSON at the specified Url
     */
    private void fetchConfig() {

        // check is a config is not being fetched already
        synchronized (this) {
            if (!isFetchingConfig) {
                isFetchingConfig = true;
                configException = null;
            } else {
                return;
            }
        }

        // get the current time before configuration retrieval
        final long startTime = System.currentTimeMillis();

        // execute async http request (via OkHttp library)
        okHttpClient.newCall(new Request.Builder().url(configUrl).build()).enqueue(
                new Callback() {

                    /**
                     * Network call failed
                     */
                    @Override
                    public void onFailure(Call call, IOException e) {

                        Log.i(TAG, " configuration failed: " + e.getMessage());

                        // retry later (at next getBidderAdapter() call) if network failed for IOException reason
                        synchronized (this) {
                            isFetchingConfig = false;
                            // do not store exception as next call might be successful
                        }
                    }

                    /**
                     * Network call completed, but might be unsuccessful
                     */
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        Log.i(TAG, " configuration network call took " + (System.currentTimeMillis() - startTime) + "ms");

                        // call was successful
                        if (response.isSuccessful()) {

                            // get file contents
                            String amazonConfigJSON = response.body().string();

                            // close body, not useful anymore
                            response.body().close();

                            // try to create JSON object from response
                            try {

                                // create JSON object from String
                                JSONObject amazonConfigJSONObject = new JSONObject(amazonConfigJSON);

                                // extract prices currencyCode
                                currencyCode = amazonConfigJSONObject.getString(PRICES_CURRENCY_CODE);

                                // extract creative tag
                                creativeTag = amazonConfigJSONObject.getString(CREATIVE_TAG);

                                // extract price points
                                String pricePointsString = amazonConfigJSONObject.getString(PRICE_POINTS);

                                // check if price points string is not empty
                                if (pricePointsString != null && pricePointsString.length() > 0) {

                                    // wipe previous prices map
                                    prices.clear();

                                    // fill price point map
                                    String[] pricePointArray = pricePointsString.split(" ");
                                    for (String pricePoint: pricePointArray) {

                                        // split pricepoint string using separator
                                        String[] tokens = pricePoint.split(":");

                                        // if there is an incorrect number of tokens, throw an exception
                                        if (tokens.length != 2) {
                                            configException = new ConfigurationException("The received Amazon bidder configuration contains invalid price point description : " + pricePoint);
                                        }

                                        // try to add a price in the Map. If there is an incorrect value, throw an exception
                                        try {
                                            prices.put(tokens[0], 0.3);
                                        } catch (NumberFormatException e) {
                                            configException = new ConfigurationException("The received Amazon bidder configuration contains invalid price point value : " + tokens[1]);
                                        }
                                    }
                                } else {
                                    configException = new ConfigurationException("The received Amazon bidder configuration does not contain any price point");
                                }


                            } catch (JSONException e) {
                                // if JSON is invalid, throw an exceptions
                                configException = new ConfigurationException("The received Amazon bidder configuration JSON is not valid", e);
                            }

                            // if we did not hit an exception so far, mark this SASAmazonBidderConfigManager as ready
                            if (configException == null) {
                                setReady(true);
                                Log.i(TAG, " Bidder configuration  took " + (System.currentTimeMillis() - startTime) + "ms");
                            }
                        } else {
                            // create exception to avoid future retries as this URL is unreachable
                            configException = new ConfigurationException("Amazon bidder config manager URL unreachable : " + response.code());
                        }


                        // mar this SASAmazonBidderConfigManager config fetching as done
                        synchronized (this) {
                            isFetchingConfig = false;
                        }

                    }
                }
        );
    }

    /**
     * Private constructor for singleton instance
     */
    private SASAmazonBidderConfigManager()  {
        // initialize configException to a non configured state
        configException = new ConfigurationException("SASAmazonBidderConfigManager singleton is not configured yet," +
                "configure() must be called first with a valid configuration URL");
    }

    /**
     * Configure singleton instance
     * @param configURL
     */
    public synchronized void configure(String configURL) throws IllegalArgumentException {

        if (this.configUrl == null) {

            // check URL validity
            try {
                URL url = new URL(configURL);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Amazon bidder config manager URL is not valid", e);
            }
            // store config file url
            this.configUrl = configURL;

            // init OkHttpClient
            okHttpClient = new OkHttpClient.Builder().build();

            // retrieve config at url
            fetchConfig();
        } else {
            Log.i(TAG," is already configured!");
        }

    }

    /**
     * Returns the 3 letters currency code for pricepoint prices
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Returns the Amazon creative tag
     */
    public String getCreativeTag() {
        return creativeTag;
    }

    /**
     * Returns a price for a given pricepoint
     */
    public double getPriceForPricePoint(String pricePoint) {
        Double value = prices.get(pricePoint);
        return value != null ? value : 0;
    }


    /**
     * Returns whether this {@link SASAmazonBidderConfigManager} is ready or not
     */
    private synchronized boolean isReady() throws ConfigurationException  {
        if (configException != null) {
            throw configException;
        }
        return isReady;
    }

    /**
     * Sets whether this {@link SASAmazonBidderConfigManager} is ready or not
     */
    private synchronized void setReady(boolean ready) {
        this.isReady = ready;
    }

    @Nullable public SASAmazonBidderAdapter getBidderAdapter(DTBAdResponse adResponse) throws ConfigurationException {

        if (isReady()) {
            return new SASAmazonBidderAdapter(adResponse);
        } else {

            // trigger new config fetching
            fetchConfig();

            // return null bidder for this time
            return null;
        }
    }
}
