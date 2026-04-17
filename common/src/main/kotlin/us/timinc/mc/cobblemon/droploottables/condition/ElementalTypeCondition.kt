package us.timinc.mc.cobblemon.droploottables.condition

import com.cobblemon.mod.common.api.types.ElementalType
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

@Deprecated("Use the newly improved pokemon_matcher condition, it now accommodates this.")
class ElementalTypeCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val elements: List<ElementalType>,
    val all: Boolean = false,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<ElementalTypeCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                DropLootTables.RESOURCE_LOCATION_CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(ElementalTypeCondition::targetPokemon),
                ElementalType.BY_STRING_CODEC.listOf().fieldOf("elements")
                    .forGetter(ElementalTypeCondition::elements),
                Codec.BOOL.fieldOf("all").orElse(false)
                    .forGetter(ElementalTypeCondition::all)
            ).apply(instance, ::ElementalTypeCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.ELEMENTAL_TYPES_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        val pokemonTypes = pokemon.types
        return if (all) elements.all(pokemonTypes::contains) else elements.any(pokemonTypes::contains)
    }
}