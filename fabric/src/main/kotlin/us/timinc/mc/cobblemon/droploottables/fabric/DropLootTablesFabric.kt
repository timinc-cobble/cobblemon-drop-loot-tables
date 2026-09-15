package us.timinc.mc.cobblemon.droploottables.fabric

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.compat.counter.DropLootTablesCounter
import us.timinc.mc.cobblemon.timcore.fabric.AbstractFabricMod

object DropLootTablesFabric : AbstractFabricMod(DropLootTables) {
    override fun onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("cobbled_counter")) {
            DropLootTablesCounter
        }

        DropLootTables.LootItemConditionTypes.entries.forEach { (id, conditionType) ->
            Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, id, conditionType)
        }
    }
}
