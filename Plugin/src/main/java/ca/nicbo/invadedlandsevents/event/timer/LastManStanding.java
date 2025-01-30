package ca.nicbo.invadedlandsevents.event.timer;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostEndEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStartEvent;
import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;
import ca.nicbo.invadedlandsevents.configuration.Message;
import ca.nicbo.invadedlandsevents.event.event.player.EventPlayerDamageByEventPlayerEvent;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboardLine;
import ca.nicbo.invadedlandsevents.task.event.MatchCountdownTask;
import ca.nicbo.invadedlandsevents.util.SkinUtil;
import ca.nicbo.invadedlandsevents.util.SpigotUtils;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import ca.nicbo.invadedlandsevents.util.TabHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;
import java.util.UUID;

/**
 * Last Man Standing.
 *
 * @author Nicbo
 */
public class LastManStanding extends TimerEvent {
    private final Kit kit;
    private final Location startOne;
    private final Location startTwo;
    private final MatchCountdownTask matchCountdownTask;

    public LastManStanding(InvadedLandsEventsPlugin plugin, String hostName) {
        super(plugin, EventType.LAST_MAN_STANDING, hostName, ListMessage.LMS_DESCRIPTION.get());
        this.kit = getEventConfig().getKit("kit");
        this.startOne = getEventConfig().getLocation("start-1");
        this.startTwo = getEventConfig().getLocation("start-2");
        this.matchCountdownTask = new MatchCountdownTask.Builder(this::broadcastMessage)
                .setStarting(Message.LMS_MATCH_STARTING.get())
                .setCounter(Message.LMS_MATCH_COUNTER.get())
                .setStarted(Message.LMS_MATCH_STARTED.get())
                .build();
    }

    @Override
    protected EventScoreboard createEventScoreboard(Player player) {
        return new LastManStandingScoreboard(player);
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEventPlayerDamageByEventPlayer(EventPlayerDamageByEventPlayerEvent event) {
        super.onEventPlayerDamageByEventPlayer(event);

        if (matchCountdownTask.isRunning()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void OnPlayerDeath(PlayerDeathEvent event) {
        Player eliminated = event.getEntity();
        if (isPlayerParticipating(eliminated) && isPlayerParticipating(eliminated.getKiller())) {
            event.setCancelled(true);
            broadcastMessage(Message.LMS_ELIMINATED.get()
                    .replace("{player}", eliminated.getName())
                    .replace("{remaining}", String.valueOf(getPlayersSize() - 1)));
            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            lose(eliminated);
            SkinUtil.setSkinFromUUID(eliminated, eliminated.getUniqueId());
            TabHook.setNameTag(eliminated, true);}
            , 10L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostStart(EventPostStartEvent event) {
        super.onEventPostStart(event);

        List<Player> players = getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            player.teleport(i % 2 == 0 ? startOne : startTwo);
            kit.apply(player);
            SkinUtil.setSkinFromUUID(player, UUID.fromString("356b64ff-90e7-4b32-beb6-375c46cb5756"));
            TabHook.setNameTag(player, false);
        }

        matchCountdownTask.start(getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostEnd(EventPostEndEvent event) {
        super.onEventPostEnd(event);
        if (matchCountdownTask.isRunning()) {
            matchCountdownTask.stop();
        }
    }

    private class LastManStandingScoreboard extends EventScoreboard {
        private final EventScoreboardLine playerCountLine;
        private final EventScoreboardLine spectatorCountLine;
        private final EventScoreboardLine timeRemainingLine;

        public LastManStandingScoreboard(Player player) {
            super(player, Message.TITLE_LMS.get(), getConfigName());
            this.playerCountLine = new EventScoreboardLine(4);
            this.spectatorCountLine = new EventScoreboardLine(3);
            this.timeRemainingLine = new EventScoreboardLine(2);
            this.setLines(playerCountLine, spectatorCountLine, timeRemainingLine);
        }

        @Override
        protected void refresh() {
            playerCountLine.setText("&ePlayers: &6" + getPlayersSize());
            spectatorCountLine.setText("&eSpectators: &6" + getSpectatorsSize());
            timeRemainingLine.setText("&eTime Remaining: &6" + StringUtils.formatSeconds(getTimeLeft()));
        }
    }
}
