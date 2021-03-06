package com.github.niwaniwa.we.core.twitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.niwaniwa.we.core.WhiteEggCore;
import com.github.niwaniwa.we.core.api.callback.Callback;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Twitter関連のクラス
 *
 * @author niwaniwa
 */
public abstract class TwitterManager implements ConfigurationSerializable {

    private static final String consumerKey = WhiteEggCore.getConf().getConfig().getString("setting.twitter.consumerKey"); // app consumer key
    private static final String consumerSecret = WhiteEggCore.getConf().getConfig().getString("setting.twitter.consumerSecret"); // app consumer secret

    private Twitter twitter;

    private AccessToken access = null;
    private RequestToken request = null;
    private final List<Status> tweets = new ArrayList<>();

    public TwitterManager() {
        this.twitter = new TwitterFactory().getInstance();
        this.twitter.setOAuthConsumer(consumerKey, consumerSecret);
    }

    /**
     * RequestTokenの取得
     *
     * @return RequestToken
     */
    public String getOAuthRequestURL() {
        return request != null ? request.getAuthorizationURL() : "nothing yet";
    }

    /**
     * 初期化する
     *
     * @return 成功したか
     */
    public boolean OAuthRequest() {
        try {
            request = twitter.getOAuthRequestToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Twitterに投稿する文字列のチェック
     *
     * @param tweet 投稿する文字列
     * @return 基準以内か(アプリ提携をしているか : 140文字以内)
     */
    public boolean check(String tweet) {
        if (this.access == null) {
            return false;
        }
        return tweet.length() < 140;
    }

    /**
     * 認証
     *
     * @param pin      PIN
     * @param callback 処理終了後に呼び出すインスタンス
     */
    public void OAuthAccess(String pin, Callback... callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Arrays.stream(callback).forEach(c -> c.onTwitter(OAuthAccess(pin)));
                ;
            }
        }.runTaskAsynchronously(WhiteEggCore.getInstance());
        return;
    }

    /**
     * 認証
     *
     * @param pin PIN
     * @return 成功したか
     */
    public boolean OAuthAccess(String pin) {
        if (this.access != null) {
            return false;
        }
        if (this.request == null) {
            OAuthRequest();
        }

        try {
            if (pin.length() > 0) {
                access = twitter.getOAuthAccessToken(request, pin);
            } else {
                access = twitter.getOAuthAccessToken();
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Status updateStatus(StatusUpdate update) {
        Status status = null;
        try {
            status = twitter.updateStatus(update);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return status;
    }

    /**
     * ツイートをします
     *
     * @param tweet ツイート
     */
    public void tweet(String tweet) {
        this.tweet(tweet, null);
    }

    /**
     * ツイートをします
     *
     * @param tweet    ツイート
     * @param callback ツイート後に呼び出す(戻り値はboolean)
     */
    public abstract void tweet(String tweet, Callback callback);

    /**
     * ツイートをします
     *
     * @param tweet ツイート
     */
    public abstract void tweet(String[] tweet);

    /**
     * AccessTokenを取得します
     *
     * @return AccessToken 認証していない場合はnull
     */
    public AccessToken getAccessToken() {
        return access;
    }

    /**
     * RequestTokenを取得します
     *
     * @return RequestToken 初期はnull
     */
    public RequestToken getRequestToken() {
        return request;
    }

    /**
     * AccessTokenを設定します
     *
     * @param access AccessToken
     * @return 成功したか
     */
    public boolean setAccessToken(AccessToken access) {
        if (access == null) {
            return false;
        }
        this.access = access;
        twitter.setOAuthAccessToken(access);
        return true;
    }

    /**
     * 現在登録されている情報を初期化します
     *
     * @return 成功したか
     */
    public boolean reset() {
        this.access = null;
        this.request = null;
        this.twitter = new TwitterFactory().getInstance();
        this.twitter.setOAuthConsumer(consumerKey, consumerSecret);
        this.OAuthRequest();
        return true;
    }

    /**
     * プラグインが有効になってからのツイートを取得します
     *
     * @return List ツイート
     */
    public List<Status> getTweet() {
        return tweets;
    }

    /**
     * Twitterクラスのインスタンスを取得します
     *
     * @return Twitter {@link Twitter}
     */
    public Twitter getTwitter() {
        return twitter;
    }

    /**
     * ツイートの削除(非同期)
     *
     * @param status   削除するツイート
     * @param callback 処理終了後に実行するインスタンス
     */
    public void removeTweet(Status status, Callback... callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Arrays.stream(callback).forEach(c -> c.onTwitter(removeTweet(status)));
                ;
            }
        }.runTaskAsynchronously(WhiteEggCore.getInstance());
    }

    /**
     * ツイートの削除
     *
     * @param status 削除するツイート
     * @return 成功したか
     */
    public boolean removeTweet(Status status) {
        try {
            twitter.destroyStatus(status.getId());
        } catch (TwitterException e) {
            return false;
        }
        return true;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("accesstoken", this.getAccessToken().getToken());
        result.put("accesstokensecret", this.getAccessToken().getTokenSecret());
        return result;
    }

}
