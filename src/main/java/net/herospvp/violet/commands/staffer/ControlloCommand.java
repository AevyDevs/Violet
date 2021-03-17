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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ControlloCommand implements SimpleCommand {

    private final Violet violet;
    private final VBank vBank;

    public static Map<VPlayer, VPlayer> playersAndStafferInSS;

    public ControlloCommand(Violet violet) {
        this.violet = violet;
        this.vBank = violet.getVBank();
        playersAndStafferInSS = new HashMap<>();

        CommandManager commandManager = violet.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("controllo").aliases("ss", "freeze").build();
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
            vPlayer.writeUsage("controllo", "controllo <start/end> <player>");
            return;
        }

        VPlayer target = vBank.get(args[1]);
        if (target == null || !target.isOnline() || !target.isAuthenticated()) {
            vPlayer.write("controllo", "Il player non e' online", MessageType.ERROR);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "start": {

                VPlayer possibleStaffer = playersAndStafferInSS.get(target);

                if (playersAndStafferInSS.containsKey(target)) {
                    vPlayer.write("controllo", "Il player e' sotto controllo da parte di: " + possibleStaffer.getName(), MessageType.ERROR);
                    return;
                }

                vBank.getVStaffers().parallelStream()
                        .forEach(p -> p.write(Component.text("\n" + target.getName()
                                + " e' stato messo sotto controllo da " + vPlayer.getName() + "\n").color(NamedTextColor.YELLOW)));

                target.write(Component.text("\n\nSei stato messo sotto controllo da parte di " + vPlayer.getName()
                        + "! Perpiacere, fornisci i dati AnyDesk allo staffer in questione.")
                        .color(NamedTextColor.RED));
                target.write(Component.text("Non hai AnyDesk? Scaricalo da: https://www.anydesk.com/")
                        .color(NamedTextColor.YELLOW));

                playersAndStafferInSS.put(target, vPlayer);

                Player player = StaticUtils.findPlayer(target), staffer = StaticUtils.findPlayer(vPlayer);
                if (staffer == null) {
                    return;
                }
                staffer.createConnectionRequest(StaticUtils.findServer("controllo")).connect();

                if (player == null) {
                    return;
                }
                player.createConnectionRequest(StaticUtils.findServer("controllo")).connect();
                break;
            }
            case "end": {

                if (!playersAndStafferInSS.containsKey(target)) {
                    vPlayer.write("controllo", "Il player non e' sotto controllo", MessageType.ERROR);
                    return;
                }

                VPlayer possibleStaffer = playersAndStafferInSS.get(target);

                if (!possibleStaffer.equals(vPlayer)) {
                    vPlayer.write("controllo", "Il player e' sotto controllo da parte di " + possibleStaffer.getName(), MessageType.ERROR);
                    return;
                }

                vBank.getVStaffers().parallelStream()
                        .forEach(p -> p.write(Component.text("\n" + possibleStaffer.getName()
                                + " ha terminato il controllo su " + target.getName() + "\n").color(NamedTextColor.YELLOW)));

                target.write("controllo", "Controllo terminato, grazie per la pazienza!", MessageType.INFO);

                playersAndStafferInSS.remove(target);

                Player player = StaticUtils.findPlayer(target), staffer = StaticUtils.findPlayer(vPlayer);
                if (staffer == null) {
                    return;
                }
                staffer.createConnectionRequest(StaticUtils.findServer("lobby-3")).connect();
                violet.getJedisThread().getLobby().offer(vPlayer);

                if (player == null) {
                    return;
                }
                player.createConnectionRequest(StaticUtils.findServer("lobby-3")).connect();
                violet.getJedisThread().getLobby().offer(target);
                break;
            }
            default: {
                vPlayer.writeUsage("controllo", "controllo <start/end> <player>");
                break;
            }
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {

            String[] args = invocation.arguments();

            if (invocation.arguments().length <= 1) {
                return StaticUtils.argsTabComplete(Arrays.asList("start", "end"), args[0]);
            }

            if (invocation.arguments().length == 2) {
                return StaticUtils.playerInCurrentServerTabComplete(invocation.source(), args[1]);
            }

            return null;
        });
    }

}
