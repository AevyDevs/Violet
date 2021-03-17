package net.herospvp.violet.commands.players;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import net.herospvp.violet.Violet;
import net.herospvp.violet.core.VBank;
import net.herospvp.violet.core.VPlayer;
import net.herospvp.violet.elements.Auth;
import net.herospvp.violet.elements.MessageType;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PremiumCommand implements SimpleCommand {

    private final VBank vBank;

    public PremiumCommand(Violet violet) {
        this.vBank = violet.getVBank();

        CommandManager commandManager = violet.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("premium").build();
        commandManager.register(commandMeta, this);
    }

    @Override
    public void execute(SimpleCommand.Invocation invocation) {
        VPlayer vPlayer = vBank.get(invocation.source());
        if (vPlayer == null) {
            return;
        }

        if (!vPlayer.isUnknown()) {
            vPlayer.write("premium", "Mi spiace, ti sei gia' confermato come: " +
                    (vPlayer.isCracked() ? "cracked" : "premium"), MessageType.ERROR);
            return;
        }

        String[] args = invocation.arguments();
        if (args.length != 1) {
            vPlayer.write("premium", "Sei sicuro della tua scelta? Questa azione e' irreversibile!" +
                    " Se il tuo account e' PREMIUM digita: /premium confirm", MessageType.WARNING);
            return;
        }

        if (!args[0].equalsIgnoreCase("confirm")) {
            vPlayer.writeUsage("cracked", "premium confirm");
            return;
        }

        vPlayer.setAuth(Auth.PREMIUM);
        vPlayer.setNeedsUpdate(true);
        vPlayer.write("premium", "Ottimo, hai impostato il tuo account su premium!", MessageType.INFO);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> Collections.singletonList("confirm"));
    }

}
