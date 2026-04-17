package us.timinc.mc.cobblemon.droploottables.condition

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
import us.timinc.mc.cobblemon.timcore.codec.FLOAT_RANGE_CODEC

@Deprecated("Use the newly improved pokemon_matcher condition, it now accommodates this.")
class PersistentDataRangeCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val key: String,
    val range: ClosedFloatingPointRange<Float>,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<PersistentDataRangeCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                DropLootTables.RESOURCE_LOCATION_CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(PersistentDataRangeCondition::targetPokemon),
                Codec.STRING.fieldOf("key").forGetter(PersistentDataRangeCondition::key),
                FLOAT_RANGE_CODEC.fieldOf("range").forGetter(PersistentDataRangeCondition::range),
            ).apply(instance, ::PersistentDataRangeCondition)
        }
    }

    override fun getType(): LootItemConditionType =
        DropLootTables.LootItemConditionTypes.PERSISTENT_DATA_RANGE_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        val tagStringValue = pokemon.persistentData.get(key)?.asString ?: return false
        val tagValue = tagStringValue.toFloatOrNull() ?: return false
        return range.contains(tagValue)
    }
}