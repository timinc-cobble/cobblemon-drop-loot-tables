package us.timinc.mc.cobblemon.droploottables.condition

import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.math.FloatRange
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.DropLootTables.DataKeys.LootParamKeys.FOCUS_POKEMON
import us.timinc.mc.cobblemon.droploottables.MOD_ID
import us.timinc.mc.cobblemon.droploottables.toFloatRange
import us.timinc.mc.cobblemon.droploottables.paramextractor.PokemonParamExtractor

@Deprecated ("Use the newly improved pokemon_matcher condition, it now accommodates this.")
class PersistentDataRangeCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val key: String,
    val range: FloatRange,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<PersistentDataRangeCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.optionalFieldOf("target_pokemon", FOCUS_POKEMON.toString())
                    .forGetter { it.targetPokemon.toString() },
                Codec.STRING.fieldOf("key").forGetter(PersistentDataRangeCondition::key),
                Codec.STRING.fieldOf("range").forGetter { it.range.toString() }
            ).apply(instance) { pokemon, key, range ->
                PersistentDataRangeCondition(
                    pokemon.asIdentifierDefaultingNamespace(MOD_ID),
                    key,
                    toFloatRange(range)
                )
            }
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.PERSISTENT_DATA_RANGE_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        val tagStringValue = pokemon.persistentData.get(key)?.asString ?: return false
        val tagValue = tagStringValue.toFloatOrNull() ?: return false
        return range.contains(tagValue)
    }
}