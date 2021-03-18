package net.herospvp.violet.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.util.UuidUtils;
import net.herospvp.violet.Violet;
import net.herospvp.violet.commands.players.ReportCommand;
import net.herospvp.violet.commands.staffer.ControlloCommand;
import net.herospvp.violet.core.VBank;
import net.herospvp.violet.core.VPlayer;
import net.herospvp.violet.core.VStaffer;
import net.herospvp.violet.elements.Auth;
import net.herospvp.violet.elements.MessageType;
import net.herospvp.violet.elements.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class InputHandler {

    private final Violet violet;
    private final VBank vBank;

    public InputHandler(Violet violet) {
        this.violet = violet;
        this.vBank = violet.getVBank();
        violet.getServer().getEventManager().register(violet, this);
    }

    @Subscribe (order = PostOrder.LAST)
    public void on(PreLoginEvent event) {

        if (!violet.isCanJoin()) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                    Component.text("Il server e' in fase di caricamento, aspetta qualche secondo!").color(NamedTextColor.RED)));
            return;
        }
        String name = event.getUsername();

        if (name.length() < 3 || name.length() > 16) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                    Component.text("Non puoi entrare con questo nome, mi spiace!").color(NamedTextColor.RED)));
        }
        InboundConnection inboundConnection = event.getConnection();
        VPlayer vPlayer = vBank.get(name);

        Optional<InetSocketAddress> from = inboundConnection.getVirtualHost();
        if (!from.isPresent()) {
            return;
        }
        boolean domain = from.get().getHostName().equals("premium.herospvp.net");

        // if null, the player has never joined the server before
        if (vPlayer == null || vPlayer.isUnknown()) {
            // if is joining from premium, deny login
            if (domain) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                        Component.text("Mi spiace, puoi entrare solo da: mc.herospvp.net").color(NamedTextColor.RED)));
            }
            return;
        }

        vPlayer.setAuthenticated(false);

        if (domain) {

            if (vPlayer.isPremium()) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
                violet.getJedisThread().getAuth().offer(vPlayer);
                return;
            }

            if (vPlayer.isCracked()) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                        Component.text("Mi spiace, puoi entrare solo da: mc.herospvp.net").color(NamedTextColor.RED)));
            }

        }
        else {

            if (vPlayer.isPremium()) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                        Component.text("Mi spiace, puoi entrare solo da: premium.herospvp.net").color(NamedTextColor.RED)));
            }

        }

    }

    @Subscribe (order = PostOrder.LAST)
    public void on(GameProfileRequestEvent event) {

        if (!event.isOnlineMode()) {
            return;
        }

        String playerName = event.getUsername();
        UUID uuid = UuidUtils.generateOfflinePlayerUuid(playerName);

        GameProfile gameProfile = new GameProfile(uuid, playerName, event.getOriginalProfile().getProperties());
        event.setGameProfile(gameProfile);
    }

    @Subscribe (order = PostOrder.LAST)
    public void on(PostLoginEvent event) {
        Player player = event.getPlayer();
        VPlayer vPlayer = vBank.get(player);
        String ip = player.getRemoteAddress().getAddress().getHostAddress(), uuid = player.getUniqueId().toString();
        long time = System.currentTimeMillis();

        if (vPlayer == null) {
            vPlayer = new VPlayer(
                    player.getUsername(),
                    ip,
                    uuid,
                    false,
                    player.hasPermission("heros.proxy.staff"),
                    Auth.UNKNOWN,
                    time,
                    0L,
                    0L);
            vBank.push(vPlayer);
            vPlayer.setNeedsInsert(true);
        }
        else {
            if (vPlayer.getUuid() == null) {
                vPlayer.setUuid(uuid);
            }
            if (vPlayer.getIp() == null || !vPlayer.getIp().equals(ip)) {
                vPlayer.setIp(ip);
            }
        }

        if (vPlayer.isBlacklisted()) {
           player.disconnect(Component.text("Sei stato blacklistato!\n\n" +
                   "Per ottenere l'unblacklist, visita: https://shop.herospvp.net/").color(NamedTextColor.RED));
           return;
        }

        vPlayer.setOnline(true);
        vPlayer.setNeedsUpdate(true);
        vPlayer.updateLastJoin();

        ReportCommand.timings.put(vPlayer, 0L);

        if (vPlayer.isUnknown()) {
            vPlayer.write("proxy",
                    "Hey! Ho notato che il tuo account non e' al sicuro!\n" +
                            "Imposta delle restrizioni usando i comandi seguenti:\n" +
                            "Se il tuo account e' premium digita: /premium\n" +
                            "Se il tuo account e' cracked (o SP) digita: /cracked\n", MessageType.WARNING);
        }

        VStaffer vStaffer = vBank.getStaffer(vPlayer);
        Rank rank = player.hasPermission("heros.proxy.rank.admin") ? Rank.ADMIN :
                    player.hasPermission("heros.proxy.rank.mod") ? Rank.MOD :
                    player.hasPermission("heros.proxy.rank.helper") ? Rank.HELPER : Rank.NONE;

        if (vStaffer == null) {
            if (rank == Rank.NONE) {
                return;
            }
            vPlayer.setStaffer(true);
            vStaffer = new VStaffer(vPlayer, rank);
            vStaffer.setNeedsInsert(true);
            vBank.pushStaffer(vStaffer);
        }

        if (!rank.equals(vStaffer.getRank())) {
            if (rank.equals(Rank.NONE)) {
                vStaffer.setNeedsDelete(true);
                vPlayer.setStaffer(false);
                vBank.remove(vStaffer);
                return;
            }
            vStaffer.setRank(rank);
            vStaffer.setNeedsUpdate(true);
        }

        if (!vPlayer.isStaffer()) {
            return;
        }

        VPlayer finalVPlayer = vPlayer;
        violet.getServer().getScheduler().buildTask(violet, () -> {
            if (!finalVPlayer.isOnline()) {
                return;
            }
            vBank.getVStaffers().forEach(
                    p -> p.write("proxy", player.getUsername() + " e' entrato nel server",
                            MessageType.WARNING)
            );
        }).delay(1, TimeUnit.SECONDS).schedule();
    }

    @Subscribe (order = PostOrder.LAST)
    public void on(ServerConnectedEvent event) {
        VPlayer vPlayer = vBank.get(event.getPlayer());
        if (vPlayer == null || !vPlayer.isAuthenticated()) {
            return;
        }

        if (event.getServer().getServerInfo().getName().contains("lobby-")) {
            violet.getJedisThread().getLobby().offer(vPlayer);
        }
    }

    @Subscribe (order = PostOrder.LAST)
    public void on(DisconnectEvent event) {
        Player player = event.getPlayer();
        VPlayer vPlayer = vBank.get(player);

        vPlayer.setAuthenticated(false);
        vPlayer.setOnline(false);
        vPlayer.updateTotalTime();

        ReportCommand.timings.remove(vPlayer);
        ControlloCommand.playersAndStafferInSS.remove(vPlayer);

        if (!vPlayer.isStaffer()) {
            return;
        }

        violet.getServer().getScheduler().buildTask(violet, () -> {
            if (vPlayer.isOnline() || vPlayer.isAuthenticated()) {
                return;
            }
            vBank.getVStaffers().forEach(
                    p -> p.write("proxy", player.getUsername() + " e' uscito dal server",
                            MessageType.WARNING)
            );
        }).delay(1, TimeUnit.SECONDS).schedule();
    }

}
