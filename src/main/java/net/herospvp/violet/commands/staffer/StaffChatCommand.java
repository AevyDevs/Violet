package net.herospvp.violet.commands.staffer;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import net.herospvp.violet.Violet;
import net.herospvp.violet.core.VBank;
import net.herospvp.violet.core.VPlayer;
import net.herospvp.violet.core.VStaffer;
import net.herospvp.violet.elements.MessageType;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StaffChatCommand implements SimpleCommand {

    private final Violet violet;
    private final VBank vBank;

    public StaffChatCommand(Violet violet) {
        this.violet = violet;
        this.vBank = violet.getVBank();

        CommandManager commandManager = violet.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("staffchat").aliases("a").build();
        commandManager.register(commandMeta, this);
    }

    @Override
    public void execute(Invocation invocation) {
        VPlayer vPlayer = vBank.get(invocation.source());
        if (vPlayer == null) {
            return;
        }

        String[] args = invocation.arguments();
        VStaffer vStaffer = vBank.getStaffer(vPlayer);

        if (args.length == 0) {
            vPlayer.write("staffchat", vStaffer.isStaffChat() ? "Disattivata" : "Attivata", MessageType.INFO);
            vStaffer.setStaffChat(!vStaffer.isStaffChat());
            if (!vStaffer.isSeeStaffChat()) {
                vPlayer.write("staffchat", "Notifiche attivate", MessageType.INFO);
                vStaffer.setSeeStaffChat(true);
            }
            return;
        }

        if (args.length != 1) {
            vPlayer.writeUsage("staffchat", "staffchat <notify>");
            return;
        }

        if (args[0].equalsIgnoreCase("notify")) {
            vPlayer.write("staffchat", vStaffer.isSeeStaffChat() ? "Notifiche disattivate" : "Notifiche attivate", MessageType.INFO);
            vStaffer.setSeeStaffChat(!vStaffer.isSeeStaffChat());
            if (vStaffer.isStaffChat()) {
                vPlayer.write("staffchat", "Disattivata", MessageType.INFO);
                vStaffer.setStaffChat(false);
            }
        }

    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> Collections.singletonList("notify"));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("heros.proxy.staffchat");
    }

}