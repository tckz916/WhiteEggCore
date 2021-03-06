package com.github.niwaniwa.we.core.init;

import com.github.niwaniwa.we.core.WhiteEggCore;
import com.github.niwaniwa.we.core.command.*;
import com.github.niwaniwa.we.core.command.core.WhiteEggCoreCommand;
import com.github.niwaniwa.we.core.command.toggle.WhiteEggToggleCommand;
import com.github.niwaniwa.we.core.command.twitter.WhiteEggTwitterCommand;
import com.github.niwaniwa.we.core.command.twitter.WhiteEggTwitterRegisterCommand;
import com.github.niwaniwa.we.core.config.WhiteEggCoreConfig;
import com.github.niwaniwa.we.core.database.DataBase;
import com.github.niwaniwa.we.core.database.DataBaseManager;
import com.github.niwaniwa.we.core.database.DataBaseType;
import com.github.niwaniwa.we.core.database.mongodb.MongoDataBaseManager;
import com.github.niwaniwa.we.core.listener.Debug;
import com.github.niwaniwa.we.core.listener.PlayerListener;
import com.github.niwaniwa.we.core.listener.ScriptListener;
import com.github.niwaniwa.we.core.player.WhitePlayerFactory;
import com.github.niwaniwa.we.core.player.rank.Rank;
import com.github.niwaniwa.we.core.script.JavaScript;
import com.github.niwaniwa.we.core.util.message.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.PluginManager;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map.Entry;

public class Initialize implements Base, Listener {

    private static boolean enable = false;
    private static Initialize init = new Initialize(WhiteEggCore.getInstance());

    private WhiteEggCoreConfig config;
    private JavaScript script;
    private WhiteEggCore mainClassInstance;
    private MessageManager msg;
    private DataBase database;

    private Initialize(WhiteEggCore instance) {
        mainClassInstance = instance;
        config = WhiteEggCore.getConf();
        Bukkit.getPluginManager().registerEvents(this, mainClassInstance);
        LoadLanguage lang = LoadLanguage.getInstance();
        lang.start(false);
        msg = lang.getManager();
    }

    @Override
    public boolean start(boolean debug) {
        this.setting(debug);
        enable = true;
        return enable;
    }

    public void debugMessage(String message, boolean debug) {
        if (debug) {
            WhiteEggCore.logger.info("setting " + message + " ...");
        }
    }

    private void setting(boolean debug) {
        this.settingCheck();
        this.register();
        this.registerCommands();
        this.registerListener();
        this.load();
        this.settingDatabase();
    }

    private void load() {
        WhitePlayerFactory.load();
        Rank.load();
    }

    /**
     * 各種登録
     */
    private void register() {
        this.registerDatabaseClass();
        if (!config.isEnableScript()) {
            return;
        }
        try {
            JavaScript.copyScript();
        } catch (IOException e) {
            e.printStackTrace();
        }
        script = JavaScript.loadScript();
    }

    /**
     * コマンドの登録
     */
    private void registerCommands() {
        if (!config.isEnableCommand()) {
            return;
        }
        WhiteEggCoreCommandHandler handler = new WhiteEggCoreCommandHandler();
        handler.registerCommand("whiteeggcore", new WhiteEggCoreCommand());
        handler.registerCommand("toggle", new WhiteEggToggleCommand());
        handler.registerCommand("head", new WhiteEggHeadCommand());
        handler.registerCommand("register", new WhiteEggTwitterRegisterCommand());
        handler.registerCommand("whisper", new WhiteEggWhisperCommand());
        handler.registerCommand("replay", new WhiteEggReplayCommand());
        handler.registerCommand("script", new WhiteEggScriptCommand());
        handler.registerCommand("twitter", new WhiteEggTwitterCommand());
    }

    /**
     * リスナーの登録
     */
    private void registerListener() {
        if (!config.isEnableListener()) {
            return;
        }
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new Debug(), mainClassInstance);
        pm.registerEvents(new PlayerListener(), mainClassInstance);
        new ScriptListener();
    }

    public static boolean isEnable() {
        return enable;
    }

    public JavaScript getScript() {
        return script;
    }

    public DataBase getDatabase() {
        return database;
    }

    private void settingCheck() {
        if (config.getConfig().getBoolean("setting.twitter.useTwitter", true)) {
            if (config.getConfig().getString("setting.twitter.consumerKey", "").isEmpty()
                    || config.getConfig().getString("setting.twitter.consumerSecret", "").isEmpty()) {
                config.getConfig().set("setting.twitter.useTwitter", false);
                if (!config.getConfig().getBoolean("warn", true)) {
                    return;
                }
                WhiteEggCore.logger.warning("Twitter Consumer key or Consumer Secret is empty");
                WhiteEggCore.logger.warning("Twitter command disable");
            }
        }
        if (!config.isEnableListener() || !config.isEnableCommand()) {
            if (!config.getConfig().getBoolean("warn", true)) {
                return;
            }
            WhiteEggCore.logger.warning("Listener or Command is disabled. Not recommended.");
        }
    }

    private void registerDatabaseClass() {
        DataBaseManager.register("MongoDB", MongoDataBaseManager.class);
    }

    private void settingDatabase() {
        if (!config.useDataBase()) {
            return;
        }
        DataBaseType type = DataBaseType.valueOf(config.getConfig().getString("setting.database.type"));
        if (type == null) {
            return;
        }
        for (Entry<String, Class<? extends DataBase>> entry : DataBaseManager.getDatabaseClass().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(type.getType())) {
                try {
                    Constructor<? extends DataBase> constructor = entry.getValue().getConstructor(String.class, int.class);
                    this.database = constructor.newInstance(config.getConfig().getString("setting.database.host"), config.getConfig().getInt("setting.database.port"));
                } catch (Exception e) {
                    config.getConfig().set("setting.database.enable", false);
                    e.printStackTrace();
                }
            }
        }
    }

    public MessageManager getMessageManager() {
        return msg;
    }

    public WhiteEggCoreConfig getConfig() {
        return config;
    }

    public static Initialize getInstance(WhiteEggCore instance) {
        if (enable) {
            return null;
        }
        return init;
    }

    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        if (event.getPlugin().getName().equalsIgnoreCase("WhiteEggCore")) {
            enable = false;
        }
    }

}
