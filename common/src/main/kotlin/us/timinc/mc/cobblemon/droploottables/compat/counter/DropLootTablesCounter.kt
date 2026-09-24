package us.timinc.mc.cobblemon.droploottables.compat.counter

import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.DropLootTables.modResource
import us.timinc.mc.cobblemon.droploottables.compat.counter.condition.CounterCondition

object DropLootTablesCounter {
    object DataKeys {
        object DropConditionKeys {
            // Can't lose the loser lose reference.
            val COUNTER = modResource("counter_count")
        }
    }

    object LootItemConditionTypes {
        val COUNTER_CONDITION = DropLootTables.LootItemConditionTypes.register(
            DataKeys.DropConditionKeys.COUNTER,
            CounterCondition.CODEC
        )
    }

    init {
        DropLootTables.debugger.debug("Loading Counter compat", true)
        LootItemConditionTypes
    }
}