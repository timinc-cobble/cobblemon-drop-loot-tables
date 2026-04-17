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

@Deprecated("Use the newly improved pokemon_matcher condition, it now accommodates this.")
class MovesCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val moves: List<String>,
    val all: Boolean = false
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<MovesCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                DropLootTables.RESOURCE_LOCATION_CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(MovesCondition::targetPokemon),
                Codec.STRING.listOf().fieldOf("moves")
                    .forGetter(MovesCondition::moves),
                Codec.BOOL.fieldOf("all").orElse(false)
                    .forGetter(MovesCondition::all),
            ).apply(instance, ::MovesCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.MOVES_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        val pokemonMoveNames = pokemon.moveSet.map { it.name }
        return if (all) moves.all(pokemonMoveNames::contains) else moves.any(pokemonMoveNames::contains)
    }
}