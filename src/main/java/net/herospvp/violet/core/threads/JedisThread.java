package net.herospvp.violet.core.threads;

import com.google.common.collect.Queues;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.herospvp.violet.Violet;
import net.herospvp.violet.core.VPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.BlockingQueue;

@SuppressWarnings("BusyWait")
@Getter
@Setter
public class JedisThread extends Thread {

    private boolean running;
    private final JedisPool jedisPool;

    private String ip, password;

    private final BlockingQueue<VPlayer> auth, lobby;

    private final Violet violet;

    public JedisThread(Violet violet, String ip, String password) {
        this.violet = violet;
        this.ip = ip;
        this.password = password;
        this.jedisPool = new JedisPool(ip);
        this.running = true;
        auth = Queues.newLinkedBlockingQueue();
        lobby = Queues.newLinkedBlockingQueue();
        this.setName("violet-jedis");
        this.start();
    }

    @SneakyThrows
    @Override
    public void run() {
        while (running) {

            try (Jedis jedis = jedisPool.getResource()) {
                jedis.auth(password);

                VPlayer vPlayer;
                while ((vPlayer = auth.poll()) != null) {
                    jedis.set(vPlayer.getName(), vPlayer.isPremium() ? "premium" : "cracked");
                }

                while ((vPlayer = lobby.poll()) != null) {
                    jedis.set(vPlayer.getName(), "hub");
                }

                violet.getVBank().getVPlayers().stream()
                        .filter(
                                player -> !player.isAuthenticated()
                        ).forEach(player -> {
                    String string = player.getName() + ":login";
                    if (!jedis.exists(string)) {
                        return;
                    }
                    if (jedis.get(string).equals("false")) {
                        player.setAuthenticated(true);
                    }
                    jedis.del(string);
                });
            }

            Thread.sleep(1000);
        }
    }

}
