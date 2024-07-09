# BlockBan

## Dependencies
- WorldGuard
- Luckperms

## Usage
Create a file `config.yml` in `./plugins/BlockBan` or run the server with the plugin installed and it will autogenerate this file
You can find the Spigot block ids here: [Block Ids](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html)

```
flags:
  <flag_name>:
    default break: true
    default place: true
    <BLOCK_ID>:
       break: false
       place: true
    <BLOCK_ID2>:
       break: false
       place: true

  <flag_name2>:
    default break: true
    default place: true
    <BLOCK_ID>:
       break: false
       place: true
```
### Config.yml
<flag_name> : Name of the flag, this is used to set the flag in Worldguard as well as the perm in Luckperms
default break: default value to allow(true) or disallow(false) breaking for all blocks
default place: default value to allow(true) or disallow(false) for all blocks
<BLOCK_ID> : the spigot block Id, this will override the default break/place values.

### WorldGuard
Set the flag for a region using /rg flag -w <world_name> <region_name> BanBlocks <flag_name>

### Luckperms
In Luckperms set the permissin using `blockban.*` or `blockban.<flag_name>`. Setting the perm to `true` will allow the user/group to bypass the placement or breaking restrictions
