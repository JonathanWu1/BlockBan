package io.papermc.blockBan;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.Ref;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Helper {
    public static Set<ProtectedRegion> getRegions(BlockEvent event){
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(event.getBlock().getLocation().toBlockLocation()));
        return set.getRegions();
    }
    public static Set<String> getFlags(@NotNull FileConfiguration config){
        Set<String> keys = new HashSet<>();
        var section = config.getConfigurationSection("flags");

        if(section != null)
        {
            keys = section.getKeys(false);
        }

        return keys;
    }
    public static PermissionMode getMode(String mode){
        mode = mode.toUpperCase().trim();
        if(Objects.equals(mode, PermissionMode.BLACKLIST.toString())) return PermissionMode.BLACKLIST;
        else if(Objects.equals(mode, PermissionMode.WHITELIST.toString())) return PermissionMode.WHITELIST;
        else return PermissionMode.NONE;
    }
    public static ConfigurationSection getBlocksSection(Configuration config, String flagName, BlockAction action)
    {
        return config.getConfigurationSection(String.format("flags.%s.%s", flagName, action.toString().toLowerCase()));
    }

    public static ConfigurationSection getBanGroup(Configuration config, String flagName, BlockAction action, BanGroups group)
    {
        return config.getConfigurationSection(String.format("flags.%s.%s.%s", flagName, action.toString().toLowerCase(), group.toString().toLowerCase().replace("_", " ")));
    }
    
}
