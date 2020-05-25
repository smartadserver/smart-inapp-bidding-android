# Amazon Bidder Adapter

The Amazon bidder adapter allows you to connect _Amazon Publisher Services_ in-app bidder SDK with _Smart Display SDK_.

You will find in this repository the classes you need to connect _Amazon Publisher Services_ in-app bidding and _Smart Display SDK_, as well as a sample in the [Sample directory](Sample/).

## Bidder implementation structure

The _Amazon bidder adapter_ is splitted into three different classes:

- ```SASAmazonBaseBidderAdapter```: this class is an abstract class implementing the [SASBidderAdapter](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/thirdpartybidding/SASBidderAdapter.html) interface regrouping the code in common for Banner and Interstitial concrete adapters
- ```SASAmazonBannerBidderAdapter```: this class implements the [SASBannerBidderAdapter](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/thirdpartybidding/SASBannerBidderAdapter.html) and is the adapter you should use to load an Amazon banner ad in a [SASBannerView](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/ui/SASBannerView.html), as it provides the needed third party banner rendering capabilities.
- ```SASAmazonInterstitialBidderAdapter```: this class implements the [SASInterstitialBidderAdapter](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/thirdpartybidding/SASInterstitialBidderAdapter.html) and is the adapter you should use to load an Amazon interstitial ad in a [SASInterstitialManager](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/ui/SASInterstitialManager.html), as it provides the needed third party interstitial rendering capabilities.

## Using the Amazon bidder adapter in your app

Request an Amazon ad using ```DTBAdLoader```, then:

For banner ads, create an instance of ```SASAmazonBannerBidderAdapter``` using the Amazon ad response when the Amazon call is successful, and pass it to the loadAd() call on the Smart SASBannerview:

    @Override
    public void onSuccess(DTBAdResponse dtbAdResponse) {
        Log.i(TAG, "Amazon ad request is successful");
        // Amazon returned an ad, wrap it in a SASAmazonBannerBidderAdapter object and pass it to the Smart ad call
        SASAmazonBannerBidderAdapter bidderAdapter =
                new SASAmazonBannerBidderAdapter(dtbAdResponse, HeaderBiddingBannerActivity.this);
        bannerView.loadAd(adPlacement, bidderAdapter);
    }

For interstitial ads, create an instance of ```SASAmazonInterstitialBidderAdapter``` using the Amazon ad response when the Amazon call is successful, and pass it to the loadAd() call on the Smart SASInterstitialManager:

    @Override
    public void onSuccess(DTBAdResponse dtbAdResponse) {
        Log.i(TAG, "Amazon ad request is successful");
        // Amazon returned an ad, wrap it in a SASAmazonInterstitialBidderAdapter object and pass it to the Smart ad call
        SASAmazonInterstitialBidderAdapter bidderAdapter =
                new SASAmazonInterstitialBidderAdapter(dtbAdResponse, HeaderBiddingInterstitialActivity.this);
        interstitialManager.loadAd(bidderAdapter);
    }

At this point, the adapter and the _Smart Display SDK_ will take care of everything for the most valuable ad to be displayed automatically, while still providing callbacks to the delegate of the _Smart Display SDK_ ad instance.

Please note that an _Amazon bidder adapter_ **can only be used once**.
