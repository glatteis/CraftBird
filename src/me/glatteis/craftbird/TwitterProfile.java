package me.glatteis.craftbird;

import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterStream;
import twitter4j.auth.AccessToken;

/**
 * Created by Linus on 21.03.2016.
 */
public class TwitterProfile {

    private Twitter twitter;
    private StatusListener listener;
    private TwitterStream stream;
    private AccessToken accessToken;
    private Long pendingReply;
    private Long pendingQuote;

    public TwitterProfile(Twitter twitter, AccessToken accessToken) {
        this.accessToken = accessToken;
        this.twitter = twitter;
    }

    public Long getPendingQuote() {
        return pendingQuote;
    }

    public void setPendingQuote(Long pendingQuote) {
        this.pendingQuote = pendingQuote;
    }

    public Long getPendingReply() {
        return pendingReply;
    }

    public void setPendingReply(Long pendingReply) {
        this.pendingReply = pendingReply;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public TwitterStream getStream() {

        return stream;
    }

    public void setStream(TwitterStream stream) {
        this.stream = stream;
    }

    public Twitter getTwitter() {

        return twitter;
    }

    public StatusListener getListener() {
        return listener;
    }

    public void setListener(StatusListener listener) {
        this.listener = listener;
    }
}
