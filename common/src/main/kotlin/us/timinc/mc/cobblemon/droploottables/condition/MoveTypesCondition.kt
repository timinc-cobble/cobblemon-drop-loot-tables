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
import us.timinc.mc.cobblemon.droploottables.paramextractor.PokemonParamExtractor
import us.timinc.mc.cobblemon.droploottables.DropLootTables.DataKeys.LootParamKeys.FOCUS_POKEMON

@Deprecated ("Use the newly improved pokemon_matcher condition, it now accommodates this.")
class MoveTypesCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val moveTypes: List<ElementalType>,
    val all: Boolean = false,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<MoveTypesCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceLocation.CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(MoveTypesCondition::targetPokemon),
                ElementalType.BY_STRING_CODEC.listOf().fieldOf("move_types")
                    .forGetter(MoveTypesCondition::moveTypes),
                Codec.BOOL.fieldOf("all").orElse(false)
                    .forGetter(MoveTypesCondition::all),
            ).apply(instance, ::MoveTypesCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.MOVE_TYPES_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        val pokemonMoveTypes = pokemon.moveSet.map { it.type }
        return if (all) moveTypes.all(pokemonMoveTypes::contains) else moveTypes.any(pokemonMoveTypes::contains)
    }
}