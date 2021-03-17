package net.herospvp.violet.commands.players;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import net.herospvp.violet.Violet;
import net.herospvp.violet.core.VPlayer;
import net.herospvp.violet.elements.MessageType;

public class ContattiCommand implements SimpleCommand {

    private final Violet violet;
    private final String[] strings;

    public ContattiCommand(Violet violet) {
        this.violet = violet;

        strings = new String[] {
                "Sito web: https://www.herospvp.net/",
                "Telegram: @HerosPvP",
                "Discord: https://www.discord.gg/6kNTCUjKBg",
                "TeamSpeak: ts.herospvp.net",
        };

        CommandManager commandManager = violet.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("contatti").build();
        commandManager.register(commandMeta, this);
    }

    @Override
    public void execute(Invocation invocation) {
        VPlayer vPlayer = violet.getVBank().get(invocation.source());
        if (vPlayer == null) {
            return;
        }

        for (String s : strings) {
            vPlayer.write("contatti", s, MessageType.INFO);
        }
    }

}
