package us.timinc.mc.cobblemon.droploottables.api

import com.cobblemon.mod.common.pokemon.FormData
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.MOD_ID
import us.timinc.mc.cobblemon.droploottables.data.DropperDataManager

interface DropHandler<C : DropContext, D : Dropper<C>, E> {
    val dropperTypeId: ResourceLocation
    fun getContext(evt: E): C

    fun getDroppers(ctx: C) =
        DropperDataManager.getValidDroppers<C, D>(dropperTypeId, ctx)

    fun getLevel(evt: E): ServerLevel?

    fun handle(evt: E) {
        if (!isRelevantEvent(evt)) return
        val dropTargets = getDropTarget(evt) ?: return
        val ctx = getContext(evt)
        val droppers = getDroppers(ctx)
        val drops: MutableList<ItemStack> = droppers?.flatMap { dropper ->
            val toDrop = dropper.lootTables.flatMap { tableId ->
                dropFromTable(
                    tableId,
                    ctx.toLootParams(),
                    getLevel(evt) as ServerLevel
                )
            }

            dropper.dropTarget?.let { overridingDropTargetId ->
                val dropTargetFunc = dropTargetTypes[overridingDropTargetId] ?: return@flatMap emptyList()
                val dropTarget = dropTargetFunc(evt) ?: return@flatMap emptyList()
                toDrop.forEach(dropTarget::dropTo)

                return@flatMap emptyList()
            }

            toDrop
        }?.toMutableList() ?: mutableListOf()
        drops.addAll(processOtherDrops(evt))
        if (DropLootTables.config.legacyMode)
            drops.addAll(processLegacyDrops(evt))
        drops.shuffle()

        for (drop in drops) {
            var toDrop = drop.copy()
            for (dropTarget in dropTargets) {
                toDrop = dropTarget.dropTo(toDrop)
                if (toDrop.isEmpty) continue
            }
        }

        cleanup(evt, drops)
    }

    val dropTargetTypes: MutableMap<ResourceLocation, (evt: E) -> DropTarget?>
    val selectedDropTargetTypes: List<ResourceLocation>

    fun getDropTarget(evt: E): List<DropTarget>? =
        selectedDropTargetTypes.mapNotNull { id -> dropTargetTypes[id]?.invoke(evt) }

    fun lootTableExists(level: ServerLevel, tableId: ResourceLocation) =
        level.server.reloadableRegistries().getKeys(Registries.LOOT_TABLE).contains(tableId)

    fun dropFromTable(
        id: ResourceLocation,
        params: net.minecraft.world.level.storage.loot.LootParams,
        level: ServerLevel,
    ): List<ItemStack> {
        if (!lootTableExists(level, id)) {
            return emptyList()
        }

        val lootTable = level.server.reloadableRegistries().getLootTable(
            ResourceKey.create(Registries.LOOT_TABLE, id)
        )

        val results = lootTable.getRandomItems(
            params
        )
        return results
    }

    fun isRelevantEvent(evt: E): Boolean = getLevel(evt) != null

    fun processOtherDrops(evt: E): List<ItemStack> = emptyList()

    fun processLegacyDrops(evt: E): List<ItemStack> = emptyList()

    @Deprecated("Old pre-determined paths for loot tables, please use dopper data layer")
    fun getLegacyDrops(form: FormData,
                       dropType: String,
                       params: net.minecraft.world.level.storage.loot.LootParams,
                       level: ServerLevel,
    ) = dropFromTable(getAllDropId(dropType), params, level) +
        dropFromTable(getFormDropId(form, dropType), params, level)

    @Deprecated("Old pre-determined paths for loot tables, please use dopper data layer")
    private fun getAllDropId(dropType: String): ResourceLocation =
        ResourceLocation.fromNamespaceAndPath(MOD_ID, "$dropType/all")

    @Deprecated("Old pre-determined paths for loot tables, please use dopper data layer")
    private fun getFormDropId(form: FormData, dropType: String): ResourceLocation =
        ResourceLocation.fromNamespaceAndPath(
            MOD_ID,
            "$dropType/${form.species.resourceIdentifier.path}${
                if (form.name != "Normal") "/${
                    form.name.lowercase().replace(Regex("[^a-z0-9/._-]"), "")
                }" else ""
            }"
        )

    fun cleanup(evt: E, drops: MutableList<ItemStack>) {}
}