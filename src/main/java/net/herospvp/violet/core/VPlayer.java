package net.herospvp.violet.core;

import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import lombok.Setter;
import net.herospvp.violet.elements.Auth;
import net.herospvp.violet.elements.MessageType;
import net.herospvp.violet.utils.StaticUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class VPlayer {

    private final String name;
    private String ip;
    private String uuid;
    private boolean blacklisted, staffer, authenticated, online, needsInsert, needsUpdate, needsDelete;
    private Auth auth;
    private long firstJoin, lastJoin, totalTime;

    public VPlayer(
            @NotNull String name,
            @NotNull String ip,
            @Nullable String uuid,
            boolean blacklisted,
            boolean staffer,
            Auth auth,
            long firstJoin,
            long lastJoin,
            long totalTime
    ) {
        this.name = name;
        this.ip = ip;
        this.uuid = uuid;
        this.blacklisted = blacklisted;
        this.staffer = staffer;
        this.auth = auth;
        this.firstJoin = firstJoin;
        this.lastJoin = lastJoin;
        this.totalTime = totalTime;
    }

    public void write(String section, String message, MessageType messageType) {
        section = section.substring(0,1).toUpperCase() + section.substring(1).toLowerCase();

        TextComponent textComponent = Component.text()
                .append(Component.text("(( ", NamedTextColor.GOLD))
                .append(Component.text(section, messageType.getColor(), TextDecoration.BOLD, TextDecoration.ITALIC))
                .append(Component.text(" )) ", NamedTextColor.GOLD))
                .append(Component.text(message + (message.endsWith("!") || message.endsWith("?") || message.endsWith("\n") ? "" : "."), NamedTextColor.WHITE))
                .build();

        Player player = StaticUtils.findPlayer(name);
        if (player == null) {
            return;
        }
        player.sendMessage(textComponent);
    }

    public void write(TextComponent textComponent) {
        Player player = StaticUtils.findPlayer(name);
        if (player == null) {
            return;
        }
        player.sendMessage(textComponent);
    }

    public void writeUsage(String section, String command) {
        write(section, "Comando errato, utilizza: /" + command, MessageType.WARNING);
    }

    public boolean isUnknown() {
        return auth.equals(Auth.UNKNOWN);
    }

    public boolean isPremium() {
        return auth.equals(Auth.PREMIUM);
    }

    public boolean isCracked() {
        return auth.equals(Auth.CRACKED);
    }

    public void updateLastJoin() {
        lastJoin = System.currentTimeMillis();
    }

    public void updateTotalTime() {
        totalTime += System.currentTimeMillis() - lastJoin;
    }

}
