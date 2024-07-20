package io.papermc.blockBan;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
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
    public static String getGroups(String key){ //        getLogger().info("flags."+ flagName + ".default place");
        return String.format("flags.%s.groups.", key);
    }
    public static String getGroupKey(String key,BlockAction action) {
        return String.format("%s.%s", key, action.toString().toLowerCase());
    }
    public static String getKey(String flagName, BlockAction action){ //        getLogger().info("flags."+ flagName + ".default place");
        return String.format("flags.%s.default.%s", flagName, action.toString().toLowerCase());
    }
    public static String getKey(String flagName, BlockAction action, Block block)
    {
        return String.format("flags.%s.%s.%s", flagName, block.getType().toString().toUpperCase(), action.toString().toLowerCase());
    }
}
