package net.herospvp.violet.commands.staffer;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import net.herospvp.violet.Violet;
import net.herospvp.violet.commands.players.ReportCommand;
import net.herospvp.violet.core.VBank;
import net.herospvp.violet.core.VPlayer;
import net.herospvp.violet.elements.MessageType;
import net.herospvp.violet.utils.StaticUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ReportsCommand implements SimpleCommand {

    private final Violet violet;
    private final VBank vBank;

    private final Map<VPlayer, Long> timings;
    private final LinkedList<String> recentlyReported;

    public ReportsCommand(Violet violet) {
        this.violet = violet;
        this.vBank = violet.getVBank();
        this.timings = ReportCommand.timings;
        this.recentlyReported = ReportCommand.recentlyReported;

        CommandManager commandManager = violet.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("reports").build();
        commandManager.register(commandMeta, this);
    }

    @Override
    public void execute(Invocation invocation) {
        VPlayer vPlayer = vBank.get(invocation.source());
        if (vPlayer == null) {
            return;
        }

        String[] args = invocation.arguments();
        if (args.length  != 2) {
            if (recentlyReported.size() == 0) {
                vPlayer.write("reports", "Non sono presenti nuovi reports", MessageType.WARNING);
                return;
            }

            StringBuilder stringBuilder = new StringBuilder();

            for (String string : recentlyReported) {
                stringBuilder.append(string);
            }
            stringBuilder.append("\n");

            vPlayer.write(Component.text("\nEcco gli ultimi 10 reports:\n" + stringBuilder.toString()).color(NamedTextColor.RED));
            return;
        }

        if (!args[0].equalsIgnoreCase("timeout")) {
            vPlayer.writeUsage("reports", "reports <timeout> <player>");
            return;
        }

        VPlayer target = vBank.get(args[1]);
        if (target == null || !target.isOnline()) {
            vPlayer.write("reports", "Il player non e' online", MessageType.ERROR);
            return;
        }

        timings.replace(target, System.currentTimeMillis() + 1800000);
        target.write("reports", "Sei stato messo in timeout per 30 minuti", MessageType.WARNING);
        vPlayer.write("reports", target.getName() + " e' stato messo in timeout per 30 minuti", MessageType.INFO);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {

            String[] args = invocation.arguments();

            if (args.length <= 1) {
                return StaticUtils.argsTabComplete(Collections.singletonList("timeout"), args[0]);
            }

            if (args.length == 2) {
                return StaticUtils.playerInCurrentServerTabComplete(invocation.source(), args[1]);
            }

            return null;
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("heros.proxy.reports");
    }

}
