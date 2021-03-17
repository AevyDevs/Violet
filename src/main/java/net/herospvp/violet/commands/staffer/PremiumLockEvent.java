package net.herospvp.violet.commands.staffer;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import net.herospvp.violet.Violet;
import net.herospvp.violet.core.VBank;
import net.herospvp.violet.core.VPlayer;
import net.herospvp.violet.elements.Auth;
import net.herospvp.violet.elements.MessageType;
import net.herospvp.violet.utils.StaticUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PremiumLockEvent implements SimpleCommand {

    private final Violet violet;
    private final VBank vBank;

    public PremiumLockEvent(Violet violet) {
        this.violet = violet;
        this.vBank = violet.getVBank();

        CommandManager commandManager = violet.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("lock").build();
        commandManager.register(commandMeta, this);
    }

    @Override
    public void execute(SimpleCommand.Invocation invocation) {
        VPlayer vPlayer = vBank.get(invocation.source());
        if (vPlayer == null) {
            return;
        }

        String[] args = invocation.arguments();
        if (args.length == 0) {
            vPlayer.writeUsage("lock", "lock <stats/set> <player> <cracked/premium>");
            return;
        }

        switch (args[0]) {
            case "stats": {
                int premium = 0, cracked = 0, unknown = 0;

                for (VPlayer player : vBank.getVPlayers()) {
                    if (player.isPremium()) {
                        premium++;
                    }
                    else if (player.isCracked()) {
                        cracked++;
                    }
                    else {
                        unknown++;
                    }
                }

                vPlayer.write("lock", "Utenti premium: " + premium, MessageType.INFO);
                vPlayer.write("lock", "Utenti cracked: " + cracked, MessageType.INFO);
                vPlayer.write("lock", "Utenti non protetti: " + unknown, MessageType.INFO);
                break;
            }
            case "set": {
                if (args.length != 3) {
                    vPlayer.writeUsage("lock", "lock <list/set> <player> <cracked/premium>");
                    break;
                }
                String status = args[1], name = args[2];

                VPlayer target = vBank.get(name);
                if (target == null) {
                    vPlayer.write("lock", "Il player non esiste", MessageType.ERROR);
                    return;
                }

                switch (status) {
                    case "premium": {
                        if (target.isPremium()) {
                            vPlayer.write("lock", "Il player e' gia' premium", MessageType.ERROR);
                            break;
                        }
                        target.setAuth(Auth.PREMIUM);
                        target.setNeedsUpdate(true);
                        vPlayer.write("lock", target.getName() + " e' ora: PREMIUM", MessageType.INFO);
                        break;
                    }
                    case "cracked": {
                        if (target.isCracked()) {
                            vPlayer.write("lock", "Il player e' gia' cracked", MessageType.ERROR);
                            break;
                        }
                        target.setAuth(Auth.CRACKED);
                        target.setNeedsUpdate(true);
                        vPlayer.write("lock", target.getName() + " e' ora: CRACKED", MessageType.INFO);
                        break;
                    }
                    default: {
                        vPlayer.writeUsage("lock", "lock <player> <cracked/premium>");
                        break;
                    }
                }
                break;
            }
            default: {
                vPlayer.writeUsage("lock", "lock <player> <cracked/premium>");
            }
        }

    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {

            String[] args = invocation.arguments();

            if (args.length <= 1) {
                return StaticUtils.argsTabComplete(Arrays.asList("stats", "set"), args[0]);
            }

            if (args.length == 2) {
                return StaticUtils.playerInCurrentServerTabComplete(invocation.source(), args[1]);
            }

            if (args.length == 3) {
                return StaticUtils.argsTabComplete(Arrays.asList("cracked", "premium"), args[2]);
            }

            return null;
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("heros.proxy.lock");
    }

}