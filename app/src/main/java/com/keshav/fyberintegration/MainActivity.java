package com.keshav.fyberintegration;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.Toast;

import com.fyber.Fyber;
import com.fyber.ads.AdFormat;
import com.fyber.ads.banners.BannerAd;
import com.fyber.ads.banners.BannerAdListener;
import com.fyber.ads.banners.BannerAdView;
import com.fyber.ads.interstitials.InterstitialActivity;
import com.fyber.ads.interstitials.InterstitialAdCloseReason;
import com.fyber.ads.videos.RewardedVideoActivity;
import com.fyber.requesters.InterstitialRequester;
import com.fyber.requesters.OfferWallRequester;
import com.fyber.requesters.RequestCallback;
import com.fyber.requesters.RequestError;
import com.fyber.requesters.RewardedVideoRequester;
import com.fyber.utils.testsuite.IntegrationAnalysisListener;
import com.fyber.utils.testsuite.IntegrationAnalyzer;
import com.fyber.utils.testsuite.IntegrationReport;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FYBER";
    protected static final int INTERSTITIAL_REQUEST_CODE = 9012;
    protected static final int REWARDED_VIDEO_REQUEST_CODE = 5678;
    protected static final int OFFER_WALL_REQUEST_CODE = 1234;
    private Intent interstitialIntent;
    private Intent rewardedVideoIntent;
    private Intent offerwallIntent;
    Button reqRV, showRV;
    Button reqOW, showOW;
    Button reqIS, showIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Log.d(TAG, "Inside onCreate");

        reqRV = findViewById(R.id.reqRvButton);
        showRV = findViewById(R.id.showRvButton);
        showRV.setEnabled(false);

        reqOW = findViewById(R.id.reqOwButton);
        showOW = findViewById(R.id.showOwButton);
        showOW.setEnabled(false);

        reqIS = findViewById(R.id.reqIsButton);
        showIS = findViewById(R.id.showISButton);
        showIS.setEnabled(false);

        // Request and show RV ads
        reqRV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, " Req RV Button Clicked");
                rewardedAds();
                Toast.makeText(MainActivity.this, "RV Request: added delay of 5 secs", Toast.LENGTH_SHORT).show();
                verifyIntegration();
            }
        });

        showRV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "show RV Button Clicked");
                startActivityForResult(rewardedVideoIntent, REWARDED_VIDEO_REQUEST_CODE);
            }
        });

        //  Request and show OW ads
        reqOW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, " Req OW Button Clicked");
                offerwallAds();
                Toast.makeText(MainActivity.this, "OW Request: added delay of 5 secs", Toast.LENGTH_SHORT).show();
            }
        });

        showOW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "show OW Button Clicked");
                startActivityForResult(offerwallIntent, OFFER_WALL_REQUEST_CODE);
            }
        });

        //Request and show IS ads
        reqIS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, " Req IS Button Clicked");
                interstitialAds();
                Toast.makeText(MainActivity.this, "Interstitial Request: added delay of 5 secs", Toast.LENGTH_SHORT).show();
            }
        });

        showIS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "show IS Button Clicked");
                startActivityForResult(interstitialIntent, INTERSTITIAL_REQUEST_CODE);
            }
        });

        bannerAds();
    }
        // INTEGRATION ANALYZER
    private void verifyIntegration() {
        Log.d(TAG, "verifyIntegration invoked");
        IntegrationAnalyzer.analyze(
                new IntegrationAnalysisListener() {
                    @Override
                    public void onAnalysisSucceeded(IntegrationReport integrationReport) {
                        //successful analysis
                        Log.d(TAG, "successful analysis");

                        integrationReport.getTestSuiteVersion();
                        integrationReport.getFyberSdkVersion();
                        integrationReport.getAppID();
                        integrationReport.getUserID();
                        integrationReport.isAnnotationsCorrectlyIntegrated();
                        integrationReport.isAnnotationsCompatible();
                        integrationReport.getStartedBundles();
                        integrationReport.getUnstartedBundles();
                    }

                    @Override
                    public void onAnalysisFailed(IntegrationAnalyzer.FailReason failReason) {
                        //something went wrong with the analysis - see message from FailReason
                        String message = failReason.getMessage();
                        Log.d(TAG, "Fail Reason message is" + message);
                        //..

                    }
                }
        );
    }


    //Listeners for banner ads.
    private void bannerAds() {
        Log.d(TAG, "bannerAds invoked");
        BannerAdView bannerAdView = new BannerAdView(MainActivity.this);
        bannerAdView.loadOnAttach();
        FrameLayout bannerPlaceholder = findViewById(R.id.banner_placeholder);
        bannerPlaceholder.addView(bannerAdView);
        bannerAdView = new BannerAdView(this)
                .withListener(new BannerAdListener() {
                    @Override
                    public void onAdError(BannerAd ad, String error) {
                        // Called when the banner triggered an error
                        Log.d(TAG, "Something went wrong with the request: " + error);
                    }

                    @Override
                    public void onAdLoaded(BannerAd ad) {
                        // Called when the banner has been successfully loaded
                        Log.d(TAG, "Banner successfully loaded");

                    }

                    @Override
                    public void onAdClicked(BannerAd ad) {
                        // Called when the banner was clicked
                        Log.d(TAG, "User clicked on banner");
                    }

                    @Override
                    public void onAdLeftApplication(BannerAd ad) {
                        // Called when the banner interaction causes an external application to be open
                        Log.d(TAG, "User directed out of app by banner");
                    }
                });
    }


    // Listeners for Interstitial ads
    private void interstitialAds() {
        Log.d(TAG, "interstitialAds invoked");
        final RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onAdAvailable(Intent intent) {
                // Store the intent that will be used later to show the interstitial
                interstitialIntent = intent;
                Log.d(TAG, "IS: Offers are available");
                Toast.makeText(MainActivity.this, "IS: Offers are available", Toast.LENGTH_SHORT).show();
                showOW.setEnabled(false);
            }

            @Override
            public void onAdNotAvailable(AdFormat adFormat) {
                // Since we don't have an ad, it's best to reset the interstitial intent
                interstitialIntent = null;
                Log.d(TAG, "IS: No ad available");
                Toast.makeText(MainActivity.this, "IS: No ad available", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestError(RequestError requestError) {
                // Since we don't have an ad, it's best to reset the interstitial intent
                interstitialIntent = null;
                Log.d(TAG, "IS: Something went wrong with the request: " + requestError.getDescription());
                Toast.makeText(MainActivity.this, "IS: Something went wrong with the request: ", Toast.LENGTH_SHORT).show();
            }
        };

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InterstitialRequester.create(requestCallback)
                        .request(MainActivity.this);
                Log.d(TAG, "Interst Request: added delay of 5 secs");
            }
        }, 5000);
    }

    // Listeners for Rewarded Ads
    private void rewardedAds() {
        Log.d(TAG, "rewardedAds invoked");
        final RequestCallback requestCallback = new RequestCallback() {

            @Override
            public void onAdAvailable(Intent intent) {
                // Store the intent that will be used later to show the video
                rewardedVideoIntent = intent;
                Log.d(TAG, "RV: Offers are available");
                showRV.setEnabled(true);
                Toast.makeText(MainActivity.this, "RV: Offers are available ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdNotAvailable(AdFormat adFormat) {
                // Since we don't have an ad, it's best to reset the video intent
                rewardedVideoIntent = null;
                Log.d(TAG, "RV: No ad available ");
                Toast.makeText(MainActivity.this, "RV: No ad available  ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestError(RequestError requestError) {
                // Since we don't have an ad, it's best to reset the video intent
                rewardedVideoIntent = null;
                Log.d(TAG, "RV: Something went wrong with the request: " + requestError.getDescription());
                Toast.makeText(MainActivity.this, "RV: Something went wrong with the request: ", Toast.LENGTH_SHORT).show();
            }
        };

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                RewardedVideoRequester.create(requestCallback)
                        .request(MainActivity.this);
                Log.d(TAG, "RV request: added delay of 5 secs");
                Toast.makeText(MainActivity.this, "RV Request: delay of 5 secs", Toast.LENGTH_SHORT).show();
            }
        }, 5000);
    }


    //Listeners for OfferWall ads.
    private void offerwallAds() {
        Log.d(TAG, "offerwallAds invoked");
        final RequestCallback requestCallback = new RequestCallback() {

            @Override
            public void onAdAvailable(Intent intent) {
                // Store the intent that will be used later to show the Offer Wall
                offerwallIntent = intent;
                Log.d(TAG, "OW: Offers are available");
                Toast.makeText(MainActivity.this, "OW: Offers are available", Toast.LENGTH_SHORT).show();
                showOW.setEnabled(true);
            }

            @Override
            public void onAdNotAvailable(AdFormat adFormat) {
                // Since we don't have an ad, it's best to reset the Offer Wall intent
                offerwallIntent = null;
                Log.d(TAG, "OW: No ad available");
                Toast.makeText(MainActivity.this, "OW: No ad available", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestError(RequestError requestError) {
                // Since we don't have an ad, it's best to reset the Offer Wall intent
                offerwallIntent = null;
                Log.d(TAG, "OW: Something went wrong with the request: " + requestError.getDescription());
                Toast.makeText(MainActivity.this, "OW: Something went wrong with the request:", Toast.LENGTH_SHORT).show();
            }
        };

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean shouldCloseOfferwall = true;
                OfferWallRequester.create(requestCallback)
                        .closeOnRedirect(shouldCloseOfferwall)
                        .request(MainActivity.this);
                Log.d(TAG, "OW request: added delay of 5 secs");
            }
        }, 5000);
    }


    // As is standard in Android, onActivityResult will be called when any activity closes that had been started with startActivityForResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle the closing of the interstitial
        if (resultCode == RESULT_OK && requestCode == INTERSTITIAL_REQUEST_CODE) {

            // check the ad status
            InterstitialAdCloseReason adStatus = (InterstitialAdCloseReason) data.getSerializableExtra(InterstitialActivity.AD_STATUS);
            if (adStatus.equals(InterstitialAdCloseReason.ReasonUserClickedOnAd)) {
                // The user clicked on the interstitial, which closed the ad
                Log.d(TAG, "The interstitial ad was dismissed because the user clicked it");
            } else if (adStatus.equals(InterstitialAdCloseReason.ReasonUserClosedAd)) {
                // The user deliberately closed the interstitial without clicking on it
                Log.d(TAG, "The interstitial ad was dismissed because the user closed it");
            } else if (adStatus.equals(InterstitialAdCloseReason.ReasonError)) {
                // An error occurred, which closed the ad
                Log.d(TAG, "The interstitial ad was dismissed because of an error");
            } else if (adStatus.equals(InterstitialAdCloseReason.ReasonUnknown)) {
                // The interstitial closed, but the reason why is unknown
                Log.d(TAG, "The interstitial ad was dismissed for an unknown reason");
            }
        }

        if (resultCode == RESULT_OK && requestCode == REWARDED_VIDEO_REQUEST_CODE) {

            // check the engagement status
            String engagementResult = data.getStringExtra(RewardedVideoActivity.ENGAGEMENT_STATUS);
            switch (engagementResult) {
                case RewardedVideoActivity.REQUEST_STATUS_PARAMETER_FINISHED_VALUE:
                    // The user watched the entire video and will be rewarded
                    Log.d(TAG, "The video ad was dismissed because the user completed it");
                    break;
                case RewardedVideoActivity.REQUEST_STATUS_PARAMETER_ABORTED_VALUE:
                    // The user stopped the video early and will not be rewarded
                    Log.d(TAG, "The video ad was dismissed because the user explicitly closed it");
                    break;
                case RewardedVideoActivity.REQUEST_STATUS_PARAMETER_ERROR:
                    // An error occurred while showing the video and the user will not be rewarded
                    Log.d(TAG, "The video ad was dismissed error during playing");
                    break;
            }
        }

        if (requestCode == OFFER_WALL_REQUEST_CODE) {
            // handle the closing of the Offer Wall
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Inside OnResume");
        Fyber.with("117230", this)
                .withSecurityToken("42d7ee1a3ef1fff3b5dbf667ef658a11")
                .start();
    }
}





