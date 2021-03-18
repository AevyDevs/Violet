package net.herospvp.violet.commands.staffer;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.herospvp.violet.Violet;
import net.herospvp.violet.core.VBank;
import net.herospvp.violet.core.VPlayer;
import net.herospvp.violet.elements.MessageType;
import net.herospvp.violet.utils.StaticUtils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SendCommand implements SimpleCommand {

    private final Violet violet;
    private final VBank vBank;

    public SendCommand(Violet violet) {
        this.violet = violet;
        this.vBank = violet.getVBank();

        CommandManager commandManager = violet.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("send").build();
        commandManager.register(commandMeta, this);
    }

    @Override
    public void execute(Invocation invocation) {
        VPlayer vPlayer = vBank.get(invocation.source());
        if (vPlayer == null) {
            return;
        }

        String[] strings = invocation.arguments();
        if (strings.length != 2) {
            vPlayer.writeUsage("send", "send <player/*> <server>");
            return;
        }

        String server = strings[1];
        RegisteredServer registeredServer = StaticUtils.findServer(server);
        if (registeredServer == null) {
            vPlayer.write("send", "Il server non esiste", MessageType.ERROR);
            return;
        }

        if (strings[0].equals("*")) {
            Collection<Player> playerCollection = StaticUtils.onlinePlayersAsCollection(vPlayer);
            if (playerCollection == null || playerCollection.isEmpty()) {
                vPlayer.write("send", "Si e' verificato un errore...", MessageType.ERROR);
                return;
            }

            playerCollection.forEach(p -> {
                VPlayer temp = vBank.get(p);
                if (temp == vPlayer || !temp.isAuthenticated()) {
                    return;
                }
                p.createConnectionRequest(registeredServer).connect();
                temp.write("send", "Connessione a: " + server, MessageType.INFO);
            });
            vPlayer.write("send", "I player (tu escluso) sono stati connessi", MessageType.INFO);
            return;
        }

        VPlayer target = vBank.get(strings[0]);
        Player player = StaticUtils.findPlayer(strings[0]);
        if (target == null || player == null || !target.isAuthenticated()) {
            vPlayer.write("send", "Il player non e' online / non e' autenticato", MessageType.ERROR);
            return;
        }

        player.createConnectionRequest(registeredServer).connect();
        vPlayer.write("send", "Connettendo " + player.getUsername() + " a: " + server, MessageType.INFO);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {

            String[] args = invocation.arguments();

            if (invocation.arguments().length <= 1) {
                return StaticUtils.playerInCurrentServerTabComplete(invocation.source(), args[0]);
            }

            if (invocation.arguments().length == 2) {
                return StaticUtils.argsTabComplete(StaticUtils.serversAsList(), args[1]);
            }

            return null;
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("heros.proxy.send");
    }

}
