package com.doctordark.hcf;

import com.doctordark.hcf.combatlog.CombatLogListener;
import com.doctordark.hcf.command.*;
import com.doctordark.hcf.deathban.*;
import com.doctordark.hcf.deathban.lives.LivesExecutor;
import com.doctordark.hcf.economy.*;
import com.doctordark.hcf.eventgame.CaptureZone;
import com.doctordark.hcf.eventgame.EventExecutor;
import com.doctordark.hcf.eventgame.EventScheduler;
import com.doctordark.hcf.eventgame.conquest.ConquestExecutor;
import com.doctordark.hcf.eventgame.crate.KeyListener;
import com.doctordark.hcf.eventgame.crate.KeyManager;
import com.doctordark.hcf.eventgame.eotw.EotwCommand;
import com.doctordark.hcf.eventgame.eotw.EotwHandler;
import com.doctordark.hcf.eventgame.eotw.EotwListener;
import com.doctordark.hcf.eventgame.faction.CapturableFaction;
import com.doctordark.hcf.eventgame.faction.ConquestFaction;
import com.doctordark.hcf.eventgame.faction.KothFaction;
import com.doctordark.hcf.eventgame.koth.KothExecutor;
import com.doctordark.hcf.faction.FactionExecutor;
import com.doctordark.hcf.faction.FactionManager;
import com.doctordark.hcf.faction.FactionMember;
import com.doctordark.hcf.faction.FlatFileFactionManager;
import com.doctordark.hcf.faction.claim.*;
import com.doctordark.hcf.faction.type.*;
import com.doctordark.hcf.listener.*;
import com.doctordark.hcf.listener.fixes.*;
import com.doctordark.hcf.pvpclass.PvpClassManager;
import com.doctordark.hcf.pvpclass.bard.EffectRestorer;
import com.doctordark.hcf.scoreboard.ScoreboardHandler;
import com.doctordark.hcf.sotw.SotwCommand;
import com.doctordark.hcf.sotw.SotwListener;
import com.doctordark.hcf.sotw.SotwTimer;
import com.doctordark.hcf.timer.TimerExecutor;
import com.doctordark.hcf.timer.TimerManager;
import com.doctordark.hcf.user.FactionUser;
import com.doctordark.hcf.user.UserManager;
import com.doctordark.hcf.visualise.ProtocolLibHook;
import com.doctordark.hcf.visualise.VisualiseHandler;
import com.doctordark.hcf.visualise.WallBorderListener;
import com.google.common.base.Joiner;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class HCF extends JavaPlugin {

    public static final Joiner SPACE_JOINER = Joiner.on(' ');
    public static final Joiner COMMA_JOINER = Joiner.on(", ");

    @Getter
    private static HCF plugin;

    @Getter
    private Random random = new Random();

    @Getter
    private Configuration configuration;

    @Getter
    private ClaimHandler claimHandler;

    @Getter
    private CombatLogListener combatLogListener;

    @Getter
    private DeathbanManager deathbanManager;

    @Getter
    private EconomyManager economyManager;

    @Getter
    private EffectRestorer effectRestorer;

    @Getter
    private EotwHandler eotwHandler;

    @Getter
    private EventScheduler eventScheduler;

    @Getter
    private FactionManager factionManager;

    @Getter
    private ImageFolder imageFolder;

    @Getter
    private KeyManager keyManager;

    @Getter
    private PvpClassManager pvpClassManager;

    @Getter
    private ScoreboardHandler scoreboardHandler;

    @Getter
    private SotwTimer sotwTimer;

    @Getter
    private TimerManager timerManager;

    @Getter
    private UserManager userManager;

    @Getter
    private VisualiseHandler visualiseHandler;

    @Getter
    private WorldEditPlugin worldEdit;

    @Getter
    private boolean paperPatch;

    private boolean configurationLoaded = true;

    @Override
    public void onEnable() {
        registerConfiguration();
        if (!configurationLoaded) {
            getLogger().severe("Disabling plugin..");
            setEnabled(false);
            return;
        }

        HCF.plugin = this;
        DateTimeFormats.reload(configuration.getServerTimeZone());        // Initialise the static fields.
        ///////////////////////////
        Plugin wep = getServer().getPluginManager().getPlugin("WorldEdit");  // Initialise WorldEdit hook.
        worldEdit = wep instanceof WorldEditPlugin && wep.isEnabled() ? (WorldEditPlugin) wep : null;

        registerSerialization();
        registerCommands();
        registerManagers();
        registerListeners();

        paperPatch = false;
        /* TODO: BROKEN: Method does not exist
        try {
            Team team = getServer().getScoreboardManager().createNewTeam("lookup");
            team.unregister();
        } catch (NoSuchMethodError ex) {
            paperPatch = false;
        } */

        //TODO: More reliable, SQL based.
        long dataSaveInterval = TimeUnit.MINUTES.toMillis(20L);
        new BukkitRunnable() {
            @Override
            public void run() {
                saveData();
            }
        }.runTaskTimerAsynchronously(this, dataSaveInterval, dataSaveInterval);

        ProtocolLibHook.hook(this); // Initialise ProtocolLib hook.
    }

    private void saveData() {
        deathbanManager.saveDeathbanData();
        economyManager.saveEconomyData();
        factionManager.saveFactionData();
        keyManager.saveKeyData();
        timerManager.saveTimerData();
        userManager.saveUserData();
    }

    @Override
    public void onDisable() {
        if (!configurationLoaded) {
            // Ignore everything.
            return;
        }

        try {
            String configFileName = "config.cdl";
            configuration.save(new File(getDataFolder(), configFileName), HCF.class.getResource("/" + configFileName));
        } catch (IOException | InvalidConfigurationException ex) {
            getLogger().warning("Unable to save config.");
            ex.printStackTrace();
        }

        combatLogListener.removeCombatLoggers();
        pvpClassManager.onDisable();
        scoreboardHandler.clearBoards();

        saveData();

        HCF.plugin = null; // Always uninitialise last.
    }

    private void registerConfiguration() {
        configuration = new Configuration();
        try {
            String configFileName = "config.cdl";
            File file = new File(getDataFolder(), configFileName);
            if (!file.exists()) {
                saveResource(configFileName, false);
            }

            configuration.load(file, HCF.class.getResource("/" + configFileName));
            configuration.updateFields();
        } catch (IOException | InvalidConfigurationException ex) {
            getLogger().log(Level.SEVERE, "Failed to load configuration", ex);
            configurationLoaded = false;
        }
    }

    //TODO: More reliable, SQL based.
    private void registerSerialization() {
        ConfigurationSerialization.registerClass(CaptureZone.class);
        ConfigurationSerialization.registerClass(Deathban.class);
        ConfigurationSerialization.registerClass(Claim.class);
        ConfigurationSerialization.registerClass(Subclaim.class);
        ConfigurationSerialization.registerClass(Deathban.class);
        ConfigurationSerialization.registerClass(FactionUser.class);
        ConfigurationSerialization.registerClass(ClaimableFaction.class);
        ConfigurationSerialization.registerClass(ConquestFaction.class);
        ConfigurationSerialization.registerClass(CapturableFaction.class);
        ConfigurationSerialization.registerClass(KothFaction.class);
        ConfigurationSerialization.registerClass(EndPortalFaction.class);
        ConfigurationSerialization.registerClass(Faction.class);
        ConfigurationSerialization.registerClass(FactionMember.class);
        ConfigurationSerialization.registerClass(PlayerFaction.class);
        ConfigurationSerialization.registerClass(RoadFaction.class);
        ConfigurationSerialization.registerClass(SpawnFaction.class);
        ConfigurationSerialization.registerClass(RoadFaction.NorthRoadFaction.class);
        ConfigurationSerialization.registerClass(RoadFaction.EastRoadFaction.class);
        ConfigurationSerialization.registerClass(RoadFaction.SouthRoadFaction.class);
        ConfigurationSerialization.registerClass(RoadFaction.WestRoadFaction.class);
    }

    private void registerListeners() {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new BlockHitFixListener(), this);
        manager.registerEvents(new BlockJumpGlitchFixListener(), this);
        manager.registerEvents(new BoatGlitchFixListener(this), this);
        manager.registerEvents(new BookDisenchantListener(this), this);
        manager.registerEvents(new BottledExpListener(this), this);
        manager.registerEvents(new ChatListener(this), this);
        manager.registerEvents(new ClaimWandListener(this), this);
        manager.registerEvents(combatLogListener = new CombatLogListener(this), this);
        manager.registerEvents(new CoreListener(this), this);
        manager.registerEvents(new CrowbarListener(this), this);
        manager.registerEvents(new DeathListener(this), this);
        manager.registerEvents(new DeathbanListener(this), this);
        manager.registerEvents(new EnchantLimitListener(this), this);
        manager.registerEvents(new EnderChestRemovalListener(this), this);
        manager.registerEvents(new EntityLimitListener(this), this);
        manager.registerEvents(new EotwListener(this), this);
        manager.registerEvents(new EventSignListener(), this);
        manager.registerEvents(new ExpMultiplierListener(this), this);
        manager.registerEvents(new FactionListener(this), this);
        manager.registerEvents(new FurnaceSmeltSpeedListener(this), this);
        manager.registerEvents(new InfinityArrowFixListener(this), this);
        manager.registerEvents(new KeyListener(this), this);
        manager.registerEvents(new PearlGlitchListener(this), this);
        manager.registerEvents(new PortalListener(this), this);
        manager.registerEvents(new PotionLimitListener(this), this);
        manager.registerEvents(new ProtectionListener(this), this);
        manager.registerEvents(new SubclaimWandListener(this), this);
        manager.registerEvents(new SignSubclaimListener(this), this);
        manager.registerEvents(new ShopSignListener(this), this);
        manager.registerEvents(new SkullListener(), this);
        manager.registerEvents(new SotwListener(this), this);
        manager.registerEvents(new BeaconStrengthFixListener(this), this);
        manager.registerEvents(new VoidGlitchFixListener(), this);
        manager.registerEvents(new WallBorderListener(this), this);
        manager.registerEvents(new WorldListener(), this);
    }

    private void registerCommands() {
        getCommand("angle").setExecutor(new AngleCommand());
        getCommand("conquest").setExecutor(new ConquestExecutor(this));
        getCommand("economy").setExecutor(new EconomyCommand(this));
        getCommand("eotw").setExecutor(new EotwCommand(this));
        getCommand("event").setExecutor(new EventExecutor(this));
        getCommand("faction").setExecutor(new FactionExecutor(this));
        getCommand("gopple").setExecutor(new GoppleCommand(this));
        getCommand("koth").setExecutor(new KothExecutor(this));
        getCommand("lives").setExecutor(new LivesExecutor(this));
        getCommand("location").setExecutor(new LocationCommand(this));
        getCommand("logout").setExecutor(new LogoutCommand(this));
        getCommand("mapkit").setExecutor(new MapKitCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("pvptimer").setExecutor(new PvpTimerCommand(this));
        getCommand("regen").setExecutor(new RegenCommand(this));
        getCommand("servertime").setExecutor(new ServerTimeCommand(this));
        getCommand("sotw").setExecutor(new SotwCommand(this));
        getCommand("spawncannon").setExecutor(new SpawnCannonCommand(this));
        getCommand("staffrevive").setExecutor(new StaffReviveCommand(this));
        getCommand("timer").setExecutor(new TimerExecutor(this));
        getCommand("togglecapzoneentry").setExecutor(new ToggleCapzoneEntryCommand(this));
        getCommand("togglelightning").setExecutor(new ToggleLightningCommand(this));
        getCommand("togglesidebar").setExecutor(new ToggleSidebarCommand(this));

        Map<String, Map<String, Object>> map = getDescription().getCommands();
        for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
            PluginCommand command = getCommand(entry.getKey());
            command.setPermission("hcf.command." + entry.getKey());
            command.setPermissionMessage(ChatColor.RED + "You do not have permission for this command.");
        }
    }

    private void registerManagers() {
        claimHandler = new ClaimHandler(this);
        deathbanManager = new FlatFileDeathbanManager(this);
        economyManager = new FlatFileEconomyManager(this);
        effectRestorer = new EffectRestorer(this);
        eotwHandler = new EotwHandler(this);
        eventScheduler = new EventScheduler(this);
        factionManager = new FlatFileFactionManager(this);
        imageFolder = new ImageFolder(this);
        keyManager = new KeyManager(this);
        pvpClassManager = new PvpClassManager(this);
        sotwTimer = new SotwTimer();
        timerManager = new TimerManager(this); // Needs to be registered before ScoreboardHandler.
        scoreboardHandler = new ScoreboardHandler(this);
        userManager = new UserManager(this);
        visualiseHandler = new VisualiseHandler();
    }
}
