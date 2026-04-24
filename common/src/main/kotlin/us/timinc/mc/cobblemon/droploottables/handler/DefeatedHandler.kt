package us.timinc.mc.cobblemon.droploottables.handler

import com.cobblemon.mod.common.api.drop.DropEntry
import com.cobblemon.mod.common.api.drop.ItemDropEntry
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.MOD_ID
import us.timinc.mc.cobblemon.droploottables.api.DropHandler
import us.timinc.mc.cobblemon.droploottables.api.DropTarget
import us.timinc.mc.cobblemon.droploottables.api.extension.buildItem
import us.timinc.mc.cobblemon.droploottables.dropper.DefeatedDropper
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerEnderChestDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonEntityDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonHeldItemDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonHeldItemReplaceDropTarget
import us.timinc.mc.cobblemon.droploottables.event.SingleDefeatEvent
import java.util.UUID

object DefeatedHandler : DropHandler<DefeatedDropper.Context, DefeatedDropper, SingleDefeatEvent> {
    val baseDrops: MutableMap<UUID, List<DropEntry>> = mutableMapOf()

    override val dropperTypeId: ResourceLocation = DropLootTables.DataKeys.DropperTypes.DEFEATED

    override val dropTargetTypes: MutableMap<ResourceLocation, (evt: SingleDefeatEvent) -> DropTarget?> =
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
        get() = DropLootTables.config.defeatedDropTargets.map { it.asIdentifierDefaultingNamespace(MOD_ID) }

    fun registerDropTargetType(id: ResourceLocation, getter: (evt: SingleDefeatEvent) -> DropTarget?) {
        dropTargetTypes[id] = getter
    }

    override fun getContext(evt: SingleDefeatEvent): DefeatedDropper.Context = DefeatedDropper.Context(
        getLevel(evt)!!,
        evt.loser,
        evt.winner,
        evt.battle,
    )

    override fun getLevel(evt: SingleDefeatEvent): ServerLevel? =
        evt.battle.players.firstNotNullOfOrNull(ServerPlayer::level) as? ServerLevel

    override fun processOtherDrops(evt: SingleDefeatEvent): List<ItemStack> {
        val ctx = getContext(evt)
        val droppers = getDroppers(ctx) ?: emptyList()
        if (!droppers.isEmpty() && !droppers.any(DefeatedDropper::preserveBaseDrops)) return emptyList()

        val caughtBaseDrops = baseDrops[evt.loser.uuid] ?: emptyList()

        return caughtBaseDrops.mapNotNull { baseDrop ->
            if (baseDrop !is ItemDropEntry) {
                val pos = evt.loser.entity?.position() ?: return@mapNotNull null
                baseDrop.drop(evt.winner.entity, ctx.level, pos, evt.winner.getOwnerPlayer())
                return@mapNotNull null
            }

            baseDrop.buildItem(ctx.level)
        }
    }

    override fun processLegacyDrops(evt: SingleDefeatEvent) =
        getLegacyDrops(evt.winner.form, "ko", getContext(evt).toLootParams(), getLevel(evt)!!)

    override fun cleanup(evt: SingleDefeatEvent, drops: MutableList<ItemStack>) {
        baseDrops.remove(evt.winner.uuid)
    }
}