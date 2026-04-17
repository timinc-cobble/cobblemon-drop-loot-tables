package us.timinc.mc.cobblemon.droploottables.handler

import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.MOD_ID
import us.timinc.mc.cobblemon.droploottables.api.DropHandler
import us.timinc.mc.cobblemon.droploottables.api.DropTarget
import us.timinc.mc.cobblemon.droploottables.dropper.CapturedDropper
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerEnderChestDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PokemonEntityDropTarget

object CapturedHandler : DropHandler<CapturedDropper.Context, CapturedDropper, PokemonCapturedEvent> {
    override val dropperTypeId: ResourceLocation = DropLootTables.DataKeys.DropperTypes.CAPTURED

    override val dropTargetTypes: MutableMap<ResourceLocation, (evt: PokemonCapturedEvent) -> DropTarget?> =
        mutableMapOf(
            DropLootTables.DataKeys.DropTargetTypes.PLAYER_ENDER_STORAGE to { evt ->
                PlayerEnderChestDropTarget(evt.player)
            },
            DropLootTables.DataKeys.DropTargetTypes.PLAYER_INVENTORY to { evt ->
                PlayerDropTarget(evt.player)
            },
            DropLootTables.DataKeys.DropTargetTypes.POKEMON_WORLD_POSITION to { evt ->
                evt.pokemon.entity?.let(::PokemonEntityDropTarget)
            },
        )

    override val selectedDropTargetTypes: List<ResourceLocation>
        get() = DropLootTables.config.capturedDropTargets.map { it.asIdentifierDefaultingNamespace(MOD_ID) }

    @Suppress("unused")
    fun registerDropTargetType(id: ResourceLocation, getter: (evt: PokemonCapturedEvent) -> DropTarget?) {
        dropTargetTypes[id] = getter
    }

    override fun getContext(evt: PokemonCapturedEvent): CapturedDropper.Context = CapturedDropper.Context(
        evt.player.level() as ServerLevel,
        evt.pokemon,
        evt.player,
        evt.pokeBallEntity.pokeBall
    )

    override fun getLevel(evt: PokemonCapturedEvent): ServerLevel = evt.player.level() as ServerLevel

    override fun processLegacyDrops(evt: PokemonCapturedEvent) =
        getLegacyDrops(evt.pokemon.form, "capture", getContext(evt).toLootParams(), getLevel(evt))
}