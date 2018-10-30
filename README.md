# # Smart AdServer — Android in-app bidding adapters & samples

_In-App bidding_ allows you to create a real time competition between direct sales, _Smart RTB+_ and third party ad networks just like header bidding does in a web environment.

This repository contains some in-app bidding adapters that can be used with the _Smart Display SDK **6.9 and up**_.

Integration samples are also available for each adapter (third party SDK may be required to build these samples).

## Requirements

- A _Smart AdServer_ account
- _Smart Display SDK_ 6.9 and up
- Android Studio 3.0 or higher

## How does it work?

Just like header bidding on the web, your application may call a third party partner at any moment to get an ad response along with a CPM for the display of the ad.
 
Then, for appropriate placements, you will pass the CPM (through an adapter object) to the ad view you want to load. While making its own ad call, the ad view will forward the CPM to our ad server and _Holistic+_ competition will occur between your programmed insertions (direct and _RTB+_) and the in-app bidding winner. The ad server will determine the ad with the highest CPM and tell the SDK which creative should be displayed to maximize your revenues.

## Available adapters

Adapters are available for the following third party SDK:

| SDK | Website | Adapter & documentation |
| --- | ------- | ----------------------- |
| Amazon Publisher Services | https://ams.amazon.com/webpublisher _(login required)_ | [Amazon adapter](Amazon/) |

## Custom adapters

You can add partners for in-app bidding without having to wait for _Smart_ to integrate them for you.

The  [SASBidderAdapter](http://help.smartadserver.com/Android/v6.9/Content/refman/reference/com/smartadserver/android/library/headerbidding/SASBidderAdapter.html) interface can be implemented by custom classes that will act as bidder adapters. These adapters should be initialized from the ad response you get from the in-app bidding partner and then passed to the [SASAdView](http://help.smartadserver.com/Android/v6.9/Content/refman/reference/com/smartadserver/android/library/ui/SASAdView.html) when loading an ad. _Holistic+_ competition will then happen server side and the ad with the highest CPM will be displayed.

### Technical overview

In-app bidding is based on the same principle as header bidding for web integrations.

Your application will make a call to third parties networks through a third party SDK, the result of this call is passed to the _Smart Display SDK_ through a bidder adapter object, it is forwarded to _Smart_ ad server which will arbitrate between its own server-side connected partners, your direct campaigns and the winner ad of the in-app bidding. 

The winner of this competition will then be returned to the _Smart Display SDK_ with the ultimate goal to ensure that the ad with the highest CPM is displayed.

#### Step by step workflow

1. Integrate the in-app bidding SDK (s) of your partner(s)
2. Request ads from the SDK(s) of your partner(s) and find the most valuable ad for you _(most partners will return only the best ad, but if you integrate several partners you will have to arbitrate which ad is the best between the different responses)_
3. Instantiate a **bidding adapter** from your partner's response with CPM, currency, name, etc... see the next section for the implementation of your adapter.
4. Pass this adapter to your [SASAdView](http://help.smartadserver.com/Android/v6.9/Content/refman/reference/com/smartadserver/android/library/ui/SASAdView.html) when calling the loadAd method
5. _Smart_ will use server-side _Holistic+_ capabilities to return the ad with the best CPM from all your sources
    - If the winning ad comes from _Smart_, the _Smart Display SDK_ will display it as usual
    - If _Smart_ loses the competition against your bidding partner, the adapter will be notified and 2 situations can happen:
        - Your partner in-app bidding SDK **has capabilities to display the winner ad**: you must request it to do so, this can be done through the adapter
        - Your partner in-app bidding SDK **does not have capabilities to display the winner ad**: the _Smart Display SDK_ will display it as long as the adapter is able to provide the HMTL ad markup.

Read the next sections to learn more about the [SASBidderAdapter](http://help.smartadserver.com/Android/v6.9/Content/refman/reference/com/smartadserver/android/library/headerbidding/SASBidderAdapter.html) interface.

#### SASBidderAdapter interface

To create a custom bidder adapter, you must create a class that implements the [SASBidderAdapter](http://help.smartadserver.com/Android/v6.9/Content/refman/reference/com/smartadserver/android/library/headerbidding/SASBidderAdapter.html) interface.
 
Most of the properties should be set in the constructor of your bidder adapter even if they will only be 'consumed' server-side when the adapter informations are passed into the ad call.

##### Rendering type

When initializing your adapter, make sure to return an appropriate  [SASBidderAdapter.RenderingType](http://help.smartadserver.com/Android/v6.10/Content/refman/reference/com/smartadserver/android/library/headerbidding/SASBidderAdapter.RenderingType.html) value in the ```getRenderingType()``` method:

- ```SASBidderAdapter.RenderingType.PrimarySDK``` means that the _Smart Display SDK_ will be responsible for rendering the winning creative whether it comes from the ad server or the in-app bidding competition. It also means that your adapter must provide the HTML ad markup to be displayed.
- ```SASBidderAdapter.RenderingType.ThirdPartySDK``` means that your partner's SDK will be responsible for rendering the winning creative if (and only if) the ad server (Smart) loses the bidding competition (If the winning creative comes from _Smart_, the display will be done by the _Smart Display SDK_ anyways).

##### Rendering methods

For the rendering of the creative to occur properly, you need to implement these methods:

    /**
     * Returns the HTML markup that must be displayed by the primary SDK when the winning creative is the one returned by the bidder.
     * <p>
     * This markup is available in the documentation of each header bidding partner and often depends on several parameters, including the creative size.
     *
     * @return the HTML markup that must be displayed by the primary SDK, or null if ThirdPartySDK rendering type is used
     */
    @Nullable
    String getBidderWinningAdMarkup();

    /**
     * This method is called when the bidder's winning ad is displayed, in case the primary SDK is responsible for creative rendering.
     * <p>
     * You may perform actions when receiving this event, like counting impressions on your side, or trigger a new header bidding call, etc…
     */
    void primarySDKDisplayedBidderAd();

    /**
     * This method is called when the bidder's winning ad is clicked, in case the primary SDK is responsible for creative rendering.
     * <p>
     * You may perform action when receiving this event, like counting clicks on your side, etc…
     */
    void primarySDKClickedBidderAd();

    /**
     * This method is called when the primary SDK did not return an ad with a better CPM than the bidder ad:
     * <p>
     * - if rendering type is set to PrimarySDK, there is nothing to implement here.
     * - if rendering type is set to ThirdPartySDK, you should cascade the information with all necessary parameters so that the winning ad is properly displayed.
     */
    void primarySDKLostBidCompetition();

The documentation of the method is pretty straightforward, just note that when choosing ```SASBidderAdapter.RenderingType.ThirdPartySDK``` rendering type you will have to trigger the display of the creative by the in-app bidding partner's SDK when ```primarySDKLostBidCompetition()``` method is called.

This means that your adapter should also keep a reference to the ad loader of the in-app bidding partner's SDK.
