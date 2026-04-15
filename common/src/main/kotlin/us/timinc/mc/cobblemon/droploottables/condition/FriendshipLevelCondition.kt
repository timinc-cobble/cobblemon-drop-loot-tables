package us.timinc.mc.cobblemon.droploottables.condition

import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.paramextractor.PokemonParamExtractor
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType
import us.timinc.mc.cobblemon.droploottables.MOD_ID
import us.timinc.mc.cobblemon.droploottables.DropLootTables.DataKeys.LootParamKeys.FOCUS_POKEMON
import us.timinc.mc.cobblemon.timcore.codec.INT_RANGE_CODEC

@Deprecated ("Use the newly improved pokemon_matcher condition, it now accommodates this.")
class FriendshipLevelCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val range: IntRange,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<FriendshipLevelCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.optionalFieldOf("target_pokemon", FOCUS_POKEMON.toString())
                    .forGetter { it.targetPokemon.toString() },
                Codec.STRING.fieldOf("range")
                    .forGetter { it.range.toString() }
            ).apply(instance) { pokemon, range ->
                FriendshipLevelCondition(
                    pokemon.asIdentifierDefaultingNamespace(MOD_ID),
                    INT_RANGE_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(range)).getOrThrow()
                )
            }
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.FRIENDSHIP_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        val pokemonFriendship = pokemon.friendship
        return range.contains(pokemonFriendship)
    }
}