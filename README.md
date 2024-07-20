# BlockBan

## Dependencies
- WorldGuard
- Luckperms

## Usage
Create a file `config.yml` in `./plugins/BlockBan` or run the server with the plugin installed and it will autogenerate this file
You can find the Spigot block ids here: [Block Ids](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html)

```
debug: true
flags:
  <flag name>:
    default:
      break: false
      place: false
    groups:
      starts with:
        <some string>:
            break: false
            place: false
      ends with:
        <some string>:
            break: false
      contains:
        <some string>:
            break: false
    blocks:
      <BLOCK_ID>:
        break: true
        place: false
```
### Config.yml
`<flag_name>` : Name of the flag, this is used to set the flag in Worldguard as well as the perm in Luckperms
`break`: value to allow(true) or disallow(false) breaking for all blocks<br/>
`place`: value to allow(true) or disallow(false) for all blocks<br/>
`default`: default value to check if all other checks fail <br/>
`groups`: allow/disallow blocks based on the substring value. <br/>
`starts with`: matches to the start of the block type<br/>
    `ends with`: matches to the ends of the block type<br/>
    `contains`: matches substring to any part of the block type <br/>
`<BLOCK_ID>` : the spigot block Id, this has priority above all other checks 

### WorldGuard
Set the flag for a region using /rg flag -w <world_name> <region_name> BanBlocks <flag_name>

### Luckperms
In Luckperms set the permissin using `blockban.*` or `blockban.<flag_name>`. Setting the perm to `true` will allow the user/group to bypass the placement or breaking restrictions
