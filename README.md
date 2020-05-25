# Smart AdServer — Android - Third party in-app bidding adapters & samples

_In-App bidding_ allows you to create a real time competition between direct sales, _Smart RTB+_ and third party ad networks just like header bidding does in a web environment.

This repository contains some in-app bidding adapters that can be used with the _Smart Display SDK **7.6 and up**_.

Integration samples are also available for each adapter (third party SDK may be required to build these samples).

## Requirements

- A _Smart AdServer_ account
- _Smart Display SDK_ 7.6 and up
- Android Studio 3.0 or higher

## How does it work?

Just like header bidding on the web, your application may call a third party partner at any moment to get an ad response along with a price associated with a third party ad.

Then, for appropriate placements, you will pass this price to the _Smart Display SDK_ ad view through a bidding adapter object. While performing its own ad call, the ad view will forward the price (or it's representation as a keyword) to our ad server and _Holistic+_ competition will occur between your programmed insertions (direct and programmatic) and the third party in-app bidding winner. The ad server will determine the ad with the highest CPM and inform _Smart Display SDK_ which creative should be displayed to maximize your revenues.

## Available adapters

Adapters are available for the following third party in-app bidding SDK:

| SDK | Website | Adapter & documentation |
| --- | ------- | ----------------------- |
| Amazon Publisher Services | https://ams.amazon.com/webpublisher _(login required)_ | [Amazon adapter](Amazon/) |

## Custom adapters

You can add partners for in-app bidding without having to wait for _Smart_ to integrate them.

The _Smart Display SDK_ has dedicated interfaces that can be implemented by custom classes to create third party in-app bidding adapters. These adapters should be initialized from the ad response you get from the in-app bidding partner and be passed to the _loadAd_ methods of the [SASBannerView](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/ui/SASAdView.html#loadAd(com.smartadserver.android.library.model.SASAdPlacement,%20com.smartadserver.android.library.thirdpartybidding.SASBidderAdapter)) or the [SASInterstitialManager](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/ui/SASInterstitialManager.html#loadAd(com.smartadserver.android.library.thirdpartybidding.SASBidderAdapter)). _Holistic+_ competition will then happen server side and the ad with the highest CPM will be displayed.

### Technical overview

In-app bidding is based on the same principle than header bidding for web integrations.

Your application will make a call to third parties networks through a third party SDK, the result of this call is passed to the _Smart Display SDK_ through an adapter object, then it is forwarded to _Smart_ ad server which will arbitrate between its own server-side connected partners, your direct campaigns and the winner ad of the in-app bidding.

The winner of this competition will then be returned to the _Smart Display SDK_ with the ultimate goal to ensure that the ad with the highest CPM is displayed.

#### Step by step workflow

1. Integrate the in-app bidding SDK of your partner(s)
2. Request ads from the SDK of your partner(s) and find the most valuable ad for you _(most partners will return only the best ad, but if you integrate several partners you will have to arbitrate which ad is the best between the different responses)_
3. Instantiate a **bidding adapter** from your partner's response with all relevant details to run the server-side competition (CPM, currency, name, keyword, etc… see the next section for the implementation of your adapter).
4. Pass this adapter when calling [loadAd()](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/ui/SASAdView.html#loadAd(com.smartadserver.android.library.model.SASAdPlacement,%20com.smartadserver.android.library.thirdpartybidding.SASBidderAdapter)) on your SASBannerView or [loadAd()](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/ui/SASInterstitialManager.html#loadAd(com.smartadserver.android.library.thirdpartybidding.SASBidderAdapter)) on your SASInterstitialManager.
5. _Smart_ will use server-side _Holistic+_ capabilities to return the ad with the best CPM from all your monetization sources
    - If the winning ad comes from _Smart_, the _Smart Display SDK_ will display it as usual
    - If _Smart_ loses the competition against your bidding partner, the adapter will be notified and 2 situations can happen:
        - Your partner in-app bidding SDK **has capabilities to display the winner ad**: you must request it to do so, this can be done through the adapter by forwarding the response to your application or by mediating the rendering of the partner SDK directly with the adapter
        - Your partner in-app bidding SDK **does not have capabilities to display the winner ad**: the _Smart Display SDK_ will display it as long as the adapter is able to provide the HMTL ad markup.

Read the next sections to learn more about the interfaces [SASBidderAdapter](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/thirdpartybidding/SASBidderAdapter.html), [SASBannerBidderAdapter](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/thirdpartybidding/SASBannerBidderAdapter.html) and  [SASInterstitialBidderAdapter](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/thirdpartybidding/SASInterstitialBidderAdapter.html).

#### SASBidderAdapter interface and its descendants

To create a custom bidder adapter, you must create a class that implements the [SASBidderAdapter](http://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/headerbidding/SASBidderAdapter.html) interface, or one of the two inheriting interfaces [SASBannerBidderAdapter](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/thirdpartybidding/SASBannerBidderAdapter.html) (with third party banner rendering capabilities) and  [SASInterstitialBidderAdapter](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/thirdpartybidding/SASInterstitialBidderAdapter.html) (with third party interstitial rendering capabilities).

Most of the values returned by the getter methods should be set upon initialization of your adapter even if they will only be 'consumed' server-side when the adapter informations are passed into the ad call.

##### Competition type

When initializing your adapter, make sure to set the proper ```competitionType```:

- ```SASBidderAdapterCompetitionTypePrice``` means that the _Smart Display SDK_ will pass the price and the currency as parameters of the ad call, unobfuscated. With this competition type, you must implement the properties ```price``` and ```currency``` of the adapter.
- ```SASBidderAdapterCompetitionTypeKeyword``` means that the _Smart Display SDK_ will pass a representation of the price as a keyword in the ad call. This keyword must match with the keyword targeting of a programmed insertion that has also an eCPM priority and CPM filled in the ad server. With this competition type, you must implement the property ```keyword``` of the adapter.

##### Rendering type

When initializing your adapter, make sure to set the proper ```creativeRenderingType```:

- ```SASBidderAdapterCreativeRenderingTypePrimarySDK``` means that the _Smart Display SDK_ will be responsible for rendering the winning creative whether it comes from the ad server or the in-app bidding competition. It also means that your adapter must provide the HTML ad markup to be displayed.
- ```SASBidderAdapterCreativeRenderingType3rdParty``` means that your partner's SDK will ultimately be responsible for rendering the winning creative and that a third party (such as your application) will trigger it. This situation occurs if (and only if) Smart's ad server loses the bidding competition. If the winning creative comes from _Smart_, the display will be done by the _Smart Display SDK_. 
- ```SASBidderAdapterCreativeRenderingTypeMediation``` means that your partner's SDK will ultimately be responsible for rendering the winning creative but that the _Smart Display SDK_ will mediate this rendering through the adapter and forward all necessary callbacks to the delegate of the instanciated _Smart Display SDK_ ad view. 

##### Win Notification Callback

When the ad server loses the server-side competition, meaning that it was not able to return an ad with a higher CPM than the third party bidder, _Smart Display SDK_ will trigger the method:

    /**
     * This method is called when Smart Display SDK did not return an ad with a better CPM than the bidder ad:
     */
    void primarySDKLostBidCompetition();

When this method is called, you should perform all actions that you think relevant to log the competition result. 

##### Rendering methods

Several methods can be called for the rendering to occur, depending on the rendering type of the adapter.


###### Smart Display SDK Creative Rendering

This corresponds to the ```SASBidderAdapterCreativeRenderingTypePrimarySDK``` rendering type. For rendering of the creative to occur properly, your adapter must implement these methods:

    /**
     * Returns the HTML markup that must be displayed by Smart Display SDK when the winning creative is the one returned by the bidder.
     * <p>
     * This markup is available in the documentation of each header bidding partner and often depends on several parameters, including the creative size.
     *
     * @return the HTML markup that must be displayed by Smart Display SDK, or null if ThirdParty or Mediation rendering type is used
     */
    @Nullable
    String getBidderWinningAdMarkup();

    /**
     * This method is called when the bidder's winning ad is displayed, in case Smart Display SDK is responsible for creative rendering.
     * <p>
     * You may perform actions when receiving this event, like counting impressions on your side, or trigger a new header bidding call, etc…
     */
    void primarySDKDisplayedBidderAd();

    /**
     * This method is called when the bidder's winning ad is clicked, in case Smart Display SDK is responsible for creative rendering.
     * <p>
     * You may perform action when receiving this event, like counting clicks on your side, etc…
     */
    void primarySDKClickedBidderAd();

###### Third party Creative Rendering

This corresponds to the ```SASBidderAdapterCreativeRenderingType3rdParty``` rendering type. For rendering of the creative to occur properly, your adapter must implement this method:

    /**
     * This method is called when Smart Display SDK did not return an ad with a better CPM than the bidder ad:
     * <p>
     * - if rendering type is set to PrimarySDK or Mediation, there is nothing to implement here.
     * - if rendering type is set to ThirdParty, you should cascade the information with all necessary parameters so that the winning ad is properly displayed.
     */
    void primarySDKRequestedThirdPartyRendering();

Note that with this mode, your adapter will have to trigger the display of the creative by the in-app bidding partner's SDK when the method is called. This means that your adapter should also keep a reference to the ad loader of the in-app bidding partner's SDK and will be responsible to forward ad events to the application.

###### Mediation Creative Rendering - Banners

This corresponds to the ```SASBidderAdapterCreativeRenderingTypeMediation``` rendering type for banner ads. For rendering of the creative to occur properly, your adapter must actually implement the [SASBannerBidderAdapter](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/thirdpartybidding/SASBannerBidderAdapter.html) interface (that extends the [SASBidderAdapter](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/thirdpartybidding/SASBidderAdapter.html) interface), and particulary this method:

    /**
     * This method is called when Smart Display SDK requires the Bidder SDK to render the banner associated with the
     * bidder ad received previously
     * @param bannerBidderAdapterListener a callback object to pass the renderer banner view to the Smart SDK, and to notify
     *                                    it of various lifecycle events on the banner
     */
    void loadBidderBanner(@NonNull SASBannerBidderAdapterListener bannerBidderAdapterListener);

When this method is called, you have to render the banner creative using the third party SDK that won the bid, and pass the banner view back to the Smart SDK by calling the [onBannerLoaded](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/mediation/SASMediationBannerAdapterListener.html#onBannerLoaded(android.view.View)) method of the SASBidderBannerAdapterListener instance passed in parameter.
You will also have to notify the Smart SDK of subsequent events occuring on the third party banner by calling appropriate methods on that SASBannerBidderAdapterListener instance, such as [onAdClicked](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/mediation/SASMediationAdapterListener.html#onAdClicked()). Please refer to the [SASBannerBidderAdapterListener](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/thirdpartybidding/SASBannerBidderAdapterListener.html) reference documentation to see all available callback methods.


###### Mediation Creative Rendering - Interstitials

For rendering of the creative to occur properly, your adapter must actually implement the [SASInterstitialBidderAdapter](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/thirdpartybidding/SASInterstitialBidderAdapter.html) interface (that extends the [SASBidderAdapter](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/thirdpartybidding/SASBidderAdapter.html) interface), and particulary these methods:

    /**
     * This method is called when Smart Display SDK requires the Bidder SDK to load the interstitial associated with the
     * bidder ad received previously
     * @param interstitialBidderAdapterListener a callback object to notify the Smart SDK of various lifecycle events on the
     *                                          interstitial (like when the interstitial is loaded and ready to be displayed
     *                                          for instance
     */
     void loadBidderInterstitial(@NonNull SASInterstitialBidderAdapterListener interstitialBidderAdapterListener);


    /**
     * This method is called when Smart Display SDK requires the Bidder SDK to display the previously loaded
     * interstitial ad.
     */
     void showBidderInterstitial();

When the [loadBidderInterstitial()](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/thirdpartybidding/SASInterstitialBidderAdapter.html#loadBidderInterstitial(com.smartadserver.android.library.thirdpartybidding.SASInterstitialBidderAdapterListener)) method is called, you have to render the interstitial creative using the third party SDK that won the bid, and notify the Smart SDK when it is ready to be displayed by calling the [onInterstitialLoaded()](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/mediation/SASMediationInterstitialAdapterListener.html#onInterstitialLoaded()) method of the SASInterstitialBidderAdapterListener instance passed in parameter.
When the [showBidderInterstitial()](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/thirdpartybidding/SASInterstitialBidderAdapter.html#showBidderInterstitial()) method is called, you have to show the interstitial creative using the third party SDK that won the bid, and notify the Smart SDK when it is actually displayed by calling the [onInterstitialShown()](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/mediation/SASMediationInterstitialAdapterListener.html#onInterstitialShown()) method of the SASInterstitialBidderAdapterListener instance passed in parameter.

You will also have to notify the Smart SDK of subsequent events occuring on the third party interstitial by calling appropriate methods on that SASInterstitialBidderAdapterListener instance, such as [onAdClicked](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/mediation/SASMediationAdapterListener.html#onAdClicked()). Please refer to the [SASInterstitialBidderAdapterListener](https://documentation.smartadserver.com/displaySDK/android/API/reference/com/smartadserver/android/library/thirdpartybidding/SASInterstitialBidderAdapterListener.html) reference documentation to see all available callback methods.
