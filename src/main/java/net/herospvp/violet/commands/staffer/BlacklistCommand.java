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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BlacklistCommand implements SimpleCommand {

    private final Violet violet;
    private final VBank vBank;

    public BlacklistCommand(Violet violet) {
        this.violet = violet;
        this.vBank = violet.getVBank();

        CommandManager commandManager = violet.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("blacklist").build();
        commandManager.register(commandMeta, this);
    }

    @Override
    public void execute(Invocation invocation) {
        VPlayer vPlayer = vBank.get(invocation.source());
        if (vPlayer == null) {
            return;
        }

        String[] args = invocation.arguments();
        if (args.length != 2) {

            if (args.length == 1 && args[0].equalsIgnoreCase("list")) {

                List<String> list = new ArrayList<>();
                vBank.getVPlayers().stream()
                        .filter(
                                VPlayer::isBlacklisted
                        ).forEach(
                                p -> list.add(p.getName())
                );

                if (list.isEmpty()) {
                    vPlayer.write("blacklist", "Non c'e' nessuno in blacklist... PER ORA", MessageType.WARNING);
                    return;
                }

                vPlayer.write(Component.text("Lista dei blacklistati (" + list.size() + ") :\n" +
                        list.toString()
                                .replace("[", "")
                                .replace("]", "")).color(NamedTextColor.RED));
                return;
            }

            vPlayer.writeUsage("blacklist", "blacklist <add/remove/list> <player>");
            return;
        }

        VPlayer target = vBank.get(args[1]);
        if (target == null) {
            vPlayer.write("blacklist", args[1] + " non esiste, pertanto non e' possibile eseguire alcuna azione",
                    MessageType.ERROR);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "add": {
                if (target.isBlacklisted()) {
                    vPlayer.write("blacklist", target.getName() + " e' gia' blacklistato",
                            MessageType.ERROR);
                    return;
                }
                target.setBlacklisted(true);
                target.setNeedsUpdate(true);

                vBank.getVPlayers().stream()
                        .filter(
                                VPlayer::isOnline
                        ).forEach(
                        p -> p.write(Component.text("\n" + target.getName() +
                                " e' stato blacklistato da " + vPlayer.getName() + "\n").color(NamedTextColor.RED))
                );

                if (target.isOnline()) {
                    Player player = StaticUtils.findPlayer(target);
                    if (player == null) {
                        return;
                    }
                    player.disconnect(Component.text("Sei stato blacklistato!\n\nPer ottenere l'unblacklist, visita: https://shop.herospvp.net/")
                            .color(NamedTextColor.RED));
                }
                break;
            }
            case "remove": {
                if (!target.isBlacklisted()) {
                    vPlayer.write("blacklist", target.getName() + " non e' blacklistato",
                            MessageType.ERROR);
                    return;
                }
                target.setBlacklisted(false);
                target.setNeedsUpdate(true);
                vPlayer.write("blacklist", "Hai unblacklistato " + target.getName(), MessageType.INFO);
                break;
            }
            default: {
                vPlayer.writeUsage("blacklist", "blacklist <add/remove/list> <player>");
                break;
            }
        }

    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {

            String[] args = invocation.arguments();

            if (args.length <= 1) {
                return StaticUtils.argsTabComplete(Arrays.asList("add", "remove", "list"), args[0]);
            }

            if (args.length == 2) {
                return StaticUtils.playerInCurrentServerTabComplete(invocation.source(), args[1]);
            }

            return null;
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("heros.proxy.blacklist");
    }

}
