package com.doctordark.hcf;

import com.doctordark.hcf.configuration.ConfigurationModule;
import com.doctordark.hcf.module.ModuleLoader;
import com.doctordark.hcf.visualise.ProtocolLibHook;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class HCF extends JavaPlugin {

    @Getter
    private ModuleLoader moduleLoader;

    @Override
    public void onEnable() {
        this.moduleLoader = new ModuleLoader(this);

        registerModules();

//        Plugin wep = getServer().getPluginManager().getPlugin("WorldEdit");  // Initialise WorldEdit hook.
//        worldEdit = wep instanceof WorldEditPlugin && wep.isEnabled() ? (WorldEditPlugin) wep : null;


//        long dataSaveInterval = TimeUnit.MINUTES.toMillis(20L);
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                saveData();
//            }
//        }.runTaskTimerAsynchronously(this, dataSaveInterval, dataSaveInterval);

        ProtocolLibHook.hook(this); // Initialise ProtocolLib hook.
    }

    private void registerModules() {
        Arrays.asList(
                new ConfigurationModule(this)
        ).forEach(this.moduleLoader::load);
    }

//    private void saveData() {
//        deathbanManager.saveDeathbanData();
//        economyManager.saveEconomyData();
//        factionManager.saveFactionData();
//        keyManager.saveKeyData();
//        timerManager.saveTimerData();
//        userManager.saveUserData();
//    }

    @Override
    public void onDisable() {
//        combatLogListener.removeCombatLoggers();
//        pvpClassManager.onDisable();
//        scoreboardHandler.clearBoards();

//        saveData();
    }

//    private void registerListeners() {
//        PluginManager manager = getServer().getPluginManager();
//        manager.registerEvents(new BlockHitFixListener(), this);
//        manager.registerEvents(new BlockJumpGlitchFixListener(), this);
//        manager.registerEvents(new BoatGlitchFixListener(this), this);
//        manager.registerEvents(new BookDisenchantListener(this), this);
//        manager.registerEvents(new BottledExpListener(this), this);
//        manager.registerEvents(new ChatListener(this), this);
//        manager.registerEvents(new ClaimWandListener(this), this);
//        manager.registerEvents(new CombatLogListener(this), this);
//        manager.registerEvents(new CoreListener(this), this);
//        manager.registerEvents(new CrowbarListener(this), this);
//        manager.registerEvents(new DeathListener(this), this);
//        manager.registerEvents(new DeathbanListener(this), this);
//        manager.registerEvents(new EnchantLimitListener(this), this);
//        manager.registerEvents(new EnderChestRemovalListener(this), this);
//        manager.registerEvents(new EntityLimitListener(this), this);
//        manager.registerEvents(new EotwListener(this), this);
//        manager.registerEvents(new EventSignListener(), this);
//        manager.registerEvents(new ExpMultiplierListener(this), this);
//        manager.registerEvents(new FactionListener(this), this);
//        manager.registerEvents(new FurnaceSmeltSpeedListener(this), this);
//        manager.registerEvents(new InfinityArrowFixListener(this), this);
//        manager.registerEvents(new KeyListener(this), this);
//        manager.registerEvents(new PearlGlitchListener(this), this);
//        manager.registerEvents(new PortalListener(this), this);
//        manager.registerEvents(new PotionLimitListener(this), this);
//        manager.registerEvents(new ProtectionListener(this), this);
//        manager.registerEvents(new SubclaimWandListener(this), this);
//        manager.registerEvents(new SignSubclaimListener(this), this);
//        manager.registerEvents(new ShopSignListener(this), this);
//        manager.registerEvents(new SkullListener(), this);
//        manager.registerEvents(new SotwListener(this), this);
//        manager.registerEvents(new BeaconStrengthFixListener(this), this);
//        manager.registerEvents(new VoidGlitchFixListener(), this);
//        manager.registerEvents(new WallBorderListener(this), this);
//        manager.registerEvents(new WorldListener(), this);
//    }

//    private void registerCommands() {
//        getCommand("angle").setExecutor(new AngleCommand());
//        getCommand("conquest").setExecutor(new ConquestExecutor(this));
//        getCommand("economy").setExecutor(new EconomyCommand(this));
//        getCommand("eotw").setExecutor(new EotwCommand(this));
//        getCommand("event").setExecutor(new EventExecutor(this));
//        getCommand("faction").setExecutor(new FactionExecutor(this));
//        getCommand("gopple").setExecutor(new GoppleCommand(this));
//        getCommand("koth").setExecutor(new KothExecutor(this));
//        getCommand("lives").setExecutor(new LivesExecutor(this));
//        getCommand("location").setExecutor(new LocationCommand(this));
//        getCommand("logout").setExecutor(new LogoutCommand(this));
//        getCommand("mapkit").setExecutor(new MapKitCommand(this));
//        getCommand("pay").setExecutor(new PayCommand(this));
//        getCommand("pvptimer").setExecutor(new PvpTimerCommand(this));
//        getCommand("regen").setExecutor(new RegenCommand(this));
//        getCommand("servertime").setExecutor(new ServerTimeCommand(this));
//        getCommand("sotw").setExecutor(new SotwCommand(this));
//        getCommand("spawncannon").setExecutor(new SpawnCannonCommand(this));
//        getCommand("staffrevive").setExecutor(new StaffReviveCommand(this));
//        getCommand("timer").setExecutor(new TimerExecutor(this));
//        getCommand("togglecapzoneentry").setExecutor(new ToggleCapzoneEntryCommand(this));
//        getCommand("togglelightning").setExecutor(new ToggleLightningCommand(this));
//        getCommand("togglesidebar").setExecutor(new ToggleSidebarCommand(this));
//
//        Map<String, Map<String, Object>> map = getDescription().getCommands();
//        for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
//            PluginCommand command = getCommand(entry.getKey());
//            command.setPermission("hcf.command." + entry.getKey());
//            command.setPermissionMessage(ChatColor.RED + "You do not have permission for this command.");
//        }
//    }

}
