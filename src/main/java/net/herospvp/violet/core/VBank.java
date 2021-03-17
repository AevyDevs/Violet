package net.herospvp.violet.core;

import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.herospvp.database.lib.Musician;
import net.herospvp.database.lib.items.Notes;
import net.herospvp.violet.Violet;
import net.herospvp.violet.elements.Auth;
import net.herospvp.violet.elements.Rank;
import net.herospvp.violet.utils.StaticUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Getter
public class VBank {

    private final Violet violet;

    private final List<VPlayer> vPlayers;
    private final List<VStaffer> vStaffers;

    private final Musician musician;
    private final Notes players, staffers;

    public VBank(Violet violet) {
        this.violet = violet;
        vPlayers = new ArrayList<>();
        vStaffers = new ArrayList<>();
        musician = violet.getMusician();
        players = violet.getPlayers();
        staffers = violet.getStaffers();
    }

    public void startup() {
        musician.offer((connection, instrument) -> {
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            try {
                preparedStatement = connection.prepareStatement(
                        players.createTable(new String[]{
                                "name CHAR(16) NOT NULL",
                                "ip CHAR(128)",
                                "uuid CHAR(36)",
                                "blacklisted BOOLEAN",
                                "staffer BOOLEAN",
                                "auth CHAR(16) NOT NULL",
                                "firstJoin BIGINT UNSIGNED",
                                "lastJoin BIGINT UNSIGNED",
                                "totalTime BIGINT UNSIGNED"
                        })
                );
                preparedStatement.executeQuery();

                preparedStatement = connection.prepareStatement(
                        players.selectAll()
                );
                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    vPlayers.add(new VPlayer(
                            resultSet.getString(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getBoolean(4),
                            resultSet.getBoolean(5),
                            Auth.valueOf(resultSet.getString(6)),
                            resultSet.getLong(7),
                            resultSet.getLong(8),
                            resultSet.getLong(9)
                    ));
                }

                preparedStatement = connection.prepareStatement(
                        staffers.createTable(new String[]{
                                "name CHAR(16) NOT NULL",
                                "rank CHAR(16)"
                        })
                );
                preparedStatement.execute();

                preparedStatement = connection.prepareStatement(
                        staffers.selectAll()
                );
                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    for (VPlayer player : vPlayers) {
                        if (!player.getName().equals(resultSet.getString(1))) {
                            continue;
                        }
                        vStaffers.add(new VStaffer(
                                player,
                                Rank.valueOf(resultSet.getString(2))
                        ));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                instrument.close(resultSet);
                instrument.close(preparedStatement);
            }
        });
        violet.setCanJoin(true);
    }

    public void saveAllStaffers() {
        musician.offer((connection, instrument) -> {
            PreparedStatement updateStatement = null, insertStatement = null, deleteStatement = null;
            try {
                insertStatement = connection.prepareStatement(
                        staffers.pendingInsert(new String[] {"name", "rank"})
                );

                updateStatement = connection.prepareStatement(
                        staffers.pendingUpdate(new String[] {"rank"}, "name")
                );

                deleteStatement = connection.prepareStatement(
                        "DELETE FROM " + staffers.getTable() + " WHERE NAME = ?;"
                );

                for (VStaffer staffer : vStaffers) {

                    if (staffer.isNeedsDelete()) {
                        deleteStatement.setString(1, staffer.getName());
                        deleteStatement.addBatch();
                    }

                    if (staffer.isNeedsInsert()) {
                        insertStatement.setString(1, staffer.getName());
                        insertStatement.setString(2, staffer.getRank().toString());
                        insertStatement.addBatch();
                        staffer.setNeedsInsert(false);
                    }

                    if (staffer.isNeedsUpdate()) {
                        updateStatement.setString(1, staffer.getRank().toString());
                        updateStatement.setString(2, staffer.getName());
                        updateStatement.addBatch();
                    }
                }
                insertStatement.executeBatch();
                updateStatement.executeBatch();
                deleteStatement.executeBatch();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                instrument.close(insertStatement);
                instrument.close(updateStatement);
                instrument.close(deleteStatement);
            }
        });
    }

    public void saveAllPlayers() {
        musician.offer((connection, instrument) -> {
            PreparedStatement updateStatement = null, insertStatement = null, deleteStatement = null;
            try {
                insertStatement = connection.prepareStatement(
                        players.pendingInsert(new String[]{
                                "name", "ip", "uuid", "blacklisted", "staffer", "auth", "firstJoin", "lastJoin", "totalTime"
                        })
                );

                updateStatement = connection.prepareStatement(
                        players.pendingUpdate(new String[]{
                                "ip", "uuid", "blacklisted", "staffer", "auth", "firstJoin", "lastJoin", "totalTime"
                        }, "name")
                );

                deleteStatement = connection.prepareStatement(
                        "DELETE FROM " + players.getTable() + " WHERE NAME = ?;"
                );

                for (VPlayer player : vPlayers) {

                    if (player.getTotalTime() <= 60000 && player.isUnknown() && !player.isBlacklisted()) {
                        deleteStatement.setString(1, player.getName());
                        deleteStatement.addBatch();
                    }

                    if (player.isNeedsInsert()) {
                        insertStatement.setString(1, player.getName());
                        insertStatement.setString(2, player.getIp());
                        insertStatement.setString(3, player.getUuid());
                        insertStatement.setBoolean(4, player.isBlacklisted());
                        insertStatement.setBoolean(5, player.isStaffer());
                        insertStatement.setString(6, player.getAuth().toString());
                        insertStatement.setLong(7, player.getFirstJoin());
                        insertStatement.setLong(8, player.getLastJoin());
                        insertStatement.setLong(9, player.getTotalTime());
                        insertStatement.addBatch();
                        player.setNeedsInsert(false);
                    }

                    if (player.isNeedsUpdate()) {
                        updateStatement.setString(1, player.getIp());
                        updateStatement.setString(2, player.getUuid());
                        updateStatement.setBoolean(3, player.isBlacklisted());
                        updateStatement.setBoolean(4, player.isStaffer());
                        updateStatement.setString(5, player.getAuth().toString());
                        updateStatement.setLong(6, player.getFirstJoin());
                        updateStatement.setLong(7, player.getLastJoin());
                        updateStatement.setLong(8, player.getTotalTime());
                        updateStatement.setString(9, player.getName());
                        updateStatement.addBatch();
                    }
                }

                insertStatement.executeBatch();
                updateStatement.executeBatch();
                deleteStatement.executeBatch();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                instrument.close(insertStatement);
                instrument.close(updateStatement);
                instrument.close(deleteStatement);
            }
        });
    }

    public void push(VPlayer vPlayer) {
        vPlayers.add(vPlayer);
    }
    
    public void pushStaffer(VStaffer vStaffer) {
        vStaffers.add(vStaffer);
    }

    public <G> VPlayer get(G generic) {

        if (generic instanceof String) {
            return vPlayers.parallelStream()
                    .filter(p -> p.getName().equals(generic))
                    .findAny().orElse(null);
        }

        if (generic instanceof Player) {
            return vPlayers.parallelStream()
                    .filter(p -> p.getName().equals(((Player) generic).getUsername()))
                    .findAny().orElse(null);
        }

        return null;
    }

    public <G> VStaffer getStaffer(G generic) {

        if (generic instanceof VPlayer) {
            return vStaffers.parallelStream()
                    .filter(p -> p.getName().equals(((VPlayer) generic).getName()))
                    .findAny().orElse(null);
        }

        if (generic instanceof String) {
            return vStaffers.parallelStream()
                    .filter(p -> p.getName().equals(generic))
                    .findAny().orElse(null);
        }

        if (generic instanceof Player) {
            return vStaffers.parallelStream()
                    .filter(p -> p.getName().equals(((Player) generic).getUsername()))
                    .findAny().orElse(null);
        }

        return null;
    }

    public void remove(VPlayer vPlayer) {
        vPlayers.remove(vPlayer);
    }

    public void remove(VStaffer vStaffer) {
        vStaffers.remove(vStaffer);
    }

    public <G> boolean hasPerms(G generic, String string) {
        Player player = StaticUtils.findPlayer(generic);
        return player != null && player.hasPermission(string);
    }

}
