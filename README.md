# Drop Loot Tables

v1.7.3-1.9

[Modrinth](https://modrinth.com/mod/cobblemon-droploottables)

[CurseForge](https://www.curseforge.com/minecraft/mc-mods/cobblemon-droploottables)

[GitHub](https://github.com/timinc-cobble/cobblemon-drop-loot-tables)

## What if…

…Cobblemon used good ol’ vanilla Minecraft loot tables?

## Features

- Adds the ability to create loot tables for various events in the Cobblemon mod:
    - When a Pokémon’s captured.
    - When a Pokémon defeats another in a Pokémon battle.
    - When a Pokémon evolves.
    - When a player hatches a Pokémon from an egg.
    - When a Pokémon is killed in the world.
    - When a player releases a Pokémon from their PC.
    - When a Pokémon is resurrected using the fossil revival machine.
    - When a player chooses a starter Pokémon (great for starter-based starter kits!).
    - When a Pokémon ticks in the world (optimized to be cheap).
    - When a Pokémon participates in a battle on the victors’ side.
    - When a Pokémon levels up.
- Adds a bunch of Cobblemon-relevant conditions:
    - Match a Pokémon or any Pokémon on a team, leveraging the power of `PokemonMatcher`s from [Tim Core](https://www.notion.so/Tim-Core-22057e0d4afd809b9c02e78f26805376?pvs=21).
    - Match the PokeBall a Pokémon’s caught with.
    - Match a player’s Pokedex knowledge on a Pokémon.
    - Match a player’s current [Counter](https://www.notion.so/Counter-21d57e0d4afd80d0815fc97b89368998?pvs=21) values.
- Drop stuff where you want stuff to drop, per-dropper type, with an override per dropper.
    - Drop it in the player’s inventory.
    - Drop it in the player’s ender storage.
    - Drop it next to the Pokémon entity.
    - Give it to the Pokémon as a held item.
    - Replace the Pokémon’s held item.
- Completely customizable with datapacks.
- Easy to add new parts for droppers, event handlers, conditions, and drop targets in your own mod.
- A new secondary layer between the event and the loot table makes the system much more malleable, replacing the old `/species/form` pattern.
- Preserves Cobblemon-given drops if instructed to for things Cobblemon already drops for.

## Dependencies

- [Cobblemon](https://www.notion.so/Cobblemon-22157e0d4afd80a49896c70a775a3c7f?pvs=21)
- [Cobblemon Tim Core](https://www.notion.so/Tim-Core-22057e0d4afd809b9c02e78f26805376?pvs=21)

## Testing

Check out the demo pack below. It has the following basic examples:

- Capturing a ghost-type Pokémon in the nether with a Dusk Ball gives a ghast tear directly to the capturing player’s inventory.
- Defeating a Kingambit in a Pokémon battle with either a Bisharp or Pawniard drops a stick named “Leader Crest” to the Bisharp/Pawniard owner’s inventory.
- Evolving an Eevee gives a type gem of the same type as the evolution (Electric Gem for Jolteon, Water Gem for Vaporeon, etc).
- Hatching a Milcery gives a random sweet. You’ll need a hatching mod, such as [Cobbreeding](https://www.notion.so/Cobbreeding-2ea57e0d4afd80a2be31dd5422178c07?pvs=21) as breeding’s not a thing yet in base mod.
- A Pidgey dying while it’s on fire will drop cooked chicken.
- Leveling up any Pokémon to level 50 will give the Pokémon’s owner a Fresh Start Mochi.
- Releasing a Pokemon below level 50 will grant an Exp. Candy S, between 51 and 75 an Exp. Candy M, and above 75 an Exp. Candy L.
- Resurrecting a Pokémon using the fossil revival machine has a chance to give another fossil of that generation.
- Picking a starter gives the player a starter kit matching the color of the type of the Pokémon picked.
- The Pidgey line will drop a feather or a random Pokémon feather every 20 ticks and emit a wing flapping sound.
- A Shuckle participating in battle on the winning side while holding a berry will turn that berry into berry juice.

[Demo Pack](https://www.notion.so/Demo-Pack-34c57e0d4afd8149860bdee4fc837012?pvs=21)

## Player Help

[How it works](https://www.notion.so/How-it-works-34c57e0d4afd812183d9f139ac3cd084?pvs=21)

[Config Options](https://www.notion.so/Config-Options-34c57e0d4afd8129895fda9177bb1809?pvs=21)

## Addon Dev Help

### Data Pack Help

[Making an Addon](https://www.notion.so/Making-an-Addon-34c57e0d4afd8134ba3dcaf79236e928?pvs=21)

## Mod Dev Help

[Make your own dropper type](https://www.notion.so/Make-your-own-dropper-type-34c57e0d4afd8180aecdc8858680d0d6?pvs=21)

[Make your own condition](https://www.notion.so/Make-your-own-condition-34c57e0d4afd81c5ac7bdd9e5bbc241a?pvs=21)

[Register a new drop target](https://www.notion.so/Register-a-new-drop-target-34c57e0d4afd81048791d7f746eaa6cd?pvs=21)

## Parts

[Drop Target Types](https://www.notion.so/Drop-Target-Types-34c57e0d4afd81778401cebf556a76cf?pvs=21)

[Dropper Type](https://www.notion.so/Dropper-Type-34c57e0d4afd819bad09cbe60f357488?pvs=21)

[Drop Conditions](https://www.notion.so/Drop-Conditions-34c57e0d4afd818992a6e65dc2025b00?pvs=21)

## Known Issues

- ≤v1.7.3-1.9.0 had an issue with trying to run the tick dropper too early and getting a null for its own ID. Yeah, doesn’t make sense to me either. ≥v1.7.3-1.9.1 fixes this by allowing for the ID to be null, and safely failing by not finding any droppers if that’s the case.

## Roadmap

If you’d like to keep up with the work being done on the mod, please join [the Discord](https://discord.com/invite/WKAR27SdSv) and subscribe to notifications on the channel for this content. You can also keep track of the to do list available on [the mod’s main page](https://www.notion.so/Drop-Loot-Tables-21d57e0d4afd80809bbbf3c38975664e?pvs=21).

## Feedback

If you have any questions or requests concerning the mod, or just want to drop by and say hi, visit us over at [the Discord](https://discord.com/invite/WKAR27SdSv)!

## Support

If I've made something you enjoyed or helped you make something, please consider [dropping a tip in the cup](https://ko-fi.com/timsminecraftmods) and mention how I helped if you'd like!