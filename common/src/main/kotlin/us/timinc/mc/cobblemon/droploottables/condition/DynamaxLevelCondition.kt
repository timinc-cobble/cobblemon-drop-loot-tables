package us.timinc.mc.cobblemon.droploottables.condition

import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.DropLootTables.DataKeys.LootParamKeys.FOCUS_POKEMON
import us.timinc.mc.cobblemon.timcore.codec.INT_RANGE_CODEC

@Deprecated("Use the newly improved pokemon_matcher condition, it now accommodates this.")
class DynamaxLevelCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val range: IntRange,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<DynamaxLevelCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                DropLootTables.RESOURCE_LOCATION_CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(DynamaxLevelCondition::targetPokemon),
                INT_RANGE_CODEC.fieldOf("range")
                    .forGetter(DynamaxLevelCondition::range),
            ).apply(instance, ::DynamaxLevelCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.DYNAMAX_LEVEL_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemonParam = DropLootTables.LootParams.params[targetPokemon] ?: return false
        val pokemon = ctx.getParamOrNull(pokemonParam) as? Pokemon ?: return false
        val pokemonLevel = pokemon.dmaxLevel
        return range.contains(pokemonLevel)
    }
}