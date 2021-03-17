package net.herospvp.violet.commands.players;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import net.herospvp.violet.Violet;
import net.herospvp.violet.core.VBank;
import net.herospvp.violet.core.VPlayer;
import net.herospvp.violet.elements.MessageType;
import net.herospvp.violet.utils.StaticUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ReportCommand implements SimpleCommand {

    private final Violet violet;
    private final VBank vBank;

    public static Map<VPlayer, Long> timings;
    public static LinkedList<String> recentlyReported;

    public ReportCommand(Violet violet) {
        this.violet = violet;
        this.vBank = violet.getVBank();
        timings = new HashMap<>();
        recentlyReported = new LinkedList<>();

        CommandManager commandManager = violet.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("report").build();
        commandManager.register(commandMeta, this);
    }

    @Override
    public void execute(Invocation invocation) {
        VPlayer vPlayer = vBank.get(invocation.source());
        if (vPlayer == null) {
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 2) {
            vPlayer.writeUsage("report", "report <player> <motivo>");
            return;
        }

        VPlayer target = vBank.get(args[0]);
        if (target == null || !target.isOnline()) {
            vPlayer.write("report", "Il player non e' online", MessageType.ERROR);
            return;
        }

        long time = System.currentTimeMillis();

        if (timings.get(vPlayer) > time) {
            vPlayer.write("report", "Devi aspettare un po' prima di poter fare un nuovo report", MessageType.ERROR);
            return;
        }
        timings.replace(vPlayer, time + 120000);

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i == 0) {
                continue;
            }
            stringBuilder.append(args[i]).append(" ");
        }

        String server = StaticUtils.getServerName(vPlayer);
        TextComponent textComponent = Component.text(
                "\nServer: " + server + "\n" + target.getName() +
                        " e' stato reportato da " + vPlayer.getName()
                        + " per: " + stringBuilder.toString() + "\n").color(NamedTextColor.RED);

        vBank.getVStaffers().parallelStream().forEach(p -> p.write(textComponent));

        vPlayer.write("report", "Segnalazione inviata", MessageType.INFO);

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        recentlyReported.add("[" + formatter.format(date) + "] Da \"" + vPlayer.getName() + "\" a \""
                + target.getName() + "\" per \"" + stringBuilder.toString() + "\" in \"" + server + "\"");

        if (recentlyReported.size() > 10) {
            recentlyReported.remove(0);
        }

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
