package us.timinc.mc.cobblemon.droploottables.handler

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import us.timinc.mc.cobblemon.timcore.codec.INT_RANGE_CODEC
import us.timinc.mc.cobblemon.timcore.event.PokemonEntityTickedEvent
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.DropLootTables.config
import us.timinc.mc.cobblemon.droploottables.MOD_ID
import us.timinc.mc.cobblemon.droploottables.api.DropHandler
import us.timinc.mc.cobblemon.droploottables.api.DropTarget
import us.timinc.mc.cobblemon.droploottables.dropper.TickedDropper
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerEnderChestDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonEntityDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonHeldItemDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonHeldItemReplaceDropTarget

object TickedHandler : DropHandler<TickedDropper.Context, TickedDropper, PokemonEntityTickedEvent> {
    override val dropperTypeId: ResourceLocation = DropLootTables.DataKeys.DropperTypes.TICKED

    override val dropTargetTypes: MutableMap<ResourceLocation, (evt: PokemonEntityTickedEvent) -> DropTarget?> =
        mutableMapOf(
            DropLootTables.DataKeys.DropTargetTypes.PLAYER_ENDER_STORAGE to { evt ->
                evt.entity.pokemon.getOwnerPlayer()?.let(::PlayerEnderChestDropTarget)
            },
            DropLootTables.DataKeys.DropTargetTypes.PLAYER_INVENTORY to { evt ->
                evt.entity.pokemon.getOwnerPlayer()?.let(::PlayerDropTarget)
            },
            DropLootTables.DataKeys.DropTargetTypes.POKEMON_WORLD_POSITION to { evt ->
                PokemonEntityDropTarget(evt.entity)
            },
            DropLootTables.DataKeys.DropTargetTypes.POKEMON_HELD_ITEM to { evt ->
                PokemonHeldItemDropTarget(evt.entity.pokemon)
            },
            DropLootTables.DataKeys.DropTargetTypes.POKEMON_HELD_ITEM_REPLACE to { evt ->
                PokemonHeldItemReplaceDropTarget(evt.entity.pokemon)
            },
        )

    override val selectedDropTargetTypes: List<ResourceLocation>
        get() = DropLootTables.config.tickedDropTargets.map { it.asIdentifierDefaultingNamespace(MOD_ID) }

    fun registerDropTargetType(id: ResourceLocation, getter: (evt: PokemonEntityTickedEvent) -> DropTarget?) {
        dropTargetTypes[id] = getter
    }

    override fun getContext(evt: PokemonEntityTickedEvent): TickedDropper.Context =
        TickedDropper.Context.fromEntity(evt.entity)

    override fun getLevel(evt: PokemonEntityTickedEvent): ServerLevel = evt.entity.level() as ServerLevel

    override fun isRelevantEvent(evt: PokemonEntityTickedEvent): Boolean = evt.entity.level() is ServerLevel

    override fun processLegacyDrops(evt: PokemonEntityTickedEvent) =
        if (isReady(evt.entity.pokemon))
            getLegacyDrops(evt.entity.pokemon.form, "periodic", getContext(evt).toLootParams(), getLevel(evt))
        else
            emptyList()

    @Deprecated("Old granular drop period setting, please do not use")
    private fun getDropTimer(pokemon: Pokemon): Int {
        val range = config.granularDropPeriods.entries.find { (k) ->
            PokemonProperties.parse(k).matches(pokemon)
        }?.value?.let {
            INT_RANGE_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(it)).getOrThrow()
        } ?: IntRange(6000, 12000)

        return range.random()
    }

    @Deprecated("Old 1.6 function, please do not use")
    private fun isReady(pokemon: Pokemon): Boolean {
        val persistentDataKeyTimer = "droploottables:periodic_timer"

        if (!pokemon.persistentData.contains(persistentDataKeyTimer)) {
            pokemon.persistentData.putInt(persistentDataKeyTimer, getDropTimer(pokemon))
            return false
        }

        val currentValue = pokemon.persistentData.getInt(persistentDataKeyTimer)
        if (currentValue <= 0) {
            pokemon.persistentData.putInt(persistentDataKeyTimer, getDropTimer(pokemon))
            return true
        }

        pokemon.persistentData.putInt(persistentDataKeyTimer, currentValue - 1)

        return false
    }

    override fun cleanup(evt: PokemonEntityTickedEvent) {
        val context = getContext(evt)
        val droppers = getDroppers(context) ?: return
        val sounds = droppers.mapNotNull { it.sound }
        sounds.forEach { it.emit(evt.entity.level() as ServerLevel, evt.entity.position()) }
    }
}