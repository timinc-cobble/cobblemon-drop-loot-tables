package us.timinc.mc.cobblemon.droploottables.condition

import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.DropLootTables.DataKeys.LootParamKeys.FOCUS_POKEMON
import us.timinc.mc.cobblemon.droploottables.MOD_ID
import us.timinc.mc.cobblemon.droploottables.paramextractor.PokemonParamExtractor
import us.timinc.mc.cobblemon.timcore.codec.INT_RANGE_CODEC

@Deprecated ("Use the newly improved pokemon_matcher condition, it now accommodates this.")
class IvCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val range: IntRange,
    val stat: String,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<IvCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.optionalFieldOf("target_pokemon", FOCUS_POKEMON.toString())
                    .forGetter { it.targetPokemon.toString() },
                Codec.STRING.fieldOf("range")
                    .forGetter { it.range.toString() },
                Codec.STRING.fieldOf("stat")
                    .forGetter(IvCondition::stat)
            ).apply(instance) { pokemon, range, stat ->
                IvCondition(
                    pokemon.asIdentifierDefaultingNamespace(MOD_ID),
                    INT_RANGE_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(range)).getOrThrow(),
                    stat
                )
            }
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.IV_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        val pokemonEv = pokemon.ivs[Stats.getStat(stat)]
        return range.contains(pokemonEv)
    }
}