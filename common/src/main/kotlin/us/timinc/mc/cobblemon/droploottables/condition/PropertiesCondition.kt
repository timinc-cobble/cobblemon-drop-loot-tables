package us.timinc.mc.cobblemon.droploottables.condition

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
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
class PropertiesCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val properties: String,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<PropertiesCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceLocation.CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(PropertiesCondition::targetPokemon),
                Codec.STRING.fieldOf("properties")
                    .forGetter { it.properties }
            ).apply(instance, ::PropertiesCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.PROPERTIES_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        val properties: PokemonProperties = PokemonProperties.parse(properties)
        return properties.matches(pokemon)
    }
}