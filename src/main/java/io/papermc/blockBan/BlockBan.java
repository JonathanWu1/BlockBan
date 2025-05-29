package io.papermc.blockBan;
import com.ibm.icu.text.Normalizer2;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.blacklist.Blacklist;
import com.sk89q.worldguard.blacklist.event.BlockBreakBlacklistEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.protection.events.DisallowedPVPEvent;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.luckperms.api.node.types.PermissionNode;
import org.antlr.v4.tool.ast.ActionAST;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Candle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.events.Event;

import javax.management.monitor.StringMonitor;
import java.nio.file.attribute.AttributeView;
import java.util.*;

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

    private Dictionary<String, BanGroups> banGroupsDictionary ;
    private boolean Debug = false;
    @Override
    public void onLoad(){
        registerFlag();
        parseConfig(); 
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
                    reload();
                    sender.sendMessage("Config Reloaded");
                    break;
                case "list":
                    for (Iterator<BanGroups> it = banGroupsDictionary.elements().asIterator(); it.hasNext(); ) {
                        var f = it.next();
                        sender.sendMessage("--------------------");
                        sender.sendMessage(f.getComponentString());
                    }
                    break;
            }
        }
        return true;
    }
    public boolean parseConfig(){
        banGroupsDictionary = new Hashtable<>();

        var flags = this.getConfig().getConfigurationSection("flags").getKeys(false);
        getLogger().info(flags.toString());
        for(var key : flags) {
            var configSection = this.getConfig().getConfigurationSection("flags." + key);
            if(configSection.getKeys(false).contains("place")) {
                var k = getFlagKey(key, BlockAction.PLACE);
                getLogger().info(k);
                banGroupsDictionary.put(k, new BanGroups(k,configSection.getConfigurationSection("place")));
            }
            if(configSection.getKeys(false).contains("break")) {
                var k = getFlagKey(key, BlockAction.BREAK);
                getLogger().info(k);
                banGroupsDictionary.put(k, new BanGroups(k, configSection.getConfigurationSection("break")));
            }
        }
        return true;
        
    } 
    public boolean reload(){
        this.reloadConfig();
        return parseConfig();
    }
    public boolean hasFlagOverride(Player player, String flagName){
        return player.hasPermission(LUCKPERMS_BASE+ flagName);
    }
    private String getFlagKey(String flagName, BlockAction action){
        return flagName + "_" + action.toString().toLowerCase();
    }
    public boolean preventBlockAction(BlockEvent event, Player player, BlockAction action){
        var regions = Helper.getRegions(event);
        var blockName = event.getBlock().getType().toString();
         
        for(var region : regions) {
            //Check if the region has a name in the BlockBan flag if not the skip the region
            String flagName = region.getFlag(FLAG_KEY);
            var flag = banGroupsDictionary.get(getFlagKey(flagName, action));
            if(flag == null) {
                continue;
            }
            
            if(hasFlagOverride(player, flagName)){
                if(Debug) {
                    getLogger().info("User permission found");
                }
                continue;
            }
                 
            return flag.preventPlacement(blockName);
        }
        return false; 
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.getPlayer().isOp())
            return;

        if (event.getBlockPlaced().getBlockData() instanceof Candle candle && candle.isLit())
            return;

        if(preventBlockAction(event, event.getPlayer(), BlockAction.PLACE))
        {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§4[BlockBan] §fThis block is restricted from being placed in this region.");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getPlayer().isOp())
            return;

        if(preventBlockAction(event, event.getPlayer(), BlockAction.BREAK))
        {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§4[BlockBan] §fThis block is restricted from being broken in this region.");
        }
    }

    private void registerFlag(){
        String key = "BlockBan";
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
