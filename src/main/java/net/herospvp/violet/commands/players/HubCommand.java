package net.herospvp.violet.commands.players;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.herospvp.violet.Violet;
import net.herospvp.violet.core.VPlayer;
import net.herospvp.violet.elements.MessageType;
import net.herospvp.violet.utils.StaticUtils;

import java.util.Arrays;
import java.util.List;

public class HubCommand implements SimpleCommand {

    private final List<String> lobbies;
    private final Violet violet;

    public HubCommand(Violet violet) {
        this.violet = violet;
        this.lobbies = Arrays.asList("pre-lobby", "lobby-1", "lobby-2", "lobby-3");

        CommandManager commandManager = violet.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("hub").aliases("lobby").build();
        commandManager.register(commandMeta, this);
    }

    @Override
    public void execute(Invocation invocation) {
        VPlayer vPlayer = violet.getVBank().get(invocation.source());
        if (vPlayer == null) {
            return;
        }
        String serverName = StaticUtils.getServerName(vPlayer);

        if (lobbies.contains(serverName)) {
            vPlayer.write("lobby", "Sei gia' connesso alla lobby!", MessageType.ERROR);
            return;
        }

        Player player = StaticUtils.findPlayer(vPlayer);
        if (player == null) {
            return;
        }

        vPlayer.write("lobby", "Connessione in corso...", MessageType.WARNING);
        violet.getJedisThread().getLobby().offer(vPlayer);
        player.createConnectionRequest(StaticUtils.findServer("lobby-2")).connect();
    }

}
