package us.timinc.mc.cobblemon.droploottables.condition

import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.util.pokedex
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.DropLootTables.DataKeys.LootParamKeys.FOCUS_PLAYER
import us.timinc.mc.cobblemon.droploottables.DropLootTables.DataKeys.LootParamKeys.FOCUS_POKEMON
import us.timinc.mc.cobblemon.droploottables.paramextractor.PlayerParamExtractor
import us.timinc.mc.cobblemon.droploottables.paramextractor.PokemonParamExtractor

class KnowledgeLevelCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val targetPlayer: ResourceLocation = FOCUS_PLAYER,
    val knowledge: PokedexEntryProgress,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<KnowledgeLevelCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                DropLootTables.RESOURCE_LOCATION_CODEC.optionalFieldOf(
                    "target_pokemon",
                    FOCUS_POKEMON
                )
                    .forGetter(KnowledgeLevelCondition::targetPokemon),
                DropLootTables.RESOURCE_LOCATION_CODEC.optionalFieldOf(
                    "target_player",
                    FOCUS_PLAYER
                )
                    .forGetter(KnowledgeLevelCondition::targetPlayer),
                PokedexEntryProgress.CODEC.fieldOf("knowledge").forGetter(KnowledgeLevelCondition::knowledge),
            ).apply(instance, ::KnowledgeLevelCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.KNOWLEDGE_LEVEL_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        val player = PlayerParamExtractor.getFrom(ctx, targetPlayer) ?: return false
        val playerKnowledge =
            player.pokedex().getSpeciesRecord(pokemon.species.resourceIdentifier)
                ?.getFormRecord(pokemon.form.name)?.knowledge
                ?: PokedexEntryProgress.NONE
        return playerKnowledge.ordinal >= knowledge.ordinal
    }
}