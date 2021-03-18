package net.herospvp.violet.events;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import net.herospvp.violet.Violet;
import net.herospvp.violet.core.VBank;
import net.herospvp.violet.core.VPlayer;
import net.herospvp.violet.core.VStaffer;
import net.herospvp.violet.elements.MessageType;
import net.herospvp.violet.elements.Rank;
import net.herospvp.violet.utils.StaticUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatHandler {

    private final VBank vBank;

    private final Map<Player, Long> commandTimings, messageTimings;
    private final List<String> whitelistedCommands;

    private final List<String> blacklistedWords;

    public ChatHandler(Violet violet, List<String> words) {
        this.vBank = violet.getVBank();
        this.commandTimings = new HashMap<>();
        this.messageTimings = new HashMap<>();
        this.blacklistedWords = words;
        this.whitelistedCommands = Arrays.asList("login", "l", "register", "reg");
        violet.getServer().getEventManager().register(violet, this);
    }

    @Subscribe (order = PostOrder.LAST)
    public void on(PostLoginEvent event) {
        messageTimings.put(event.getPlayer(), 0L);
        commandTimings.put(event.getPlayer(), 0L);
    }

    @Subscribe (order = PostOrder.LAST)
    public void on(DisconnectEvent event) {
        messageTimings.remove(event.getPlayer());
        commandTimings.remove(event.getPlayer());
    }

    @Subscribe (order = PostOrder.FIRST)
    public void on(CommandExecuteEvent event) {
        CommandSource commandSource = event.getCommandSource();
        if (!(commandSource instanceof Player)) {
            return;
        }
        Player player = (Player) commandSource;
        VPlayer vPlayer = vBank.get(player);

        if (vPlayer == null || !vPlayer.isAuthenticated()) {
            String[] split = event.getCommand().split(" ");
            if (whitelistedCommands.contains(split[0].toLowerCase())) {
                return;
            }
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }

        long time = System.currentTimeMillis();

        if (commandTimings.get(player) + 500 > time) {
            if (vPlayer.isStaffer()) {
                VStaffer vStaffer = vBank.getStaffer(vPlayer);
                if (!vStaffer.getRank().equals(Rank.HELPER)) {
                    return;
                }
            }
            vPlayer.write("proxy", "Puoi eseguire 1 comando ogni 0.5 secondi", MessageType.ERROR);
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }
        commandTimings.replace(player, time);
    }

    @Subscribe (order = PostOrder.FIRST)
    public void on(PlayerChatEvent event) {
        Player player = event.getPlayer();
        VPlayer vPlayer = vBank.get(player);

        if (vPlayer == null || !vPlayer.isAuthenticated()) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }

        VStaffer vStaffer = vBank.getStaffer(vPlayer);
        if (vStaffer != null) {
            if (!(vStaffer.isStaffChat() && vStaffer.isSeeStaffChat())) {
                return;
            }
            vBank.getVStaffers().parallelStream()
                    .filter(
                            VStaffer::isSeeStaffChat
                    ).forEach(p -> p.write(
                    Component.text("[" + StaticUtils.getServerName(vPlayer) + "] ").color(NamedTextColor.DARK_GREEN)
                    .append(Component.text(vPlayer.getName()).color(NamedTextColor.GREEN))
                    .append(Component.text(" » ").color(NamedTextColor.GRAY))
                    .append(Component.text(event.getMessage()).color(NamedTextColor.WHITE)))
            );
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }

        long time = System.currentTimeMillis();

        if (messageTimings.get(player) + 1500 > time) {
            vPlayer.write("proxy", "Puoi mandare 1 messaggio ogni 1.5 secondi", MessageType.ERROR);
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }
        messageTimings.replace(player, time);

        String string = blacklistedWords.parallelStream()
                .filter(
                        w -> event.getMessage().contains(w)
                ).findFirst().orElse(null);

        if (string == null) {
            return;
        }

        vPlayer.write("proxy", "Mi spiace, non puoi usare queste parole sul server, " +
                "eludere questa restrizione comporta il mute di 12 ore!", MessageType.WARNING);
        event.setResult(PlayerChatEvent.ChatResult.denied());
    }

}
