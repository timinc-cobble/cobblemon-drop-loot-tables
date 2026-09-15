package us.timinc.mc.cobblemon.droploottables.condition

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.DropLootTables.DataKeys.LootParamKeys.FOCUS_POKEMON
import us.timinc.mc.cobblemon.droploottables.paramextractor.PokemonParamExtractor
import us.timinc.mc.cobblemon.timcore.LimitedList
import us.timinc.mc.cobblemon.timcore.PokemonMatcher

class PokemonMatcherCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val matcher: Set<PokemonMatcher>,
    val antiMatcher: Set<PokemonMatcher>,
) : LootItemCondition {
    constructor(
        targetPokemon: ResourceLocation = FOCUS_POKEMON,
        matcher: Iterable<PokemonMatcher>,
        antiMatcher: Iterable<PokemonMatcher>
    ) : this(targetPokemon, matcher.toSet(), antiMatcher.toSet())

    companion object {
        val CODEC: MapCodec<PokemonMatcherCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                DropLootTables.RESOURCE_LOCATION_CODEC.optionalFieldOf(
                    "target_pokemon",
                    FOCUS_POKEMON
                )
                    .forGetter(PokemonMatcherCondition::targetPokemon),
                PokemonMatcher.STRING_CODEC.listOf().optionalFieldOf("matcher", emptyList())
                    .forGetter { it.matcher.toList() },
                PokemonMatcher.STRING_CODEC.listOf().optionalFieldOf("anti_matcher", emptyList())
                    .forGetter { it.antiMatcher.toList() },
            ).apply(instance, ::PokemonMatcherCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.POKEMON_MATCHER_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        return LimitedList.PokemonMatcherList.matchesList(pokemon, matcher, antiMatcher)
    }
}