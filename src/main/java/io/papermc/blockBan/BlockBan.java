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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.events.Event;

import java.nio.file.attribute.AttributeView;
import java.util.HashSet;
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
    private static final String LUCKPERMS_BASE = "blockban.";

    private boolean Debug = false;
    @Override
    public void onLoad(){
        registerFlag();
    }

    @Override
    public void onEnable() {
        //Register Event Handler
        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("blockban").setExecutor(this);
        this.saveDefaultConfig();

        Debug = this.getConfig().getBoolean("debug");

        var keys = Helper.getFlags(this.getConfig());
        if(keys != null)
        {
            for(var key : keys){
                PermissionNode pn = PermissionNode.builder("blockban." + key).build();
            }
            AvailableFlags = keys;
        }
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
                    break;
                case "list":
                    sender.sendMessage("Available BanGroups: \n" + String.join("\n", AvailableFlags));
                    break;
            }
        }
        return true;
    }
    public boolean hasFlagOverride(Player player, String flagName){
        return player.hasPermission(LUCKPERMS_BASE+ flagName);
    }

    public boolean preventBlockAction(BlockEvent event, Player player, BlockAction action){
        if(Debug) {

            getLogger().info(event.getBlock().getType().toString());
        }
        var regions = Helper.getRegions(event);
        var block = event.getBlock();

        for(var region : regions) {
            String flagName = region.getFlag(FLAG_KEY);
            if(region == null || flagName == null || hasFlagOverride(player, flagName)){
                continue;
            }
            String defaultRegionValuePath = Helper.getKey(flagName, action);

            //Specified block ids have the highest priority
            var blockSection = this.getConfig().getConfigurationSection("flags."+ flagName + ".blocks");
            if(Debug) {

                getLogger().info("flag."+ flagName + ".blocks");
                if(blockSection == null)
                {
                    getLogger().info("no block found");
                }

                else {
                    getLogger().info(blockSection.getKeys(false).toString());
                }
            }
            if(blockSection != null)
            {
                if(Debug) {
                    getLogger().info("found exact match");
                    getLogger().info(blockSection.getKeys(false).toString());
                }
                if(blockSection.getKeys(false).contains(block.getType().toString()))
                {
                    if(Debug) {
                        getLogger().info("found block");
                        getLogger().info(block.getType().toString() + "." + action.toString().toLowerCase());
                    }
                    return !blockSection.getBoolean(block.getType().toString() + "." + action.toString().toLowerCase());
                }
            }

            //Check for patterns
            var groupsSection = this.getConfig().getConfigurationSection("flags." + flagName + ".groups");
            if(groupsSection != null)
            {
                var keys = groupsSection.getKeys(false);
                for(var key : keys)
                {
                    var group = groupsSection.getConfigurationSection(key);
                    if(group != null)
                    {
                        var groupKeys = group.getKeys(false);
                        for(var groupKey : groupKeys){
                            switch(key){
                                case "starts with":
                                    if(block.getType().name().startsWith(groupKey.toUpperCase()))
                                    {
                                        var groupAction = group.getString(Helper.getGroupKey(groupKey, action));
                                        getLogger().info(groupAction);
                                        if(groupAction != null){
                                            return !Boolean.parseBoolean(groupAction);
                                        }
                                    }
                                    break;
                                case "ends with":
                                    if(block.getType().name().endsWith(groupKey.toUpperCase()))
                                    {
                                        var groupAction = group.getString(Helper.getGroupKey(groupKey, action));
                                        getLogger().info(groupAction);
                                        if(groupAction != null){
                                            return !Boolean.parseBoolean(groupAction);
                                        }
                                    }
                                    break;
                                case "contains":
                                    if(block.getType().name().contains(groupKey.toUpperCase()))
                                    {
                                        var groupAction = group.getString(Helper.getGroupKey(groupKey, action));
                                        getLogger().info(groupAction);
                                        if(groupAction != null){
                                            return !Boolean.parseBoolean(groupAction);
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                }
            }

            boolean defaultRestriction = this.getConfig().getBoolean(defaultRegionValuePath);
            if(!defaultRestriction)
                return true;
        }
        return false;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.getPlayer().isOp())
            return;
        if(preventBlockAction(event, event.getPlayer(), BlockAction.PLACE))
        {
            event.setCancelled(true);
            event.getPlayer().sendMessage("[BlockBan] This block is restricted from being placed in this region.");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getPlayer().isOp())
            return;
        if(preventBlockAction(event, event.getPlayer(), BlockAction.BREAK))
        {
            event.setCancelled(true);
            event.getPlayer().sendMessage("[BlockBan] This block is restricted from being broken in this region.");
        }
    }

    private void registerFlag(){
        String key = "BanBlocks";
        try {
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            StringFlag flag = new StringFlag(key);
            registry.register(flag);
            FLAG_KEY = flag;
        } catch (FlagConflictException e) {
            getLogger().warning("Another plugin is using the flag " + key + "please use another name");
        }
    }
}
