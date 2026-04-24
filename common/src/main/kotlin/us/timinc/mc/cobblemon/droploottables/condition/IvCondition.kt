package us.timinc.mc.cobblemon.droploottables.condition

import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.DropLootTables.DataKeys.LootParamKeys.FOCUS_POKEMON
import us.timinc.mc.cobblemon.droploottables.paramextractor.PokemonParamExtractor
import us.timinc.mc.cobblemon.timcore.codec.INT_RANGE_CODEC

@Deprecated("Use the newly improved pokemon_matcher condition, it now accommodates this.")
class IvCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val range: IntRange,
    val stat: String,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<IvCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                DropLootTables.RESOURCE_LOCATION_CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(IvCondition::targetPokemon),
                INT_RANGE_CODEC.fieldOf("range")
                    .forGetter(IvCondition::range),
                Codec.STRING.fieldOf("stat")
                    .forGetter(IvCondition::stat)
            ).apply(instance, ::IvCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.IV_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        val pokemonEv = pokemon.ivs[Stats.getStat(stat)]
        return range.contains(pokemonEv)
    }
}