package me.glatteis.craftbird;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twitter4j.*;

/**
 * Created by Linus on 21.03.2016.
 */
public class StreamingUtils {

    public void addStream(final Player player, final TwitterProfile profile) throws TwitterException {
        if (profile.getStream() != null) return;
        TwitterStreamFactory factory = new TwitterStreamFactory();
        profile.setListener(new UserStreamListener() {
            @Override
            public void onDeletionNotice(long l, long l1) {

            }

            @Override
            public void onFriendList(long[] longs) {

            }

            @Override
            public void onFavorite(User user, User user1, Status status) {
                player.sendMessage(ChatColor.BLUE + user.getScreenName() + " liked your tweet:");
                player.sendMessage(status.getText());
                player.sendMessage("");
            }

            @Override
            public void onUnfavorite(User user, User user1, Status status) {

            }

            @Override
            public void onFollow(User user, User user1) {
                try {
                    if (user.getScreenName().equals(profile.getTwitter().getScreenName())) return;
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                player.sendMessage(ChatColor.BLUE + user.getScreenName() + " followed you!");
                player.sendMessage("");
            }

            @Override
            public void onUnfollow(User user, User user1) {
                try {
                    if (user.getScreenName().equals(profile.getTwitter().getScreenName())) return;
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                player.sendMessage(ChatColor.BLUE + user.getScreenName() + " unfollowed you :(");
                player.sendMessage("");
            }

            @Override
            public void onDirectMessage(DirectMessage directMessage) {}

            @Override
            public void onUserListMemberAddition(User user, User user1, UserList userList) {

            }

            @Override
            public void onUserListMemberDeletion(User user, User user1, UserList userList) {

            }

            @Override
            public void onUserListSubscription(User user, User user1, UserList userList) {

            }

            @Override
            public void onUserListUnsubscription(User user, User user1, UserList userList) {

            }

            @Override
            public void onUserListCreation(User user, UserList userList) {

            }

            @Override
            public void onUserListUpdate(User user, UserList userList) {

            }

            @Override
            public void onUserListDeletion(User user, UserList userList) {

            }

            @Override
            public void onUserProfileUpdate(User user) {

            }

            @Override
            public void onUserSuspension(long l) {

            }

            @Override
            public void onUserDeletion(long l) {

            }

            @Override
            public void onBlock(User user, User user1) {

            }

            @Override
            public void onUnblock(User user, User user1) {

            }

            @Override
            public void onRetweetedRetweet(User user, User user1, Status status) {
                try {
                    if (user.getScreenName().equals(profile.getTwitter().getScreenName())) return;
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                player.sendMessage(ChatColor.BLUE + user.getScreenName() + " has retweeted your retweet:");
                player.sendMessage(status.getText());
                player.sendMessage("");
            }

            @Override
            public void onFavoritedRetweet(User user, User user1, Status status) {
                try {
                    if (user.getScreenName().equals(profile.getTwitter().getScreenName())) return;
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                player.sendMessage(ChatColor.BLUE + user.getScreenName() + " has liked your retweet:");
                player.sendMessage(status.getText());
                player.sendMessage("");
            }

            @Override
            public void onQuotedTweet(User user, User user1, Status status) {
                player.sendMessage(ChatColor.BLUE + user.getScreenName() + " has quoted your tweet:");
                player.sendMessage(status.getText());
                player.sendMessage("");
            }

            @Override
            public void onStatus(Status status) {
                while (status.isRetweet()) {
                    sendJsonMesage(new String[][]{
                            new String[] {ChatColor.GREEN + "↹" + ChatColor.GRAY + status.getUser().getName()},
                            new String[] {ChatColor.GRAY + " @" + status.getUser().getScreenName(), "/twitter viewprofile " +
                                    status.getUser().getId(), "View this user's profile"},
                    },player);
                    status = status.getRetweetedStatus();
                }

                sendJsonMesage(new String[][]{
                        new String[] {ChatColor.BLUE + status.getUser().getName()},
                        new String[] {ChatColor.GRAY + " @" + status.getUser().getScreenName(), "/twitter viewprofile " +
                                status.getUser().getId(), "View this user's profile"}
                },player);
                String text = status.getText();
                for (URLEntity entity : status.getURLEntities()) {
                    text = text.replace(entity.getURL(), entity.getExpandedURL());
                }
                for (URLEntity entity : status.getURLEntities()) {
                    if (entity.getExpandedURL().contains("twitter.com/") && entity.getExpandedURL().contains("/status/")) {
                        player.sendMessage(ChatColor.GRAY + "   ”” Quoted");
                        try {
                            onStatus(profile.getTwitter().showStatus(Long.valueOf(entity.getExpandedURL().split("/")[5])));
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }
                        player.sendMessage(ChatColor.GRAY + "   ””");
                        text = text.replace(entity.getExpandedURL(), "");
                    }
                }
                player.sendMessage(text);
                sendJsonMesage(new String[][] {
                        new String[] {ChatColor.RED + "♥ ", "/twitter like " + status.getId(), "Like this tweet"},
                        new String[] {ChatColor.GREEN+ "↹ ", "/twitter rt " + status.getId(), "Retweet this tweet"},
                        new String[] {ChatColor.BLUE + "↜ ", "/twitter reply " + status.getId(), "Reply to this tweet"},
                        new String[] {ChatColor.BLUE + "”", "/twitter quote " + status.getId(), "Quote this tweet"}

                }, player);
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            @Override
            public void onTrackLimitationNotice(int i) {

            }

            @Override
            public void onScrubGeo(long l, long l1) {

            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {

            }

            @Override
            public void onException(Exception e) {

            }
        });
        TwitterStream stream = factory.getInstance(profile.getTwitter().getAuthorization());
        stream.addListener(profile.getListener());
        stream.user();
        profile.setStream(stream);
    }

    public void removeStream(TwitterProfile profile) {
        if (profile.getStream() != null) {
            profile.getStream().cleanUp();
            profile.getStream().clearListeners();
        }
        profile.setListener(null);
    }

    public void sendJsonMesage(String[][] messagesAndCommands, CommandSender sender) {
        try {
            Object iChatBaseComponent = getNMSClass("ChatMessage").getConstructor(String.class, Object[].class).newInstance("", new Object[0]);
            for (String[] messages : messagesAndCommands) {
                if (messages.length == 1) {
                    Object normalText = getNMSClass("ChatMessage").getConstructor(String.class, Object[].class).newInstance(messages[0], new Object[0]);
                    iChatBaseComponent.getClass().getMethod("addSibling", getNMSClass("IChatBaseComponent")).invoke(iChatBaseComponent, normalText);
                    continue;
                }
                String message = "[\"\",{\"text\":\"" + messages[0] + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + messages[1] + "\"}," +
                        "\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + messages[2] + "\"}]}}}]";
                Object component = getNMSClass("IChatBaseComponent$ChatSerializer").getDeclaredMethod("a", String.class).invoke(null, message);
                iChatBaseComponent.getClass().getMethod("addSibling", getNMSClass("IChatBaseComponent")).invoke(iChatBaseComponent, component);
            }
            Object packet = getNMSClass("PacketPlayOutChat").getConstructor(getNMSClass("IChatBaseComponent")).newInstance(iChatBaseComponent);
            Class<?> cpClass = getCraftClass("entity.CraftPlayer");
            Object craftPlayer = cpClass.cast(sender);
            Object player = craftPlayer.getClass().getMethod("getHandle").invoke(craftPlayer);
            Object connection = player.getClass().getField("playerConnection").get(player);
            connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private Class<?> getNMSClass(String className) {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.split("\\.")[3];
        String path = "net.minecraft.server." + version + "." + className;
        try {
            return Class.forName(path);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Class<?> getCraftClass(String className) {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.split("\\.")[3];
        String path = "org.bukkit.craftbukkit." + version + "." + className;
        try {
            return Class.forName(path);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


}
