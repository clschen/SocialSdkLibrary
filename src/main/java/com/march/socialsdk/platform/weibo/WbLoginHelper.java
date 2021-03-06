package com.march.socialsdk.platform.weibo;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.march.socialsdk.exception.SocialException;
import com.march.socialsdk.helper.OtherHelper;
import com.march.socialsdk.helper.GsonHelper;
import com.march.socialsdk.helper.PlatformLog;
import com.march.socialsdk.listener.OnLoginListener;
import com.march.socialsdk.manager.LoginManager;
import com.march.socialsdk.model.LoginResult;
import com.march.socialsdk.model.token.SinaAccessToken;
import com.march.socialsdk.model.user.SinaUser;
import com.march.socialsdk.platform.weibo.extend.UsersAPI;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.march.socialsdk.platform.Target;

/**
 * CreateAt : 2016/12/5
 * Describe : 新浪微博登陆辅助
 *
 * @author chendong
 */

public class WbLoginHelper {

    public static final String TAG = WbLoginHelper.class.getSimpleName();

    private int loginType;
    private OnLoginListener loginListener;
    private Context         context;
    private String          appId;

    public WbLoginHelper(Context context, String appId) {
        this.context = context;
        this.appId = appId;
        this.loginType= Target.LOGIN_WB;
    }

    /**
     * 获取用户信息
     * @param mAccessToken
     */
    private void getUserInfo(final Oauth2AccessToken mAccessToken) {
        //获取用户的信息
        UsersAPI mUsersAPI = new UsersAPI(context,
                appId, mAccessToken);
        mUsersAPI.show(OtherHelper.String2Long(mAccessToken.getUid()), new RequestListener() {
            @Override
            public void onComplete(String response) {
                if (!TextUtils.isEmpty(response)) {
                    PlatformLog.e(TAG,response);
                    SinaUser sinaUser = GsonHelper.getObject(response, SinaUser.class);
                    loginListener.onLoginSucceed(new LoginResult(loginType,sinaUser,new SinaAccessToken(mAccessToken)));
                }
            }

            @Override
            public void onWeiboException(WeiboException e) {
                loginListener.onFailure(new SocialException("sina,获取用户信息失败", e));
            }
        });
    }


    public void login(Activity activity, SsoHandler ssoHandler, final OnLoginListener loginListener) {
        this.loginListener = loginListener;
        if (loginListener == null)
            return;
        WbAuthHelper.auth(activity, ssoHandler, new WbAuthHelper.OnAuthOverListener() {
            @Override
            public void onAuth(Oauth2AccessToken token) {
                getUserInfo(token);
            }

            @Override
            public void onException(SocialException e) {
                loginListener.onFailure(e);
            }

            @Override
            public void onCancel() {
                loginListener.onCancel();
            }
        });
    }
}
