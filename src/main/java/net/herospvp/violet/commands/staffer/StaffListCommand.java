package net.herospvp.violet.commands.staffer;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import net.herospvp.violet.Violet;
import net.herospvp.violet.core.VBank;
import net.herospvp.violet.core.VPlayer;
import net.herospvp.violet.elements.MessageType;
import net.herospvp.violet.utils.StaticUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class StaffListCommand implements SimpleCommand {

    private final Violet violet;
    private final VBank vBank;

    public StaffListCommand(Violet violet) {
        this.violet = violet;
        this.vBank = violet.getVBank();

        CommandManager commandManager = violet.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("stafflist").aliases("staffers").build();
        commandManager.register(commandMeta, this);
    }

    @Override
    public void execute(Invocation invocation) {
        VPlayer vPlayer = vBank.get(invocation.source());
        if (vPlayer == null) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();

        AtomicInteger i = new AtomicInteger();
        vBank.getVPlayers().parallelStream()
                .filter(
                        p -> vBank.hasPerms(p, "heros.proxy.stafflist")
                ).forEach(
                        p -> {
                            i.getAndIncrement();
                            stringBuilder
                                    .append("[")
                                    .append(StaticUtils.getServerName(p))
                                    .append("] ")
                                    .append(p.getName())
                                    .append("\n");
                        }
        );

        if (i.get() == 1) {
            vPlayer.write("stafflist", "Sei l'unico online!", MessageType.INFO);
            return;
        }

        vPlayer.write("stafflist", "Online (" + i.get() + "): \n" + stringBuilder.toString(), MessageType.INFO);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("heros.proxy.stafflist");
    }

}
