package us.timinc.mc.cobblemon.droploottables.handler

import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.MOD_ID
import us.timinc.mc.cobblemon.droploottables.api.DropHandler
import us.timinc.mc.cobblemon.droploottables.api.DropTarget
import us.timinc.mc.cobblemon.droploottables.dropper.VictoryDropper
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerEnderChestDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonEntityDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonHeldItemDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonHeldItemReplaceDropTarget
import us.timinc.mc.cobblemon.droploottables.event.SingleVictoryEvent

object VictoryHandler : DropHandler<VictoryDropper.Context, VictoryDropper, SingleVictoryEvent> {
    override val dropperTypeId: ResourceLocation = DropLootTables.DataKeys.DropperTypes.VICTORY

    override val dropTargetTypes: MutableMap<ResourceLocation, (evt: SingleVictoryEvent) -> DropTarget?> =
        mutableMapOf(
            DropLootTables.DataKeys.DropTargetTypes.PLAYER_ENDER_STORAGE to { evt ->
                evt.winner.getOwnerPlayer()?.let(::PlayerEnderChestDropTarget)
            },
            DropLootTables.DataKeys.DropTargetTypes.PLAYER_INVENTORY to { evt ->
                evt.winner.getOwnerPlayer()?.let(::PlayerDropTarget)
            },
            DropLootTables.DataKeys.DropTargetTypes.POKEMON_WORLD_POSITION to { evt ->
                evt.loser.entity?.let(::PokemonEntityDropTarget)
            },
            DropLootTables.DataKeys.DropTargetTypes.POKEMON_HELD_ITEM to { evt ->
                PokemonHeldItemDropTarget(evt.winner)
            },
            DropLootTables.DataKeys.DropTargetTypes.POKEMON_HELD_ITEM_REPLACE to { evt ->
                PokemonHeldItemReplaceDropTarget(evt.winner)
            },
        )

    override val selectedDropTargetTypes: List<ResourceLocation>
        get() = DropLootTables.config.victoryDropTargets.map { it.asIdentifierDefaultingNamespace(MOD_ID) }

    fun registerDropTargetType(id: ResourceLocation, getter: (evt: SingleVictoryEvent) -> DropTarget?) {
        dropTargetTypes[id] = getter
    }

    override fun getContext(evt: SingleVictoryEvent): VictoryDropper.Context = VictoryDropper.Context(
        getLevel(evt)!!,
        evt.winner,
        evt.loser,
        evt.battle,
    )

    override fun getLevel(evt: SingleVictoryEvent): ServerLevel? =
        evt.battle.players.firstNotNullOfOrNull(ServerPlayer::level) as? ServerLevel

    override fun processLegacyDrops(evt: SingleVictoryEvent) =
        getLegacyDrops(evt.winner.form, "victory", getContext(evt).toLootParams(), getLevel(evt)!!)
}