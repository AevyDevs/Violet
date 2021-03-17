package net.herospvp.violet.commands.staffer;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import net.herospvp.violet.Violet;
import net.herospvp.violet.core.VBank;
import net.herospvp.violet.core.VPlayer;
import net.herospvp.violet.elements.MessageType;
import net.herospvp.violet.utils.StaticUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StatisticsCommand implements SimpleCommand {

    private final Violet violet;
    private final VBank vBank;

    public StatisticsCommand(Violet violet) {
        this.violet = violet;
        this.vBank = violet.getVBank();

        CommandManager commandManager = violet.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("sts").build();
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
            vPlayer.writeUsage("stats", "sts <player>");
            return;
        }

        VPlayer target = vBank.get(args[0]);
        if (target == null) {
            vPlayer.write("stats", "Il player non esiste", MessageType.ERROR);
            return;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

        vPlayer.write(
                Component.text("\nStatistiche di " + target.getName() + ":\nUUID: " + target.getUuid() + "\nBlacklist: " + target.isBlacklisted()
                        + "\nAuth: " + target.getAuth() + "\nPrimo login: " + simpleDateFormat.format(target.getFirstJoin()) + "\nUltimo login: "
                        + simpleDateFormat.format(target.getLastJoin()) + "\nTempo totale: " + (target.getTotalTime() / 1000 / 60 / 60)
                        + " ore (" + (target.getTotalTime() / 1000 / 60) + " minuti)\n")
                        .color(NamedTextColor.RED)
        );

    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {

            String[] args = invocation.arguments();

            if (invocation.arguments().length <= 1) {
                return StaticUtils.playerInCurrentServerTabComplete(invocation.source(), args[0]);
            }

            return null;
        });
    }

}
