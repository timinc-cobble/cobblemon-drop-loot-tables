package us.timinc.mc.cobblemon.droploottables.neoforge

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModList
import net.neoforged.fml.common.Mod
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.registries.RegisterEvent
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.MOD_ID
import us.timinc.mc.cobblemon.droploottables.compat.counter.DropLootTablesCounter
import us.timinc.mc.cobblemon.timcore.neoforge.AbstractNeoForgeMod

@Mod(MOD_ID)
@EventBusSubscriber(modid = MOD_ID)
object DropLootTablesNeoForge : AbstractNeoForgeMod(DropLootTables) {
    @SubscribeEvent
    fun registerLootItemConditionTypes(event: RegisterEvent) {
        if (event.registry != BuiltInRegistries.LOOT_CONDITION_TYPE) return

        if (ModList.get().isLoaded("cobbled_counter")) {
            DropLootTablesCounter
        }

        DropLootTables.LootItemConditionTypes.entries.forEach { (id, conditionType) ->
            Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, id, conditionType)
        }
    }
}
