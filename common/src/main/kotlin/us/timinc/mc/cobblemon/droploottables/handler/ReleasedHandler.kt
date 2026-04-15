package us.timinc.mc.cobblemon.droploottables.handler

import com.cobblemon.mod.common.api.events.storage.ReleasePokemonEvent
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.MOD_ID
import us.timinc.mc.cobblemon.droploottables.api.DropHandler
import us.timinc.mc.cobblemon.droploottables.api.DropTarget
import us.timinc.mc.cobblemon.droploottables.dropper.ReleasedDropper
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerDropTarget
import us.timinc.mc.cobblemon.droploottables.droptarget.PlayerEnderChestDropTarget

object ReleasedHandler : DropHandler<ReleasedDropper.Context, ReleasedDropper, ReleasePokemonEvent.Post> {
    override val dropperTypeId: ResourceLocation = DropLootTables.DataKeys.DropperTypes.RELEASED

    override fun getContext(evt: ReleasePokemonEvent.Post): ReleasedDropper.Context = ReleasedDropper.Context(
        getLevel(evt)!!,
        evt.pokemon,
        evt.player
    )

    override fun getLevel(evt: ReleasePokemonEvent.Post): ServerLevel? = evt.player.level() as? ServerLevel

    override val dropTargetTypes: MutableMap<ResourceLocation, (evt: ReleasePokemonEvent.Post) -> DropTarget?> =
        mutableMapOf(
            DropLootTables.DataKeys.DropTargetTypes.PLAYER_ENDER_STORAGE to { evt ->
                PlayerEnderChestDropTarget(evt.player)
            },
            DropLootTables.DataKeys.DropTargetTypes.PLAYER_INVENTORY to { evt ->
                PlayerDropTarget(evt.player)
            }
        )

    fun registerDropTargetType(id: ResourceLocation, getter: (evt: ReleasePokemonEvent.Post) -> DropTarget?) {
        dropTargetTypes[id] = getter
    }

    override val selectedDropTargetTypes: List<ResourceLocation>
        get() = DropLootTables.config.releasedDropTargets.map { it.asIdentifierDefaultingNamespace(MOD_ID) }

    override fun processLegacyDrops(evt: ReleasePokemonEvent.Post) =
        getLegacyDrops(evt.pokemon.form, "release", getContext(evt).toLootParams(), getLevel(evt)!!)
}