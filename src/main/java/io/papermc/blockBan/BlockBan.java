package io.papermc.blockBan;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.attribute.AttributeView;
import java.util.Set;

//Call it BlockBan or BlockGuard
//Change to flags
//add flags must use exact string
//add /list command
//Permission nodes that work with luckperms

//anyone with the permission can override the break/place check
//Op can always do whatever
//Feedback for why something isnt breaking or placing

public class BlockBan extends JavaPlugin implements Listener, CommandExecutor {
    private StringFlag FLAG_KEY;
    private Set<String> AvailableFlags = null;
    @Override
    public void onLoad(){
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        String key = "BanBlocks";
        try {
            // create a flag with the name "my-custom-flag", defaulting to true
            StringFlag flag = new StringFlag(key);
            registry.register(flag);
            FLAG_KEY = flag;
        } catch (FlagConflictException e) {
            getLogger().warning("Another plugin is using the flag " + key + "please use another name");
        }
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("blockban").setExecutor(this);
        this.saveDefaultConfig();
        var keys = getAvailableFlags();
        if(keys != null)
        {
            for(var key : keys){
                PermissionNode pn = PermissionNode.builder("blockban." + key).build();
            }
            AvailableFlags = keys;
        }
    }

    private Set<String> getAvailableFlags(){
        Set<String> keys = null;

        var config = this.getConfig().getConfigurationSection("flags");
        if(config == null)
        {
            return null;
        }
        keys = config.getKeys(false);
        return keys;
    }
    private Set<ProtectedRegion> getRegions(BlockEvent event){
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(event.getBlock().getLocation().toBlockLocation()));
        return set.getRegions();
    }
    private String getDefaultPlacementValuePath(String flagName){
//        getLogger().info("flags."+ flagName + ".default place");
        return "flags."+ flagName + ".default place";
    }
    private String getDefaultBreakValuePath(String flagName){
//        getLogger().info("flags."+ flagName + ".default break");
        return "flags."+ flagName + ".default break";
    }
    private String getPlacementValuePath(String flagName, Block block)
    {
//        getLogger().info("flags."+ flagName + "." + block.getType() + ".place");
        return "flags."+ flagName + "." + block.getType() + ".place";
    }
    private String getBreakValuePath(String flagName, Block block)
    {
//        getLogger().info("flags."+ flagName + "." + block.getType() + ".break");
        return "flags."+ flagName + "." + block.getType() + ".break";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(label.equals("blockban"))
        {
            switch(args[0])
            {
                case "reload":
                    this.reloadConfig();
                    sender.sendMessage("Config Reloaded");
                    var keys = getAvailableFlags();
                    if(keys != null && AvailableFlags != null)
                    {
                        if(keys.size() != AvailableFlags.size())
                        {
                            sender.sendMessage("New flags found in the config, please restart the server to load them into worldguard");
                        }
                        getLogger().info(keys.toString());
                        getLogger().info(AvailableFlags.toString());
                        keys.removeAll(AvailableFlags);
                        getLogger().info(keys.toString());
                        if(!keys.isEmpty())
                        {
                            sender.sendMessage("New flags found in the config, please restart the server to load them into worldguard");
                        }
                    }
                    break;
                case "list":
                    sender.sendMessage("Available BanGroups: \n" + String.join("\n", AvailableFlags));
                    break;
            }
        }
        return true;
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.getPlayer().isOp())
            return;
        Block block = event.getBlock();
        var regions = getRegions(event);
        for(var region : regions) {
            if(region == null){
                continue;
            }
            var flags = region.getFlag(FLAG_KEY);
            if(flags != null){
                String defaultRegionValuePath = getDefaultPlacementValuePath(flags);
                String blockPath = getPlacementValuePath(flags, block);
                var defaultRestriction = this.getConfig().getBoolean(defaultRegionValuePath);
                var allowBlockPlacement = this.getConfig().getBoolean(blockPath, defaultRestriction);
                if(!allowBlockPlacement && !event.getPlayer().hasPermission("blockban." + flags))
                {
                    if(regions.size() > 1){
                        getLogger().warning("Warning: Found multiple overlapping regions,using value for " + flags);
                    }
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("[BlockBan] This block is restricted from placement in this region.");
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getPlayer().isOp())
            return;
        Block block = event.getBlock();
        var regions = getRegions(event);
        for(var region : regions) {
            if(region == null){
                continue;
            }
            var flags = region.getFlag(FLAG_KEY);
            if(flags != null){
                String defaultRegionValuePath = getDefaultBreakValuePath(flags);
                String blockPath = getBreakValuePath(flags, block);
                var defaultRestriction = this.getConfig().getBoolean(defaultRegionValuePath);
                var allowBlockBreak = this.getConfig().getBoolean(blockPath, defaultRestriction);

                if(!allowBlockBreak && !event.getPlayer().hasPermission("blockban." + flags))
                {
                    if(regions.size() > 1){
                        getLogger().warning("[Warning]: Found multiple overlapping regions,using value for " + flags);
                    }
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("[BlockBan] This block is restricted from breaking in this region.");
                }
            }
        }
    }
}
