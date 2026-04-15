package us.timinc.mc.cobblemon.droploottables.handler

import com.cobblemon.mod.common.api.drop.DropEntry
import com.cobblemon.mod.common.api.drop.ItemDropEntry
import com.cobblemon.mod.common.api.events.pokemon.PokemonFaintedEvent
import com.cobblemon.mod.common.api.scheduling.afterOnServer
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.TamableAnimal
import net.minecraft.world.item.ItemStack
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.MOD_ID
import us.timinc.mc.cobblemon.droploottables.api.DropHandler
import us.timinc.mc.cobblemon.droploottables.api.DropTarget
import us.timinc.mc.cobblemon.droploottables.api.extension.buildItem
import us.timinc.mc.cobblemon.droploottables.dropper.KilledDropper
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerEnderChestDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonEntityDropTarget
import java.util.*

object KilledHandler : DropHandler<KilledDropper.Context, KilledDropper, PokemonFaintedEvent> {
    val baseDrops: MutableMap<UUID, List<DropEntry>> = mutableMapOf()

    override val dropperTypeId: ResourceLocation = DropLootTables.DataKeys.DropperTypes.KILLED

    override fun getContext(evt: PokemonFaintedEvent): KilledDropper.Context =
        KilledDropper.Context(
            getLevel(evt)!!,
            evt.pokemon,
            (evt.pokemon.entity!!.lastAttacker as? ServerPlayer)
                ?: (evt.pokemon.entity!!.lastAttacker as? TamableAnimal).takeIf { it?.isTame ?: false }
        )

    override fun getLevel(evt: PokemonFaintedEvent): ServerLevel? =
        evt.pokemon.entity?.level() as? ServerLevel

    override val dropTargetTypes: MutableMap<ResourceLocation, (evt: PokemonFaintedEvent) -> DropTarget?> =
        mutableMapOf(
            DropLootTables.DataKeys.DropTargetTypes.PLAYER_ENDER_STORAGE to { evt ->
                evt.pokemon.getOwnerPlayer()?.let(::PlayerEnderChestDropTarget)
            },
            DropLootTables.DataKeys.DropTargetTypes.PLAYER_INVENTORY to { evt ->
                evt.pokemon.getOwnerPlayer()?.let(::PlayerDropTarget)
            },
            DropLootTables.DataKeys.DropTargetTypes.POKEMON_WORLD_POSITION to { evt ->
                evt.pokemon.entity?.let(::PokemonEntityDropTarget)
            }
        )

    fun registerDropTargetType(id: ResourceLocation, getter: (evt: PokemonFaintedEvent) -> DropTarget?) {
        dropTargetTypes[id] = getter
    }

    override val selectedDropTargetTypes: List<ResourceLocation>
        get() = DropLootTables.config.killedDropTargets.map { it.asIdentifierDefaultingNamespace(MOD_ID) }

    override fun isRelevantEvent(evt: PokemonFaintedEvent): Boolean =
        super.isRelevantEvent(evt) && evt.pokemon.entity?.battle == null

    override fun processOtherDrops(evt: PokemonFaintedEvent): List<ItemStack> {
        val ctx = getContext(evt)
        val droppers = getDroppers(ctx) ?: emptyList()
        if (!droppers.isEmpty() && !droppers.any(KilledDropper::preserveBaseDrops)) return emptyList()

        val caughtBaseDrops = baseDrops[evt.pokemon.uuid] ?: emptyList()

        return caughtBaseDrops.mapNotNull { baseDrop ->
            if (baseDrop !is ItemDropEntry) {
                val pos = evt.pokemon.entity?.position() ?: return@mapNotNull null
                baseDrop.drop(evt.pokemon.entity, ctx.level, pos, evt.pokemon.getOwnerPlayer())
                return@mapNotNull null
            }

            baseDrop.buildItem(ctx.level)
        }
    }

    override fun processLegacyDrops(evt: PokemonFaintedEvent): List<ItemStack> {
        return if (isRelevantEvent(evt))
            getLegacyDrops(evt.pokemon.form, "ko", getContext(evt).toLootParams(), getLevel(evt)!!)
        else
            emptyList()
    }

    override fun cleanup(evt: PokemonFaintedEvent) {
        baseDrops.remove(evt.pokemon.uuid)
    }

    override fun handle(evt: PokemonFaintedEvent) {
        getLevel(evt)?.let { level ->
            afterOnServer(1, level) {
                super.handle(evt)
            }
        }
    }
}