package us.timinc.mc.cobblemon.droploottables.api.extension

import com.cobblemon.mod.common.api.drop.ItemDropEntry
import com.mojang.serialization.JsonOps
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.registries.Registries
import net.minecraft.resources.RegistryOps
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import us.timinc.mc.cobblemon.droploottables.DropLootTables

fun ItemDropEntry.buildItem(level: ServerLevel): ItemStack? {
    val item = level.registryAccess().registryOrThrow(Registries.ITEM).get(item)
        ?: run {
            DropLootTables.debugger.debug("Unable to load drop item: $item", true)
            return null
        }
    val stack = ItemStack(item, quantityRange?.random() ?: quantity)
    components?.let { components ->
        val registryOps = RegistryOps.create(JsonOps.INSTANCE, level.registryAccess())
        DataComponentPatch.CODEC.parse(registryOps, components)
            .ifSuccess { stack.applyComponentsAndValidate(it) }
            .ifError { error ->
                DropLootTables.debugger.debug("Unable to parse components for drop item $item: ${error.message()}", true)
            }
    }

    return stack
}
