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

@Deprecated ("Use the newly improved pokemon_matcher condition, it now accommodates this.")
class PersistentDataCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val key: String,
    val value: String,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<PersistentDataCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceLocation.CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(PersistentDataCondition::targetPokemon),
                Codec.STRING.fieldOf("key")
                    .forGetter(PersistentDataCondition::key),
                Codec.STRING.fieldOf("value")
                    .forGetter(PersistentDataCondition::value)
            ).apply(instance, ::PersistentDataCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.PERSISTENT_DATA_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        val tagValue = pokemon.persistentData.get(key)?.asString ?: return false
        return tagValue == value
    }
}