package us.timinc.mc.cobblemon.droploottables.condition

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.paramextractor.PokemonParamExtractor
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType
import us.timinc.mc.cobblemon.droploottables.DropLootTables.DataKeys.LootParamKeys.FOCUS_POKEMON

@Deprecated ("Use the newly improved pokemon_matcher condition, it now accommodates this.")
class AspectsCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val aspects: List<String>,
    val all: Boolean = false,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<AspectsCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceLocation.CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(AspectsCondition::targetPokemon),
                Codec.STRING.listOf().fieldOf("aspects")
                    .forGetter(AspectsCondition::aspects),
                Codec.BOOL.fieldOf("all").orElse(false)
                    .forGetter(AspectsCondition::all),
            ).apply(instance, ::AspectsCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.ASPECTS_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        return if (all) aspects.all(pokemon.aspects::contains) else aspects.any(pokemon.aspects::contains)
    }
}