
# Amazon adapter

The Amazon bidder adapter allows you to connect _Amazon Publisher Services_ in-app bidder SDK with _Smart Display SDK_.

You will find in this repository the classes you need to connect _Amazon Publisher Services_ in-app bidding and _Smart Display SDK_, as well as a sample in the [Sample directory](Sample/).

## Bidder implementation structure

The _Amazon bidder adapter_ is composed of two different classes:

- ```SASAmazonBidderAdapter```: this class is a [SASBidderAdapter](http://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/headerbidding/SASBidderAdapter.html) interface implementation and must be provided to the _Smart Display SDK_ when loading ads
- ```SASAmazonBidderConfigManager```: this singleton class acts as a bidder adapter factory that fetches a remote configuration with Amazon specific parameters first, and then creates instances of ```SASAmazonBidderAdapter``` from Amazon ad responses through the ```getBidderAdapter(DTBAdResponse adResponse)```method.
It must be initialized **once** at application startup (via its ```configure``` method), typically in the Android Application class of your application.

To work properly the _Amazon bidder adapter_ must contain mandatory parameters values : a **currency**, an **ad markup** and a **price** (a CPM). These values are retrieved automatically during the configure method call of the ```SASAmazonBidderConfigManager``` singleton class and set accordingly on the returned ```SASAmazonBidderAdapter``` instances.

To define a ```SASAmazonBidderConfigManager``` configuration, you can use the _Smart Manage interface_ to create an insertion with the _**Amazon Inapp Bidding Configuration**_ template, then retrieve the direct URL of this insertion.

You can also host the configuration JSON yourself as long as it complies with the following specification:

    {
      "pricePoints":"<a list of space separated price points>",
      "creativeTag":"<an HTML creative containing the 4 macros: %%KEYWORD:adWidth%% / %%KEYWORD:adHeight%% / %%KEYWORD:amzn_b%% / %%KEYWORD:amzn_h%%",
      "currencyCode":"<an ISO 4217 currency>"
    }

>note: a price point format is '```pricePointName:pricePointCPM```'.
>Example of pricePoints attribute value : ```"pricePoints":"t300x50p1:0.01 t300x50p2:0.02 t300x50p3:0.03"```


## Using the Amazon bidder adapter in your app

There are three major steps to use the _Amazon bidding adapter_.

### Configure the adapter

You must configure the adapter by calling ```configure(String configUrl)``` method on the ```SASAmazonBidderConfigManager``` shared instance as soon as possible: **no in-app bidding ad call will be made until the configuration has been retrieved** _(if the configuration retrieval fails, it will be retried every time an Amazon bidder adapter is instantiated)_.

The best place to retrieve the configuration is in the ```onCreate()``` method of your ```Application``` class, where you should put:

    SASAmazonBidderConfigManager.getInstance().configure("yourConfigUrl");

### Request an Amazon ad to create an instance of the Amazon bidder adapter

Request an Amazon ad using ```DTBAdLoader```, then create an instance of ```SASAmazonBidderAdapter``` using the Amazon ad response when the Amazon call is successful (i.e. in the ```OnSuccess()``` method of the ```DTBAdCallback``` object) :


    @Override  
    public void onSuccess(DTBAdResponse dtbAdResponse) {  
    Log.i(TAG, "Amazon ad request is successful");  

        try {
            // get SASAmazonBidderConfigManager singleton instance
            SASAmazonBidderConfigManager configManager = SASAmazonBidderConfigManager.getInstance();

            // request a SASAmazonBidderAdapter from the SASAmazonBidderConfigManager
            SASAmazonBidderAdapter bidderAdapter = configManager.getBidderAdapter(dtbAdResponse);

            // proceed with Smart loadAd call, passing the SASAmazonBidderAdapter instance (see below)
        } catch (Exception exception) {  
           Log.e(TAG, "Amazon bidder can't be created : " + exception);  
           // fallback: Smart call without inapp bidding
        }
    }

Please note that an _Amazon bidder adapter_ **can only be used once**.

### Make an ad call with the Amazon bidder adapter

You can now make an ad call using the _Smart Display SDK_. Simply provide the adapter instance created earlier to Smart's ad view, or interstitial manager, when loading it. If this instance is ```null```, the _Smart Display SDK_ will make an ad call without in-app bidding so you will still get an ad.

    // for a banner
    bannerView.loadAd(new SASAdPlacement(<the site ID>, <the page ID>, <the format ID>), bidderAdapter);

    // for an interstitial
    interstitialManager.loadAd(bidderAdapter);

At this point, the adapter and the _Smart Display SDK_ will take care of everything so the most valuable ad will be displayed automatically.
