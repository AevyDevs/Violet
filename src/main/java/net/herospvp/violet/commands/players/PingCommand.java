package net.herospvp.violet.commands.players;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.herospvp.violet.Violet;
import net.herospvp.violet.core.VPlayer;
import net.herospvp.violet.elements.MessageType;
import net.herospvp.violet.utils.StaticUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PingCommand implements SimpleCommand {

    private final Violet violet;

    public PingCommand(Violet violet) {
        this.violet = violet;

        CommandManager commandManager = violet.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("ping").aliases("latency").build();
        commandManager.register(commandMeta, this);
    }

    @Override
    public void execute(Invocation invocation) {
        VPlayer vPlayer = violet.getVBank().get(invocation.source());
        if (vPlayer == null) {
            return;
        }

        long latency;
        String[] strings = invocation.arguments();
        if (strings.length != 1) {
            Player player = StaticUtils.findPlayer(vPlayer);
            if (player == null) {
                return;
            }
            latency = player.getPing();
            vPlayer.write("ping", "La tua latenza (Client<->Proxy) e' pari a: " + latency + "ms", MessageType.INFO);
            vPlayer.write("ping", "La latenza media (Proxy<->Server) e' circa: 6ms", MessageType.INFO);
            vPlayer.write("ping", "La latenza finale (Client<->Proxy<->Server) e' circa: " + (latency + 6) + "ms", MessageType.INFO);
            return;
        }

        Player player = StaticUtils.findPlayer(strings[0]);
        if (player == null) {
            vPlayer.write("ping", "Il player non e' online", MessageType.ERROR);
            return;
        }

        latency = player.getPing();
        vPlayer.write("ping", "La latenza di " + player.getUsername() + " (Client<->Proxy) e' pari a: " + latency + "ms", MessageType.INFO);
        vPlayer.write("ping", "La latenza media (Proxy<->Server) e' circa: 6ms", MessageType.INFO);
        vPlayer.write("ping", "La latenza finale (Client<->Proxy<->Server) e' circa: " + (latency + 6) + "ms", MessageType.INFO);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {

            String[] args = invocation.arguments();

            if (args.length <= 1) {
                return StaticUtils.playerInCurrentServerTabComplete(invocation.source(), args[0]);
            }

            return null;
        });
    }

}
