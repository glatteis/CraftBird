package me.glatteis.craftbird;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;

/**
 * Created by Linus on 21.03.2016.
 */
public class TwitterListener implements Listener {

    private CraftBird main;

    public TwitterListener(CraftBird main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.getMessage().startsWith("@t")) {
            event.setCancelled(true);
            if (!main.getTwitters().containsKey(event.getPlayer())) return;
            try {
                main.getTwitters().get(event.getPlayer()).getTwitter().updateStatus(event.getMessage().substring(2));
                event.getPlayer().sendMessage(ChatColor.BLUE + "Tweeted!");
            } catch (Exception e) {
                event.getPlayer().sendMessage(ChatColor.RED + "Could not be tweeted: " + e.getMessage());
            }
        }
        else if (event.getMessage().startsWith("@r")) {
            event.setCancelled(true);
            if (!main.getTwitters().containsKey(event.getPlayer()) ||
                    main.getTwitters().get(event.getPlayer()).getPendingReply() == null) return;
            try {
                Twitter twitter =  main.getTwitters().get(event.getPlayer()).getTwitter();
                long pendingReply = main.getTwitters().get(event.getPlayer()).getPendingReply();
                String users = "";
                Status s = twitter.showStatus(pendingReply);
                users += "@" + s.getUser().getScreenName() + " ";
                for (String word : s.getText().split(" ")) {
                    if (word.startsWith("@") && !word.equalsIgnoreCase("@" + twitter.getScreenName())) {
                        users += word + " ";
                    }
                }
                StatusUpdate update = new StatusUpdate(users + event.getMessage().substring(3));
                update.setInReplyToStatusId(pendingReply);
                twitter.updateStatus(update);
                event.getPlayer().sendMessage(ChatColor.BLUE + "Replied!");
                main.getTwitters().get(event.getPlayer()).setPendingReply(null);
            } catch (Exception e) {
                event.getPlayer().sendMessage(ChatColor.RED + "Could not be tweeted: " + e.getMessage());
            }
        }
        else if (event.getMessage().startsWith("@q")) {
            event.setCancelled(true);
            if (!main.getTwitters().containsKey(event.getPlayer()) ||
                    main.getTwitters().get(event.getPlayer()).getPendingQuote() == null) return;
            try {
                Twitter twitter =  main.getTwitters().get(event.getPlayer()).getTwitter();
                long pendingQuote = main.getTwitters().get(event.getPlayer()).getPendingQuote();
                Status quoteStatus = twitter.showStatus(pendingQuote);
                twitter.updateStatus(event.getMessage().substring(3) + " https://twitter.com/" +
                    quoteStatus.getUser().getScreenName() + "/status/" + pendingQuote + "/"
                );
                main.getTwitters().get(event.getPlayer()).setPendingQuote(null);
            } catch (Exception e) {
                event.getPlayer().sendMessage(ChatColor.RED + "Could not be tweeted: " + e.getMessage());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        for (Player p : main.getTwitters().keySet()) {
            if (p.equals(event.getPlayer())) {
                main.getStreamingUtils().removeStream(main.getTwitters().get(p));
                return;
            }
        }
    }

}
