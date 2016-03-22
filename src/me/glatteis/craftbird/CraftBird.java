package me.glatteis.craftbird;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.util.HashMap;

/**
 * Created by Linus on 21.03.2016.
 */
public class CraftBird extends JavaPlugin {

    private static final B b = new B();
    public static final String a = b.ab();
    public static final String c = b.bc();
    private StreamingUtils streamingUtils = new StreamingUtils();
    private HashMap<Player, TwitterProfile> twitters = new HashMap<Player, TwitterProfile>();
    private HashMap<Player, Twitter> authenticating = new HashMap<Player, Twitter>();
    private TwitterListener twitterListener = new TwitterListener(this);

    public HashMap<Player, TwitterProfile> getTwitters() {
        return twitters;
    }

    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(twitterListener, this);
    }

    public void onDisable() {
        for (TwitterProfile profile : twitters.values()) {
            streamingUtils.removeStream(profile);
        }
    }

    public StreamingUtils getStreamingUtils() {
        return streamingUtils;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals("test")) {
            //Unit tests
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("Usage:");
            if (twitters.get(sender) == null) sender.sendMessage("/twitter login");
            else {
                sender.sendMessage("/twitter enable|disable");
                sender.sendMessage("@t [tweet]");
                sender.sendMessage("Advanced commands: /twitter advanced");
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("advanced")) {
            sender.sendMessage("These are advanced commands.");
            sender.sendMessage("They are mainly meant to be executed if a user clicks something.");
            sender.sendMessage("/twitter like|rt|quote [statusID]");
            sender.sendMessage("/twitter reply [statusID]; then @r [reply]");
            sender.sendMessage("/twitter viewprofile [userID]");
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("You have to be a player to do this!");
            return true;
        }
        if (args[0].equalsIgnoreCase("login")) {
            if (authenticating.containsKey(sender) && args.length == 2) {
                Twitter twitter = authenticating.get(sender);
                try {
                    AccessToken token = twitter.getOAuthAccessToken(args[1]);
                    twitter.setOAuthAccessToken(token);
                    sender.sendMessage("Authenticated.");
                    authenticating.remove(sender);
                    TwitterProfile profile = new TwitterProfile(twitter, token);
                    twitters.put((Player) sender, profile);
                    streamingUtils.addStream((Player) sender, profile);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return true;
            }
            if (twitters.containsKey(sender)) {
                sender.sendMessage("/twitter");
                return true;
            }
            Twitter newTwitter = new TwitterFactory().getInstance();
            newTwitter.setOAuthConsumer(a, c);
            try {
                RequestToken token = newTwitter.getOAuthRequestToken();
                sender.sendMessage(ChatColor.DARK_GRAY + token.getAuthenticationURL());
                sender.sendMessage(ChatColor.BLUE + "->" + ChatColor.WHITE + "/twitter login <PIN>");
                authenticating.put((Player) sender, newTwitter);
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Failed: " + e.getMessage());
            }
        } else if (args[0].equalsIgnoreCase("like") || args[0].equalsIgnoreCase("rt") ||
                args[0].equalsIgnoreCase("reply") | args[0].equalsIgnoreCase("quote")) {
            if (args.length == 1) {
                sender.sendMessage("/twitter like|rt|reply|quote [statusID]");
                return true;
            }
            String id = args[1];
            TwitterProfile profile = twitters.get(sender);
            if (profile == null) return false;
            if (args[0].equalsIgnoreCase("rt")) {
                try {
                    profile.getTwitter().retweetStatus(Long.valueOf(id));
                    sender.sendMessage(ChatColor.GREEN + "Retweeted tweet!");
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Failed: " + e.getMessage());
                }
            } else if (args[0].equalsIgnoreCase("like")) {
                try {
                    profile.getTwitter().createFavorite(Long.valueOf(id));
                    sender.sendMessage(ChatColor.RED + "Liked tweet!");
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Failed: " + e.getMessage());
                }
            } else if (args[0].equalsIgnoreCase("reply")) {
                profile.setPendingReply(Long.valueOf(id));
                sender.sendMessage(ChatColor.BLUE + "Reply with " + ChatColor.WHITE + "@r");
            } else if (args[0].equalsIgnoreCase("quote")) {
                profile.setPendingQuote(Long.valueOf(id));
                sender.sendMessage(ChatColor.BLUE + "Quote with " + ChatColor.WHITE + "@q");
            }
        } else if (args[0].equalsIgnoreCase("viewprofile")) {
            if (args.length == 1) {
                sender.sendMessage("/twitter viewprofile [userID]");
                return true;
            }
            long userID = Long.valueOf(args[1]);
            TwitterProfile profile = twitters.get(sender);
            if (profile == null) return false;
            try {
                User user = profile.getTwitter().showUser(userID);
                sender.sendMessage(ChatColor.BLUE + user.getName() + " " + ChatColor.WHITE + user.getScreenName());
                sender.sendMessage(user.getDescription());
                if (user.getLocation() != null) sender.sendMessage(user.getLocation());
                if (user.getURLEntity() != null) sender.sendMessage(user.getURLEntity().getExpandedURL());
                sender.sendMessage(
                        ChatColor.DARK_AQUA.toString() + user.getStatusesCount() + ChatColor.BLUE.toString() + " Tweets " +
                        ChatColor.DARK_AQUA.toString() + user.getFollowersCount() + ChatColor.BLUE.toString() + " Followers " +
                        ChatColor.DARK_AQUA.toString() + user.getFriendsCount() + ChatColor.BLUE.toString() + " Following "
                );
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Failed: " + e.getMessage());
            }
        } else if (args[0].equalsIgnoreCase("enable")) {
            try {
                if (twitters.get(sender) == null) return false;
                streamingUtils.addStream((Player) sender, twitters.get(sender));
                sender.sendMessage("Enabled Twitter!");
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Failed: " + e.getMessage());
            }
        } else if (args[0].equalsIgnoreCase("disable")) {
            if (twitters.get(sender) == null) return false;
            streamingUtils.removeStream(twitters.get(sender));
            twitters.get(sender).setStream(null);
            sender.sendMessage("Disabled Twitter!");
        }
        return true;
    }


}
