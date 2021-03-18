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

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GotoCommand implements SimpleCommand {

    private final Violet violet;
    private final VBank vBank;

    public GotoCommand(Violet violet) {
        this.violet = violet;
        this.vBank = violet.getVBank();

        CommandManager commandManager = violet.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("goto").build();
        commandManager.register(commandMeta, this);
    }

    @Override
    public void execute(Invocation invocation) {
        VPlayer vPlayer = vBank.get(invocation.source());
        if (vPlayer == null) {
            return;
        }

        String[] args = invocation.arguments();
        if (args.length != 1) {
            vPlayer.writeUsage("goto", "goto <player>");
            return;
        }

        VPlayer target = vBank.get(args[0]);
        if (target == null || !target.isOnline()) {
            vPlayer.write("goto", "Il player non e' online", MessageType.ERROR);
            return;
        }

        if (target == vPlayer) {
            vPlayer.write("goto", "Ma che vuoi fa?", MessageType.ERROR);
            return;
        }

        Player player = StaticUtils.findPlayer(vPlayer);
        if (player == null) {
            return;
        }

        String server = StaticUtils.getServerName(args[0]);
        RegisteredServer registeredServer = StaticUtils.findServer(server);
        if (registeredServer == null) {
            return;
        }

        vPlayer.write("goto", "Connessione a: " + server, MessageType.INFO);
        player.createConnectionRequest(StaticUtils.findServer(server)).connect();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {

            String[] args = invocation.arguments();

            if (args.length <= 1) {
                return StaticUtils.playerInGlobalTabComplete(args[0]);
            }

            return null;
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("heros.proxy.goto");
    }

}
