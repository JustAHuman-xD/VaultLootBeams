# Vault Loot Beams
A fork of the Popular [Loot Beams](https://www.curseforge.com/minecraft/mc-mods/loot-beams) mod modified specifically for the VaultHunters 3rd Edition modpack.
Please note that multiple of this mod's "new" features were featurs backported from the original Loot Beams mod for newer game versions.
Thus, I cannot take full credit for those features, even if I did change or rewrite their implementation's for this fork.

# Features
## Loot Beams
Configured items dropped on the ground will have a colored loot beam projecting from them.
This beam has many configurable properties listed below.

### Rendering Requirements
- `Render Distance` - The distance at which the loot beam will render. Accepts 0-1024 blocks, defaults 24.
- `Require Ground` - If enabled, the loot beam will only render if the item is on the ground rather than in the air.
- `Whitelist Color Overrides` - If enabled, items with a color override will be automatically considered whitelisted for rendering.
- `Render Condition` - An [ItemCondition] for automatically determining if an item should render a beam. Defaults to `Lists Only`.
- `Render Whitelist` - An [ItemList] for items that should always render a loot beam.
- `Render Blacklist` - An [ItemList] for items that should never render a loot beam. (This will override the whitelist.)

### Rendering Properties
- `Beam Radius` - The radius of the loot beam, accepts 0-5, defaults 1.
- `Beam Height` - The height of the loot beam, maximum of 10, default of 2.
- `Beam Y Offset` - The vertical offset of the loot beam, maximum of 10, default of 0.
- `Beam Alpha` - The opacity of the loot beam, accepts 0-1, defaults 0.7.
- `White Beam Center` - Should the beam have an additional white inner beam, enabled by default.
- `Beam Shadow` - Should the beam have a colored shadow (replaces the vanilla item shadow), enabled by default.
- `Shadow Radius` - The radius of the shadow, accepts 0.00001-1, defaults 0.5.
- `Shadow Alpha Multiplier` - The opacity multiplier of the shadow, the shadow inherits the opacity of the beam and is multiplied by this factor, accepts 0.00001-3, defaults 1.
- `Animate Shadow` - Should the shadow animate with the bob of the item, enabled by default.
- `Beam Render Mode` - The render mode of the loot beam, this can be set to:
  - `Glowing` - The beam will render "like" lightning, making it glow (emissive) and work well with shaders. This is the default render mode.
  - `Beacon` - The beam will have a beacon-like texture, if you used the original Loot Beams mod, this was how beams were always rendered.
  - `Solid` - The beam will use solid colors.
- `Beam Color Mode` - The default color mode for loot beams, any items that don't provide a loot beam color, and don't have a configured color, will have its color determined by this mode:
  - `Default` - A static white color.
  - `Name Color` - Takes the color of the item's name.
  - `Rarity Color` - Takes the color of the item's rarity.
  - `Name or Rarity Color` - If present uses name color, otherwise uses rarity. (default)
- `Color Overrides` - A map of [ColorGroup]s for providing custom colors for specific items.

### ColorGroup
A color group associates an [ItemList] with a list of colors.
If there are multiple colors, they will be cycled between via a gradient effect.
Color groups can be defined in a number of ways:
- `Inline` - An inlined item list to a singular color, or an array of colors.
- `Full` - An identifier for the color group, with an [ItemList] and a list of colors.
Within the color group, you can define colors in the following ways:
- `Integer Color` - A single color in the form of a packed integer
  - Accepts both direct integers `11146495` or a hex integer `0xAA14FF`
- `Hex Color` - A single color in the form of a hex string
  - Accepts both `"#AA14FF"` or `"0xAA14FF"`

Full Map Example:
```json5
{
  "color_map": {
    "minecraft:diamond": 0x00FF00, // Single color for a single item
    "minecraft:*_axe": ["#FF0000", "0x00FF00"], // Multiple colors for a partial item identifier
    "favorites": { // Full color group with an item list and inlined color
      "items": [
        "minecraft:diamond",
        "minecraft:*_sword",
        "minecraft:netherite_ingot"
      ],
      "colors": 0xFFFF00
    },
    "other_favorites": { // Full color group with an item list and multiple colors
      "items": [
        "minecraft:golden_apple",
        "minecraft:enchanted_golden_apple"
      ],
      "colors": ["#FF00FF", "#00FFFF"]
    }
  }
}
```


### Loot Beam Particles
- `Beam Particles` - Should the loot beam have particles, enabled by default.
- `Particle Groups` - A map of ids to [ParticleGroup]s defining particle effects for specific item's loot beams.

### ParticleGroup
- `Particle Size` - The size of the particles, accepts 0.00001-10, defaults 0.25.
- `Particle Speed` - The speed of the particles, accepts 0.00001-10, defaults 0.1.
- `Particle Spread` - The initial spread of the particles, accepts 0.00001-10, defaults 0.05.
- `Particle Count` - The number of particles to spawn every second, accepts 1-20, defaults to 19.
- `Particle Lifetime` - The lifetime of the particles in ticks, accepts 1-100, defaults to 20, as a particle ages its opacity will decrease until it despawns.
- `Particle Condition` - An [ItemCondition] for automatically determining if an item should have particles. Defaults to `Lists Only`.
- `Particle Whitelist` - An [ItemList] for items that should always have particles.
- `Particle Blacklist` - An [ItemList] for items that should never have particles. (This will override the whitelist.)

### Beam Nameplates
Loot beams can optionally render a nameplate for the item as well, showing the name, its count (optional), and rarity (optional).<br>
- `Beam Nameplate` - Should loot beams render nameplates, enabled by default.
- `Nameplate on Look` - Should the nameplate only render when the player is looking at the item/beam, enabled by default.
- `Nameplate Look Sensitivity` - How closely the player must be looking at the item for the nameplate to render, accepts 0-5, defaults to 0.018. The larger the number the less precise the player can be.
- `Nameplate Outline` - Should the nameplate text have an outline, enabled by default.
- `Nameplate Include Count` - Should the nameplate include the item count, enabled by default.
- `Nameplate Scale` - The scale of the nameplate, accepts -10-10, defaults to 1.
- `Nameplate Y Offset` - The vertical offset of the nameplate from the item entity, accepts -30-30, defaults to 0.75.
- `Nameplate Text Alpha` - The opacity of the nameplate text, accepts 0-1, defaults to 1.
- `Nameplate Background Alpha` - The opacity of the nameplate background, accepts 0-1, defaults to 0.5.
- `Render Vanilla Rarities` - Should the nameplate render vanilla item rarities, disabled by default.
- `Custom Nameplate Rarities` - A list of custom rarities to be rendered if detected in the item's tooltip.
- `Nameplate Condition` - An [ItemCondition] for automatically determining if an item should have a nameplate. Defaults to `Lists Only`.
- `Nameplate Whitelist` - An [ItemList] for items that should always have a nameplate.
- `Nameplate Blacklist` - An [ItemList] for items that should never have a nameplate. (This will override the whitelist.)

### Loot Beam Sounds
- `Landing Sound` - Should loot beams play a sound when they land, disabled by default.
- `Sound Volume` - The volume of the landing sound, accepts 0-1, defaults to 0.3.
- `Sound Condition` - An [ItemCondition] for automatically determining if an item should play a sound when it lands. Defaults to `Lists Only`.
- `Sound Whitelist` - An [ItemList] for items that should always play a sound when they land.
- `Sound Blacklist` - An [ItemList] for items that should never play a sound when they land. (This will override the whitelist.)

### LootBeamHolder
This is a simple api interface that can be implemented by items within mods for designating an item as a loot beam holder.<br>
This allows the item to have a loot beam rendered for it without needing to be in the render whitelist or to pass the render condition while also allowing the beams color to be dynamically determined based on the item's properties.<br>
The interface has 2 methods, `shouldRenderBeam(ItemEntity, ItemStack)` and `getBeamColor(ItemEntity, ItemStack)`.<br>
You can optionally register a `LootBeamHolder` through the `LootBeamHolderRegistry` instead of implementing the interface directly.

## Config
Vault Loot Beams uses a json config file to configure its features.
You can directly edit the file and reload it in-game using the specified hotkey, or you can use the in-game config editor (opened with the specified hotkey) to modify the config.
There are a few custom config option types used for multiple config options detailed below.

### ItemCondition
A condition applied to items that will either pass or fail for the relevant config option.
- `Lists Only` - Only consider the provided whitelist and blacklist.
- `All Items` - All items pass.
- `Equipment` - Equipment items pass. (Tools, Armor, Weapons, etc.)
- `Rare Items` - Items with a rarity higher than `Common` pass.
- `Rare or Equipment` - Items that pass the `Equipment` or `Rare Items`.

### ItemList
An item list is a special type of list that is used to collect items for various config options.
Item lists can either be inlined as a singular entry, or as a full list of entries.
Entries can be formatted in a number of ways:
- Item Identifier - Targets a single item with the provided identifier, such as `minecraft:diamond`.
- Partial Item Identifier - A partial identifier indicated using a `*`, such as `minecraft:*_axe` or `minecraft:diamond_*`, partial identifiers target all items under the same namespace whose identifier starts/ends with the provided identifier.
- Item Tag - Targets all items within the provided tag, such as `minecraft:beds`.
- Mod Id - Targets all items within the provided mod, such as `minecraft` or `the_vault`.

## Vault Hunters Integration
Vault Loot Beams comes with a built-in default config addition when used with `The Vault` mod.
The integration does 2 things:
1. Implements `LootBeamHolder` for the following items, allowing for dynamic colors based on the items properties
   - Augments, Blessings, Potions, Vault Crystals, Catalysts, Inscriptions, Jewels, and Tools
2. Adds a number of Vault Hunters items & colors to the default config.
   - Whitelists all vault armor, mainhands, offhands, and curios.
   - Whitelists gemstones, keypieces, blank keys, unidentified keys, knowledge stars, and antiques.
   - Provides colors for artifacts, inscriptions, lost bounty, old notes, bounty pearls, catalysts, card deck, pogs, all treasure keys, and all seals.

## Bug Fixes & Optimizations
There are a number of new bug fixes and optimizations in this fork that should allow you to
use loot beams without any worry for performance.

[ItemCondition]: #itemcondition
[ItemList]: #itemlist
[ColorGroup]: #colorgroup
[ParticleGroup]: #particlegroup