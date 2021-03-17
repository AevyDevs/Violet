package net.herospvp.violet.commands.staffer;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import net.herospvp.violet.Violet;
import net.herospvp.violet.core.VBank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ForceSaveCommand implements SimpleCommand {

    private final Violet violet;
    private final VBank vBank;

    public ForceSaveCommand(Violet violet) {
        this.violet = violet;
        this.vBank = violet.getVBank();

        CommandManager commandManager = violet.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("forcesave").build();
        commandManager.register(commandMeta, this);
    }

    @Override
    public void execute(Invocation invocation) {
        vBank.saveAllStaffers();
        vBank.saveAllPlayers();
        invocation.source().sendMessage(Component.text("Salvando...").color(NamedTextColor.YELLOW));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("heros.proxy.forcesave");
    }

}
