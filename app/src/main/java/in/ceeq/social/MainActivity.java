package in.ceeq.social;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import java.util.Arrays;

import in.ceeq.social.helpers.PreferenceUtils;
import in.ceeq.social.helpers.TwitterUserApiClient;

public class MainActivity extends AppCompatActivity
    implements FacebookCallback<LoginResult>, View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
                       GoogleApiClient.OnConnectionFailedListener, AuthListener{

    public static final String FACEBOOK_SCOPE_PUBLIC_PROFILE = "public_profile";
    public static final int RC_SIGN_IN = 123;

    private TextView mNameTextView;
    private Button mTwitterSignInButton;
    private Button mFacebookSignInButton;
    private Button mGoogleSignInButton;
    private Button mLinkedInSignInButton;

    private CallbackManager mFacebookCallbackManager;
    private LoginManager mFacebookLoginManager;


    private TwitterAuthClient mTwitterAuthClient;
    private Callback<TwitterSession> mCallbackTwitterSession = new Callback<TwitterSession>() {
        @Override
        public void success(Result<TwitterSession> result) {
            TwitterSession session = result.data;

            TwitterUserApiClient.getInstance(session).getUsersService().show(session.getUserId(), null, true, new Callback<User>() {
                @Override
                public void success(final Result<User> result) {
                    User user = result.data;
                    mNameTextView.setText(user.name);
                    mTwitterSignInButton.setText("Logout from Twitter");
                    PreferenceUtils.set(MainActivity.this, PreferenceUtils.HAS_TWITTER_SIGN_IN, true);
                }

                @Override
                public void failure(final TwitterException e) {

                }
            });
        }
        @Override
        public void failure(TwitterException exception) {
        }
    };

    private GoogleApiClient mGoogleApiClient;

    private LISessionManager mLISessionManager;
    private APIHelper mLinkedInApiHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNameTextView = (TextView) findViewById(R.id.name);

        // Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                          .requestEmail()
                                          .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                                   .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                                   .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                                   .build();
        mGoogleSignInButton = (Button) findViewById(R.id.sign_in_button_google);
        mGoogleSignInButton.setOnClickListener(this);


        // Twitter
        mTwitterAuthClient = new TwitterAuthClient();
        mTwitterSignInButton = (Button) findViewById(R.id.sign_in_button_twitter);
        mTwitterSignInButton.setOnClickListener(this);

        //Facebook
        mFacebookSignInButton = (Button) findViewById(R.id.sign_in_button_facebook);
        mFacebookSignInButton.setOnClickListener(this);
        findViewById(R.id.sign_in_button_facebook).setOnClickListener(this);
        mFacebookCallbackManager = CallbackManager.Factory.create();
        mFacebookLoginManager = LoginManager.getInstance();
        mFacebookLoginManager.registerCallback(mFacebookCallbackManager, this);

        //LinkedIn
        mLinkedInSignInButton = (Button) findViewById(R.id.sign_in_button_linkedin);
        mLinkedInSignInButton.setOnClickListener(this);
        mLISessionManager = LISessionManager.getInstance(this);
        mLinkedInApiHelper = APIHelper.getInstance(this);
    }

    // Build the list of member permissions our LinkedIn session requires
    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.W_SHARE);
    }

    @Override
    public void onSuccess(final LoginResult loginResult) {
        mNameTextView.setText(Profile.getCurrentProfile().getName());
        mFacebookSignInButton.setText("Logout from Facebook");
        PreferenceUtils.set(this, PreferenceUtils.HAS_FACEBOOK_SIGN_IN, true);
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onError(final FacebookException error) {
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.sign_in_button_facebook:
                if (PreferenceUtils.getBooleanPrefs(this, PreferenceUtils.HAS_FACEBOOK_SIGN_IN)) {
                    mFacebookLoginManager.logOut();
                    PreferenceUtils.set(this, PreferenceUtils.HAS_FACEBOOK_SIGN_IN, false);
                    mFacebookSignInButton.setText("Login with Facebook");
                } else {
                    mFacebookLoginManager.logInWithReadPermissions(this, Arrays.asList(FACEBOOK_SCOPE_PUBLIC_PROFILE));
                }
                break;
            case R.id.sign_in_button_google:
                if (PreferenceUtils.getBooleanPrefs(this, PreferenceUtils.HAS_GOOGLE_SIGN_IN)) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                    PreferenceUtils.set(this, PreferenceUtils.HAS_GOOGLE_SIGN_IN, false);
                    mGoogleSignInButton.setText("Login with Google");
                } else {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
                break;
            case R.id.sign_in_button_twitter:
                if (PreferenceUtils.getBooleanPrefs(this, PreferenceUtils.HAS_TWITTER_SIGN_IN)) {
                    TwitterCore.getInstance().logOut();
                    PreferenceUtils.set(this, PreferenceUtils.HAS_TWITTER_SIGN_IN, false);
                    mTwitterSignInButton.setText("Login with Twitter");
                } else {
                    mTwitterAuthClient.authorize(this, mCallbackTwitterSession);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Signed in successfully, show authenticated UI.
                GoogleSignInAccount googleSignInAccount = result.getSignInAccount();
                mNameTextView.setText(googleSignInAccount.getDisplayName());
                mGoogleSignInButton.setText("Logout from Google");
                PreferenceUtils.set(this, PreferenceUtils.HAS_GOOGLE_SIGN_IN, true);
            } else {
                // Signed out, show unauthenticated UI.
            }
        }
    }

    @Override
    public void onConnected(@Nullable final Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(final int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {

    }

    @Override
    public void onAuthSuccess() {
        String url = "https://api.linkedin.com/v1/people/~";

        mLinkedInApiHelper.getRequest(MainActivity.this, url, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse apiResponse) {
                mNameTextView.setText(apiResponse.getResponseDataAsJson().toString());
                mGoogleSignInButton.setText("Logout from LinkedIn");
                PreferenceUtils.set(MainActivity.this, PreferenceUtils.HAS_LINKEDIN_SIGN_IN, true);
            }

            @Override
            public void onApiError(LIApiError liApiError) {
                // Error making GET request!
            }
        });
    }

    @Override
    public void onAuthError(final LIAuthError error) {

    }
}
