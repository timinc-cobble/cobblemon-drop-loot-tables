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
class LabelCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val labels: List<String>,
    val all: Boolean = false,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<LabelCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                DropLootTables.RESOURCE_LOCATION_CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(LabelCondition::targetPokemon),
                Codec.STRING.listOf().fieldOf("labels")
                    .forGetter(LabelCondition::labels),
                Codec.BOOL.fieldOf("all").orElse(false)
                    .forGetter(LabelCondition::all)
            ).apply(instance, ::LabelCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.LABEL_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        val pokemonLabels = pokemon.form.labels
        return if (all) labels.all(pokemonLabels::contains) else labels.any(pokemonLabels::contains)
    }
}