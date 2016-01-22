package in.ceeq.social.helpers;

import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;

public class TwitterUserApiClient extends TwitterApiClient {

    public TwitterUserApiClient(TwitterSession session) {
        super(session);
    }

    public static TwitterUserApiClient getInstance(TwitterSession session) {
        return new TwitterUserApiClient(session);
    }

    public UsersService getUsersService() {
        return getService(UsersService.class);
    }
}