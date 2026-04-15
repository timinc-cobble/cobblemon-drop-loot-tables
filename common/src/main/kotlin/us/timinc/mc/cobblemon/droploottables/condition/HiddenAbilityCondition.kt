package us.timinc.mc.cobblemon.droploottables.condition

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType
import us.timinc.mc.cobblemon.droploottables.DropLootTables.DataKeys.LootParamKeys.FOCUS_POKEMON
import us.timinc.mc.cobblemon.droploottables.paramextractor.PokemonParamExtractor
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.pokemon.Pokemon
import us.timinc.mc.cobblemon.droploottables.DropLootTables

@Deprecated ("Use the newly improved pokemon_matcher condition, it now accommodates this.")
class HiddenAbilityCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val value: Boolean = true,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<HiddenAbilityCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceLocation.CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(HiddenAbilityCondition::targetPokemon),
                Codec.BOOL.fieldOf("value").orElse(true)
                    .forGetter(HiddenAbilityCondition::value)
            ).apply(instance, ::HiddenAbilityCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.HIDDEN_ABILITY_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        return pokemon.hasHiddenAbility() == value
    }

    private fun Pokemon.hasHiddenAbility(): Boolean =
        form.abilities.filter { it.priority == Priority.LOW }.map { it.template.name }.contains(ability.name)
}