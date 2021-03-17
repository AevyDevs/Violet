package net.herospvp.violet.commands.staffer;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.herospvp.violet.Violet;
import net.herospvp.violet.core.VBank;
import net.herospvp.violet.core.VPlayer;
import net.herospvp.violet.elements.MessageType;
import net.herospvp.violet.utils.StaticUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WhereCommand implements SimpleCommand {

    private final Violet violet;
    private final VBank vBank;

    public WhereCommand(Violet violet) {
        this.violet = violet;
        this.vBank = violet.getVBank();

        CommandManager commandManager = violet.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("where").aliases("find").build();
        commandManager.register(commandMeta, this);
    }

    @Override
    public void execute(Invocation invocation) {
        VPlayer vPlayer = vBank.get(invocation.source());
        if (vPlayer == null) {
            return;
        }

        String[] strings = invocation.arguments();
        if (strings.length != 1) {
            vPlayer.writeUsage("where", "where <player>");
            return;
        }

        Player player = StaticUtils.findPlayer(strings[0]);
        if (player == null) {
            vPlayer.write("where", "Il player non e' online", MessageType.ERROR);
            return;
        }

        String server = StaticUtils.getServerName(player);
        vPlayer.write("where", player.getUsername() + " si trova in: " + server, MessageType.ERROR);
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
        return invocation.source().hasPermission("heros.proxy.where");
    }

}
