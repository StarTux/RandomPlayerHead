# RandomPlayerHead
Spawn random (or optionally specific) player heads from your own list of heads

Heads not included! Put them in a directory named "heads" in your plugin's data folder. It should contain one or more YAML files, each with a list of the following structure.

```yaml
heads:
- Name: 'Name of your head'
  Id: 'UUID of your head'
  Texture: 'The texture string, SkullOwner.Properties.textures.Value from the original data tag'
- 'And so on and so forth.'
```

Heads will be loaded when the plugin is enabled or you type '/rph -reload'.

Commands:
- __/rph &lt;Player Name&gt;__ Give the named player a random head.
- __/rph &lt;Player Name&gt; &lt;Head Name&gt;__ Give a player specific heads. __Beware!__ *All* heads with the same name will be spawned!
- __/rph -reload__ Reload the heads files.
- __/rph -search &lt;Search Term&gt;__ Search for heads by name.
