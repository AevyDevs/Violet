package net.herospvp.violet.core;

import lombok.Getter;
import lombok.Setter;
import net.herospvp.violet.elements.Rank;

@Setter
@Getter
public class VStaffer extends VPlayer {

    private Rank rank;
    private boolean staffChat, seeStaffChat = true, needsInsert, needsUpdate, needsDelete;

    public VStaffer(VPlayer player, Rank rank) {
        super(player.getName(), player.getIp(), player.getUuid(), player.isBlacklisted(), player.isStaffer(),
                player.getAuth(), player.getFirstJoin(), player.getLastJoin(), player.getTotalTime());
        this.rank = rank;
    }

}
