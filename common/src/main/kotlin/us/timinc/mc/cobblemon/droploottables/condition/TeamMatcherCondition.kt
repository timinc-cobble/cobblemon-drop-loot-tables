package us.timinc.mc.cobblemon.droploottables.condition

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.DropLootTables.DataKeys.LootParamKeys.FOCUS_PLAYER
import us.timinc.mc.cobblemon.droploottables.paramextractor.TeamParamExtractor
import us.timinc.mc.cobblemon.timcore.LimitedList
import us.timinc.mc.cobblemon.timcore.PokemonMatcher

class TeamMatcherCondition(
    val targetTeam: ResourceLocation = FOCUS_PLAYER,
    val matcher: Set<PokemonMatcher>,
    val antiMatcher: Set<PokemonMatcher>,
) : LootItemCondition {
    constructor(
        targetTeam: ResourceLocation = FOCUS_PLAYER,
        matcher: Iterable<PokemonMatcher>,
        antiMatcher: Iterable<PokemonMatcher>,
    ) : this(targetTeam, matcher.toSet(), antiMatcher.toSet())

    companion object {
        val CODEC: MapCodec<TeamMatcherCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                DropLootTables.RESOURCE_LOCATION_CODEC.optionalFieldOf(
                    "target_team",
                    FOCUS_PLAYER
                )
                    .forGetter(TeamMatcherCondition::targetTeam),
                PokemonMatcher.STRING_CODEC.listOf().optionalFieldOf("matcher", emptyList())
                    .forGetter { it.matcher.toList() },
                PokemonMatcher.STRING_CODEC.listOf().optionalFieldOf("anti_matcher", emptyList())
                    .forGetter { it.antiMatcher.toList() },
            ).apply(instance, ::TeamMatcherCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.TEAM_MATCHER_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val team = TeamParamExtractor.getFrom(ctx, targetTeam) ?: return false
        return team.any { pokemon -> LimitedList.PokemonMatcherList.matchesList(pokemon, matcher, antiMatcher) }
    }
}