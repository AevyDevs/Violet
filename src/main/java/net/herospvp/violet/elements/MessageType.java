package net.herospvp.violet.elements;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public enum MessageType {

    INFO(NamedTextColor.GREEN),
    WARNING(NamedTextColor.YELLOW),
    ERROR(NamedTextColor.RED),
    SUPER_ERROR(NamedTextColor.DARK_RED);

    private final NamedTextColor color;

    MessageType(NamedTextColor color) {
        this.color = color;
    }

}
