package us.timinc.mc.cobblemon.droploottables.handler

import com.cobblemon.mod.common.api.events.pokemon.FossilRevivedEvent
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.MOD_ID
import us.timinc.mc.cobblemon.droploottables.api.DropHandler
import us.timinc.mc.cobblemon.droploottables.api.DropTarget
import us.timinc.mc.cobblemon.droploottables.dropper.ResurrectedDropper
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerEnderChestDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonEntityDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonHeldItemDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonHeldItemReplaceDropTarget

object ResurrectedHandler : DropHandler<ResurrectedDropper.Context, ResurrectedDropper, FossilRevivedEvent> {
    override val dropperTypeId: ResourceLocation = DropLootTables.DataKeys.DropperTypes.RESURRECTED

    override fun getContext(evt: FossilRevivedEvent): ResurrectedDropper.Context = ResurrectedDropper.Context(
        getLevel(evt)!!,
        evt.pokemon,
        evt.player
    )

    override fun getLevel(evt: FossilRevivedEvent): ServerLevel? =
        (evt.player?.level() ?: evt.pokemon.entity?.level()) as? ServerLevel

    override val dropTargetTypes: MutableMap<ResourceLocation, (evt: FossilRevivedEvent) -> DropTarget?> =
        mutableMapOf(
            DropLootTables.DataKeys.DropTargetTypes.PLAYER_ENDER_STORAGE to { evt ->
                evt.player?.let(::PlayerEnderChestDropTarget)
            },
            DropLootTables.DataKeys.DropTargetTypes.PLAYER_INVENTORY to { evt ->
                evt.player?.let(::PlayerDropTarget)
            },
            DropLootTables.DataKeys.DropTargetTypes.POKEMON_WORLD_POSITION to { evt ->
                evt.pokemon.entity?.let(::PokemonEntityDropTarget)
            },
            DropLootTables.DataKeys.DropTargetTypes.POKEMON_HELD_ITEM to { evt ->
                PokemonHeldItemDropTarget(evt.pokemon)
            },
            DropLootTables.DataKeys.DropTargetTypes.POKEMON_HELD_ITEM_REPLACE to { evt ->
                PokemonHeldItemReplaceDropTarget(evt.pokemon)
            },
        )

    fun registerDropTargetType(id: ResourceLocation, getter: (evt: FossilRevivedEvent) -> DropTarget?) {
        dropTargetTypes[id] = getter
    }

    override val selectedDropTargetTypes: List<ResourceLocation>
        get() = DropLootTables.config.resurrectedDropTargets.map { it.asIdentifierDefaultingNamespace(MOD_ID) }

    override fun processLegacyDrops(evt: FossilRevivedEvent) =
        getLegacyDrops(evt.pokemon.form, "resurrect", getContext(evt).toLootParams(), getLevel(evt)!!)
}