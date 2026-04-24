package us.timinc.mc.cobblemon.droploottables.handler

import com.cobblemon.mod.common.api.drop.DropEntry
import com.cobblemon.mod.common.api.drop.ItemDropEntry
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionCompleteEvent
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.MOD_ID
import us.timinc.mc.cobblemon.droploottables.api.DropHandler
import us.timinc.mc.cobblemon.droploottables.api.DropTarget
import us.timinc.mc.cobblemon.droploottables.api.extension.buildItem
import us.timinc.mc.cobblemon.droploottables.dropper.EvolvedDropper
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerEnderChestDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonEntityDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonHeldItemDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonHeldItemReplaceDropTarget
import java.util.UUID

object EvolvedHandler : DropHandler<EvolvedDropper.Context, EvolvedDropper, EvolutionCompleteEvent> {
    val baseDrops: MutableMap<UUID, MutableList<DropEntry>> = mutableMapOf()
    val whoEvolvingWho: MutableMap<UUID, UUID> = mutableMapOf()

    override val dropperTypeId: ResourceLocation = DropLootTables.DataKeys.DropperTypes.EVOLVED

    override fun getContext(evt: EvolutionCompleteEvent): EvolvedDropper.Context =
        EvolvedDropper.Context(
            getLevel(evt)!!,
            evt.pokemon,
            evt.pokemon.getOwnerPlayer()!!,
            evt.sourcePokemon
        )

    override fun getLevel(evt: EvolutionCompleteEvent): ServerLevel? =
        (evt.pokemon.entity?.level() ?: evt.pokemon.getOwnerPlayer()?.level()) as? ServerLevel

    override val dropTargetTypes: MutableMap<ResourceLocation, (evt: EvolutionCompleteEvent) -> DropTarget?> =
        mutableMapOf(
            DropLootTables.DataKeys.DropTargetTypes.PLAYER_ENDER_STORAGE to { evt ->
                evt.pokemon.getOwnerPlayer()?.let(::PlayerEnderChestDropTarget)
            },
            DropLootTables.DataKeys.DropTargetTypes.PLAYER_INVENTORY to { evt ->
                evt.pokemon.getOwnerPlayer()?.let(::PlayerDropTarget)
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

    fun registerDropTargetType(id: ResourceLocation, getter: (evt: EvolutionCompleteEvent) -> DropTarget?) {
        dropTargetTypes[id] = getter
    }

    override val selectedDropTargetTypes: List<ResourceLocation>
        get() = DropLootTables.config.evolutionDropTargets.map { it.asIdentifierDefaultingNamespace(MOD_ID) }

    override fun isRelevantEvent(evt: EvolutionCompleteEvent): Boolean =
        super.isRelevantEvent(evt)
                && evt.pokemon.getOwnerPlayer() != null

    override fun processOtherDrops(evt: EvolutionCompleteEvent): List<ItemStack> {
        val ctx = getContext(evt)
        val droppers = getDroppers(ctx) ?: emptyList()
        if (!droppers.isEmpty() && !droppers.any(EvolvedDropper::preserveBaseDrops)) return emptyList()

        val caughtBaseDrops = evt.pokemon.uuid.let(baseDrops::get) ?: emptyList()

        return caughtBaseDrops.mapNotNull { baseDrop ->
            if (baseDrop !is ItemDropEntry) {
                val pos = evt.pokemon.entity?.position() ?: return@mapNotNull null
                baseDrop.drop(evt.pokemon.entity, ctx.level, pos, evt.pokemon.getOwnerPlayer())
                return@mapNotNull null
            }

            baseDrop.buildItem(ctx.level)
        }
    }

    override fun processLegacyDrops(evt: EvolutionCompleteEvent) =
        getLegacyDrops(evt.pokemon.form, "evolve", getContext(evt).toLootParams(), getLevel(evt)!!)

    override fun cleanup(evt: EvolutionCompleteEvent, drops: MutableList<ItemStack>) {
        evt.pokemon.getOwnerUUID()?.let(whoEvolvingWho::remove)
        baseDrops.remove(evt.pokemon.uuid)
    }
}