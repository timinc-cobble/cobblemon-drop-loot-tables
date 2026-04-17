package us.timinc.mc.cobblemon.droploottables.handler

import com.cobblemon.mod.common.api.events.pokemon.HatchEggEvent
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.MOD_ID
import us.timinc.mc.cobblemon.droploottables.api.DropHandler
import us.timinc.mc.cobblemon.droploottables.api.DropTarget
import us.timinc.mc.cobblemon.droploottables.dropper.HatchedDropper
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerEnderChestDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonHeldItemDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonHeldItemReplaceDropTarget

object HatchedHandler : DropHandler<HatchedDropper.Context, HatchedDropper, HatchEggEvent.Post> {
    override val dropperTypeId: ResourceLocation = DropLootTables.DataKeys.DropperTypes.HATCHED

    override fun getContext(evt: HatchEggEvent.Post): HatchedDropper.Context = HatchedDropper.Context(
        evt.player.level() as ServerLevel,
        evt.pokemon,
        evt.player
    )

    override fun getLevel(evt: HatchEggEvent.Post): ServerLevel = evt.player.level() as ServerLevel

    override val dropTargetTypes: MutableMap<ResourceLocation, (evt: HatchEggEvent.Post) -> DropTarget?> =
        mutableMapOf(
            DropLootTables.DataKeys.DropTargetTypes.PLAYER_ENDER_STORAGE to { evt ->
                PlayerEnderChestDropTarget(evt.player)
            },
            DropLootTables.DataKeys.DropTargetTypes.PLAYER_INVENTORY to { evt ->
                PlayerDropTarget(evt.player)
            },
            DropLootTables.DataKeys.DropTargetTypes.POKEMON_HELD_ITEM to { evt ->
                PokemonHeldItemDropTarget(evt.pokemon)
            },
            DropLootTables.DataKeys.DropTargetTypes.POKEMON_HELD_ITEM_REPLACE to { evt ->
                PokemonHeldItemReplaceDropTarget(evt.pokemon)
            },
        )

    fun registerDropTargetType(id: ResourceLocation, getter: (evt: HatchEggEvent.Post) -> DropTarget?) {
        dropTargetTypes[id] = getter
    }

    override val selectedDropTargetTypes: List<ResourceLocation>
        get() = DropLootTables.config.hatchedDropTargets.map { it.asIdentifierDefaultingNamespace(MOD_ID) }

    override fun processLegacyDrops(evt: HatchEggEvent.Post) =
        getLegacyDrops(evt.pokemon.form, "hatch", getContext(evt).toLootParams(), getLevel(evt))
}