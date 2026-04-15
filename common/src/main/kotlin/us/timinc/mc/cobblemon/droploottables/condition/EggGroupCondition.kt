package us.timinc.mc.cobblemon.droploottables.condition

import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.util.codec.CodecUtils
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
class EggGroupCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val eggGroups: List<EggGroup>,
    val all: Boolean = false,
) : LootItemCondition {
    companion object {
        // TODO: Remove if/when Cobblemon adds one.
        private val EGG_GROUP_BY_STRING_CODEC: Codec<EggGroup> = CodecUtils.createByStringCodec(
            EggGroup::fromIdentifier,
            EggGroup::name
        ) { id -> "No EggGroup for ID $id" }

        val CODEC: MapCodec<EggGroupCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceLocation.CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(EggGroupCondition::targetPokemon),
                EGG_GROUP_BY_STRING_CODEC.listOf().fieldOf("egg_groups")
                    .forGetter(EggGroupCondition::eggGroups),
                Codec.BOOL.fieldOf("all").orElse(false)
                    .forGetter(EggGroupCondition::all)
            ).apply(instance, ::EggGroupCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.EGG_GROUP_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        val pokemonEggGroups = pokemon.form.eggGroups
        return if (all) eggGroups.all(pokemonEggGroups::contains) else eggGroups.any(pokemonEggGroups::contains)
    }
}