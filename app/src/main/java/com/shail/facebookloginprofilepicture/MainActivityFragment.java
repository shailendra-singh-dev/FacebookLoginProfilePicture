package com.shail.facebookloginprofilepicture;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.itexico.facebookloginprofilepicture.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements FacebookCallback<LoginResult>, View.OnClickListener {

    private static final String TAG = MainActivityFragment.class.getSimpleName();
    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final String LINK = "link";
    private static final String ID = "id";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String AGE_RANGE = "age_range";
    private static final String GENDER = "gender";
    private static final String LOCALE = "locale";

    private CallbackManager mCallbackManager;
    private AccessTokenTracker mAccessTokenTracker;
    private AccessToken mAccessToken;
    private ProfileTracker mProfileTracker;
    private MainActivity mMainActivity;
    private ProfilePictureView mProfilePictureView;
    private ShareDialog mShareDialog;
    private Button mShareButton;
    private Button mDetailsButton;
    private Dialog mDetailsDialog;
    private TextView mDetailsTextView;
    private Profile mCurrentProfile;

    public MainActivityFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        trackAccessTokenChanges();
        trackProfileChanges();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_main, container, false);

        final LoginButton loginButton = (LoginButton) linearLayout.findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(getString(R.string.facebook_permission_public_profile),
                getString(R.string.facebook_permission_email), getString(R.string.facebook_permission_user_friends),
                getString(R.string.facebook_permission_user_about_me), getString(R.string.facebook_permission_user_actions_books)));
        loginButton.setFragment(this);
        mCallbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(mCallbackManager, this);

        mProfilePictureView = (ProfilePictureView) linearLayout.findViewById(R.id.picture);
        mShareDialog = new ShareDialog(this);

        mShareButton = (Button) linearLayout.findViewById(R.id.share);
        mShareButton.setOnClickListener(this);

        mDetailsButton = (Button) linearLayout.findViewById(R.id.details);
        mDetailsButton.setOnClickListener(this);

        mDetailsDialog = new Dialog(getActivity());
        mDetailsDialog.setContentView(R.layout.dialog_details);
        mDetailsDialog.setTitle("Details");
        mDetailsTextView = (TextView) mDetailsDialog.findViewById(R.id.details);
        return linearLayout;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void trackAccessTokenChanges() {
        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                mAccessToken = currentAccessToken;
                // Set the access token using
                // currentAccessToken when it's loaded or set.
            }
        };
        mAccessTokenTracker.startTracking();
        // If the access token is available already assign it.
        mAccessToken = AccessToken.getCurrentAccessToken();
        if (null != mAccessToken && !mAccessToken.isExpired()) {
            logTokenDetails();
        }
    }

    private void trackProfileChanges() {
        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                // App code
                mCurrentProfile = currentProfile;
            }
        };
        mProfileTracker.startTracking();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAccessTokenTracker.stopTracking();
        mProfileTracker.stopTracking();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mAccessToken != null) {
            requestProfileInfo();
        }
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        if (mAccessToken == null || mAccessToken.isExpired()) {
            mAccessToken = loginResult.getAccessToken();
            logTokenDetails();
            requestProfileInfo();
        }
    }

    private void logTokenDetails() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = formatter.format(mAccessToken.getExpires());
        final String info = "AccessToken:" + mAccessToken.getToken() + ",\nExpiryDate:" + date + ",\nUserI+d:" + mAccessToken.getUserId() + ",\naccessToken.isExpired:" + mAccessToken.isExpired();
        Log.i(TAG, info);
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onError(FacebookException error) {

    }

    private void requestProfileInfo() {
        GraphRequest request = GraphRequest.newMeRequest(mAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                JSONObject json = response.getJSONObject();
                Log.i(TAG, "requestProfileInfo json" + json);
                try {
                    if (json != null) {
                        final StringBuilder stringBuilder = new StringBuilder();
                        if (json.has(ID)) {
                            stringBuilder.append(json.getString(ID));
                        } else if (json.has(NAME)) {
                            stringBuilder.append(json.getString(NAME));
                        } else if (json.has(EMAIL)) {
                            stringBuilder.append(json.getString(EMAIL));
                        } else if (json.has(LINK)) {
                            stringBuilder.append(json.getString(LINK));
                        }
                        mDetailsTextView.setText(stringBuilder.toString());
                        mProfilePictureView.setProfileId(json.getString(ID));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        request.setParameters(parameters);
        parameters.putString("fields", ID + "," + NAME + "," + EMAIL + "," + LINK + ","
                + FIRST_NAME + "," + LAST_NAME + "," + AGE_RANGE + "," + GENDER + "," + LOCALE);
        request.executeAsync();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share:
                ShareLinkContent content = new ShareLinkContent.Builder().build();
                mShareDialog.show(content);
                break;

            case R.id.details:
                mDetailsDialog.show();
                break;
        }

    }
}
