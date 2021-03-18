package net.herospvp.violet;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.herospvp.database.lib.Director;
import net.herospvp.database.lib.Musician;
import net.herospvp.database.lib.items.Instrument;
import net.herospvp.database.lib.items.Notes;
import net.herospvp.violet.commands.players.*;
import net.herospvp.violet.commands.staffer.*;
import net.herospvp.violet.core.VBank;
import net.herospvp.violet.core.VPlayer;
import net.herospvp.violet.core.threads.JedisThread;
import net.herospvp.violet.events.ChatHandler;
import net.herospvp.violet.events.InputHandler;
import net.herospvp.violet.utils.StaticUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@SuppressWarnings("BusyWait")
@Plugin(
        id = "violet",
        name = "Violet",
        version = "1.0.4-SNAPSHOT",
        url = "https://www.herospvp.net",
        description = "Addons for VelocityPowered",
        authors = { "Sorridi" }
)
@Getter
public class Violet {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final CommandManager commandManager;

    private VBank vBank;
    private JedisThread jedisThread;

    private Director director;
    private Musician musician;
    private Notes players, staffers;

    @Setter
    private boolean canJoin;

    @Inject
    public Violet(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.commandManager = server.getCommandManager();
    }

    @SneakyThrows
    @Subscribe
    public void on(ProxyInitializeEvent event) {
        StaticUtils.setViolet(this);

        if (!Files.exists(dataDirectory)) {
            Files.createDirectory(dataDirectory);
        }

        File file = new File(dataDirectory + "/config.toml");
        if (!file.exists()) {
            TomlWriter tomlWriter = new TomlWriter.Builder().build();

            Map<String, Object> main = new HashMap<>(), mysql = new HashMap<>(), redis = new HashMap<>(), words = new HashMap<>();

            redis.put("ip", "localhost");
            redis.put("password", "password");

            mysql.put("ip", "localhost");
            mysql.put("port", "3306");
            mysql.put("user", "root");
            mysql.put("password", "password");
            mysql.put("database", "violet");
            mysql.put("url", "mariadb");
            mysql.put("pool-size", 12);
            mysql.put("driver", "org.mariadb.jdbc.Driver");

            words.put("words",
                    Arrays.asList("coglione", "testa di cazzo", "madre morta",
                            "puttana", "kys", "hang yourself", "ucciditi", "troia",
                            "zoccola", "minorato", "ritardato", "frocio", "ricchione",
                            "travione", "finocchio", "ammazzati", "impiccati", "datti fuoco"
                    )
            );

            main.put("redis", redis);
            main.put("mysql", mysql);
            main.put("blacklisted-words", words);

            tomlWriter.write(main, file);
        }
        Toml toml = new Toml().read(file);

        this.director = new Director();
        Instrument instrument = new Instrument(
                toml.getString("mysql.ip"), toml.getString("mysql.port"), toml.getString("mysql.database"),
                toml.getString("mysql.user"), toml.getString("mysql.password"), toml.getString("mysql.url"),
                toml.getString("mysql.driver"), null, true,
                Math.toIntExact(toml.getLong("mysql.pool-size"))
        );
        this.musician = new Musician(director, instrument, true);

        this.players = new Notes("players");
        this.staffers = new Notes("staffers");

        this.vBank = new VBank(this);
        vBank.startup();

        // events
        new InputHandler(this);
        new ChatHandler(this, toml.getList("blacklisted-words.words"));

        // player commands
        new HubCommand(this);
        new ContattiCommand(this);
        new PingCommand(this);
        new VoteCommand(this);
        new PremiumCommand(this);
        new CrackedCommand(this);
        new ReportCommand(this);

        // staff commands
        new WhereCommand(this);
        new SendCommand(this);
        new StaffChatCommand(this);
        new StaffListCommand(this);
        new PremiumLockEvent(this);
        new ReportsCommand(this);
        new StatisticsCommand(this);
        new ForceSaveCommand(this);
        new ControlloCommand(this);
        new BlacklistCommand(this);
        new GotoCommand(this);

        this.jedisThread = new JedisThread(this, toml.getString("redis.ip"), toml.getString("redis.password"));

        getServer().getScheduler().buildTask(this, () -> {
            logger.warning("[Task] SAVING DATA!");
            vBank.saveAllStaffers();
            vBank.saveAllPlayers();
        }).repeat(10, TimeUnit.MINUTES).schedule();

    }

    @SneakyThrows
    @Subscribe
    public void on(ProxyShutdownEvent event) {

        vBank.saveAllPlayers();
        vBank.saveAllStaffers();

        logger.warning("[Shutdown] SAVING DATA!");
        while (!musician.getBlockingQueue().isEmpty()) {
            Thread.sleep(50);
        }
        logger.warning("[Shutdown] DATA SAVED!");
    }

}
