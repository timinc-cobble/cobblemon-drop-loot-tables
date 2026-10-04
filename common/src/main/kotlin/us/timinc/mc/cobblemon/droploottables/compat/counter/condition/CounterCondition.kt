package us.timinc.mc.cobblemon.droploottables.compat.counter.condition

import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType
import us.timinc.mc.cobblemon.counter.api.CounterType
import us.timinc.mc.cobblemon.counter.api.CounterTypeRegistry
import us.timinc.mc.cobblemon.counter.extension.getCounterManager
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.compat.counter.DropLootTablesCounter

class CounterCondition(
    val targetPlayer: ResourceLocation = DropLootTables.DataKeys.LootParamKeys.FOCUS_PLAYER,
    val targetPokemon: ResourceLocation = DropLootTables.DataKeys.LootParamKeys.FOCUS_POKEMON,
    val range: IntRange,
    val counterType: CounterType,
    val streak: Boolean = false,
) : LootItemCondition {
    companion object {
        val INT_RANGE_CODEC: Codec<IntRange> = Codec.STRING.xmap(
            { str ->
                val (start, end) = str.split("..")

                try {
                    val actualStart = when (start.lowercase()) {
                        "min" -> Int.MIN_VALUE
                        else -> start.toInt()
                    }
                    val actualEnd = when (end.lowercase()) {
                        "max" -> Int.MAX_VALUE
                        else -> end.toInt()
                    }
                    actualStart..actualEnd
                } catch (e: NumberFormatException) {
                    throw IllegalArgumentException("'$start' and/or '$end' is/are not integers", e)
                }
            },
            { it.toString() }
        )

        val CODEC: MapCodec<CounterCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceLocation.CODEC.optionalFieldOf(
                    "target_player",
                    DropLootTables.DataKeys.LootParamKeys.FOCUS_PLAYER
                ).forGetter(CounterCondition::targetPlayer),
                ResourceLocation.CODEC.optionalFieldOf(
                    "target_pokemon",
                    DropLootTables.DataKeys.LootParamKeys.FOCUS_POKEMON
                ).forGetter(CounterCondition::targetPokemon),
                INT_RANGE_CODEC.fieldOf("range").forGetter(CounterCondition::range),
                Codec.STRING.fieldOf("counter_type").forGetter { it.counterType.type },
                Codec.BOOL.fieldOf("streak").orElse(false).forGetter(CounterCondition::streak)
            ).apply(instance) { targetPlayer, targetPokemon, range, counterType, streak ->
                CounterCondition(
                    targetPlayer,
                    targetPokemon,
                    range,
                    CounterTypeRegistry.findByType(counterType),
                    streak
                )
            }
        }
    }

    override fun test(context: LootContext): Boolean {
        val playerParam = DropLootTables.LootParams.params[targetPlayer] ?: return false
        val player = context.getParamOrNull(playerParam) as? ServerPlayer ?: return false
        val pokemonParam = DropLootTables.LootParams.params[targetPokemon] ?: return false
        val pokemonData = context.getParamOrNull(pokemonParam) as? Pokemon ?: return false

        val speciesId = pokemonData.species.resourceIdentifier
        val formName = pokemonData.form.name

        val manager = player.getCounterManager()
        val value =
            if (streak) {
                manager.getStreakScore(counterType, speciesId, formName)
            } else {
                manager.getCountScore(counterType, speciesId, formName)
            }

        return range.contains(value)
    }

    override fun getType(): LootItemConditionType = DropLootTablesCounter.LootItemConditionTypes.COUNTER_CONDITION
}
