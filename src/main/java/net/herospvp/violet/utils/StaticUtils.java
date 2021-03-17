package net.herospvp.violet.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Setter;
import net.herospvp.violet.Violet;
import net.herospvp.violet.core.VPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StaticUtils {

    @Setter
    private static Violet violet;

    @Nullable
    public static <G> String getServerName(G generic) {
        Player player = generic instanceof Player ? (Player) generic : findPlayer(generic);
        return player == null ? null : player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : null;
    }

    @Nullable
    public static <G> RegisteredServer getCurrentServer(G generic) {
        Player player = generic instanceof Player ? (Player) generic : findPlayer(generic);
        return player == null ? null : player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServer() : null;
    }

    @Nullable
    public static <G> Player findPlayer(G generic) {
        if (generic instanceof Player) {
            return (Player) generic;
        }

        String name = generic instanceof VPlayer ? ((VPlayer) generic).getName() : (String) generic;
        return violet.getServer().getPlayer(name).orElse(null);
    }

    @Nullable
    public static RegisteredServer findServer(String string) {
        return violet.getServer().getServer(string).orElse(null);
    }

    @Nullable
    public static <G> List<String> playerInCurrentServerTabComplete(G generic, String string) {
        List<String> players = onlinePlayersAsList(generic);
        if (players == null || players.isEmpty()) {
            return null;
        }
        List<String> res = new ArrayList<>();
        players.stream().filter(
                s -> s.toLowerCase().startsWith(string.toLowerCase())
        ).forEach(res::add);
        return res.isEmpty() ? players : res;
    }

    @Nullable
    public static List<String> playerInGlobalTabComplete(String string) {
        List<String> players = onlineGlobalPlayersAsList();
        if (players.isEmpty()) {
            return null;
        }
        List<String> res = new ArrayList<>();
        players.stream().filter(
                s -> s.toLowerCase().startsWith(string.toLowerCase())
        ).forEach(res::add);
        return res.isEmpty() ? players : res;
    }

    public static List<String> argsTabComplete(List<String> list, String string) {
        List<String> res = new ArrayList<>();
        list.stream().filter(s -> s.toLowerCase().startsWith(string.toLowerCase())).forEach(res::add);
        return res.isEmpty() ? list : res;
    }

    public static boolean isPlayerPresent(String string) {
        return findPlayer(string) != null;
    }

    public static boolean isServerPresent(String string) {
        return findServer(string) != null;
    }

    @Nullable
    public static <G> List<String> onlinePlayersAsList(G generic) {
        Player player = generic instanceof Player ? (Player) generic : findPlayer((String) generic);
        if (player == null || !player.getCurrentServer().isPresent()) {
            return null;
        }

        List<String> list = new ArrayList<>();
        player.getCurrentServer().get().getServer().getPlayersConnected().forEach(p -> list.add(p.getUsername()));
        return list;
    }

    public static Collection<Player> onlineGlobalPlayers() {
        return violet.getServer().getAllPlayers();
    }

    public static List<String> onlineGlobalPlayersAsList() {
        List<String> res = new ArrayList<>();
        onlineGlobalPlayers().forEach(player -> res.add(player.getUsername()));
        return res;
    }

    public static List<String> serversAsList() {
        List<String> list = new ArrayList<>();
        violet.getServer().getAllServers().forEach(s -> list.add(s.getServerInfo().getName()));
        return list;
    }

    @Nullable
    public static <G> Collection<Player> onlinePlayersAsCollection(G generic) {
        Player player = generic instanceof Player ? (Player) generic : findPlayer(generic);

        if (player == null || !player.getCurrentServer().isPresent()) {
            return null;
        }

        return player.getCurrentServer().get().getServer().getPlayersConnected();
    }

}
