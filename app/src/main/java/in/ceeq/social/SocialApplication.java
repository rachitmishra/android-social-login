package in.ceeq.social;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

public class SocialApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(this);
        TwitterAuthConfig authConfig =
                new TwitterAuthConfig(BuildConfig.TWITTER_CONSUMER_KEY, BuildConfig.TWITTER_CONSUMER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
    }

}
