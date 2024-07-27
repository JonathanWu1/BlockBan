package io.papermc.blockBan;

import com.google.common.collect.SetMultimap;
import it.unimi.dsi.fastutil.shorts.ShortRBTreeSet;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.security.KeyFactorySpi;
import java.security.KeyStore;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BanGroups{
    String banGroupKey = "";
    boolean isLoaded = false;
    PermissionMode mode = PermissionMode.NONE;
    ConfigurationSection configurationSection;
    Set<String> blocks = new HashSet<>();
    Set<String> startsWith = new HashSet<>();
    Set<String> endsWith = new HashSet<>();
    Set<String> contains = new HashSet<>();
    
    public void clear(){
        isLoaded = false;
        blocks.clear();
        startsWith.clear();
        endsWith.clear();;
        contains.clear();
        mode = PermissionMode.NONE;
    } 
    public void ParseConfig(){
        var keys = configurationSection.getKeys(true);
        if(keys.contains("mode")) {
            mode = Helper.getMode(configurationSection.getString("mode"));
        }
        
        if(keys.contains("starts with")) {
            startsWith = new HashSet<>(configurationSection.getStringList("starts with").stream()
                    .map(s -> s.trim().toUpperCase().replace(" ", "_"))
                    .collect(Collectors.toSet()));
        }
        
        if(keys.contains("ends with")) {
            endsWith = new HashSet<>(configurationSection.getStringList("ends with").stream()
                    .map(s -> s.trim().toUpperCase().replace(" ", "_"))
                    .collect(Collectors.toSet()));
        }
        
        if(keys.contains("contains")) {
            contains = new HashSet<>(configurationSection.getStringList("contains").stream()
                    .map(s -> s.trim().toUpperCase().replace(" ", "_"))
                    .collect(Collectors.toSet()));
        }
        
        if(keys.contains("blocks")) {
            blocks = new HashSet<>(configurationSection.getStringList("blocks").stream()
                    .map(s -> s.trim().toUpperCase().replace(" ", "_"))
                    .collect(Collectors.toSet()));
        }
    }
    public void setConfigurationSection(ConfigurationSection section)
    {
        clear();
        configurationSection = section;
    }
    public Component getComponentString(){
        
        var s = new StringBuilder();
        s = s.append("Key: ").append(banGroupKey)
                .append("\nMode: ").append(mode.toString())
                .append("\nCached Blocks: ").append(blocks.toString())
                .append("\nStarts With: ").append(startsWith.toString())
                .append("\nEnds With: ").append(endsWith.toString())
                .append("\nContains: ").append(contains.toString());
                
        
        return Component.text(s.toString());
    }
    public boolean preventPlacement(String blockName){
        if(!isLoaded) ParseConfig();
        if(mode == PermissionMode.NONE) return false;
        blockName = blockName.toUpperCase().trim().replace(" ", "_");
        if(blocks.contains(blockName)) {
            return mode == PermissionMode.BLACKLIST ;
        }
        
        if(startsWith.stream().anyMatch(blockName::startsWith)) {
            blocks.add(blockName);
            return mode == PermissionMode.BLACKLIST;
        }
        
        if(endsWith.stream().anyMatch(blockName::endsWith)) {
            blocks.add(blockName);
            return mode == PermissionMode.BLACKLIST;
        }
        
        if(contains.stream().anyMatch(blockName::contains)) {
            blocks.add(blockName);
            return mode == PermissionMode.BLACKLIST;
        }
        
        return mode == PermissionMode.WHITELIST;
    }
    public BanGroups(String key, ConfigurationSection section){
        this.banGroupKey  = key;
        setConfigurationSection(section); 
    }
}
