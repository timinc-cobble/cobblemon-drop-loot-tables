package us.timinc.mc.cobblemon.droploottables

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.reactive.EventObservable
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.loot.parameters.LootContextParam
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType
import us.timinc.mc.cobblemon.droploottables.api.DropContext
import us.timinc.mc.cobblemon.droploottables.api.Dropper
import us.timinc.mc.cobblemon.droploottables.api.DropperType
import us.timinc.mc.cobblemon.droploottables.condition.AbilityCondition
import us.timinc.mc.cobblemon.droploottables.condition.AspectsCondition
import us.timinc.mc.cobblemon.droploottables.condition.CaughtBallCondition
import us.timinc.mc.cobblemon.droploottables.condition.DynamaxLevelCondition
import us.timinc.mc.cobblemon.droploottables.condition.EggGroupCondition
import us.timinc.mc.cobblemon.droploottables.condition.ElementalTypeCondition
import us.timinc.mc.cobblemon.droploottables.condition.EvCondition
import us.timinc.mc.cobblemon.droploottables.condition.FriendshipLevelCondition
import us.timinc.mc.cobblemon.droploottables.condition.GenderCondition
import us.timinc.mc.cobblemon.droploottables.condition.GmaxCondition
import us.timinc.mc.cobblemon.droploottables.condition.HeldItemCondition
import us.timinc.mc.cobblemon.droploottables.condition.HiddenAbilityCondition
import us.timinc.mc.cobblemon.droploottables.condition.IvCondition
import us.timinc.mc.cobblemon.droploottables.condition.KnowledgeLevelCondition
import us.timinc.mc.cobblemon.droploottables.condition.LabelCondition
import us.timinc.mc.cobblemon.droploottables.condition.LevelCondition
import us.timinc.mc.cobblemon.droploottables.condition.MoveTypesCondition
import us.timinc.mc.cobblemon.droploottables.condition.MovesCondition
import us.timinc.mc.cobblemon.droploottables.condition.NatureCondition
import us.timinc.mc.cobblemon.droploottables.condition.NicknameCondition
import us.timinc.mc.cobblemon.droploottables.condition.OriginalTrainerCondition
import us.timinc.mc.cobblemon.droploottables.condition.PersistentDataCondition
import us.timinc.mc.cobblemon.droploottables.condition.PersistentDataRangeCondition
import us.timinc.mc.cobblemon.droploottables.condition.PokemonMatcherCondition
import us.timinc.mc.cobblemon.droploottables.condition.PropertiesCondition
import us.timinc.mc.cobblemon.droploottables.condition.ShinyCondition
import us.timinc.mc.cobblemon.droploottables.condition.StatusCondition
import us.timinc.mc.cobblemon.droploottables.condition.TeamMatcherCondition
import us.timinc.mc.cobblemon.droploottables.condition.TeraTypeCondition
import us.timinc.mc.cobblemon.droploottables.condition.TradeableCondition
import us.timinc.mc.cobblemon.droploottables.data.DropperDataManager
import us.timinc.mc.cobblemon.droploottables.dropper.CapturedDropper
import us.timinc.mc.cobblemon.droploottables.dropper.DefeatedDropper
import us.timinc.mc.cobblemon.droploottables.dropper.EvolvedDropper
import us.timinc.mc.cobblemon.droploottables.dropper.HatchedDropper
import us.timinc.mc.cobblemon.droploottables.dropper.KilledDropper
import us.timinc.mc.cobblemon.droploottables.dropper.LevelUpDropper
import us.timinc.mc.cobblemon.droploottables.dropper.ReleasedDropper
import us.timinc.mc.cobblemon.droploottables.dropper.ResurrectedDropper
import us.timinc.mc.cobblemon.droploottables.dropper.StarterChosenDropper
import us.timinc.mc.cobblemon.droploottables.dropper.TickedDropper
import us.timinc.mc.cobblemon.droploottables.dropper.VictoryDropper
import us.timinc.mc.cobblemon.droploottables.event.SingleDefeatEvent
import us.timinc.mc.cobblemon.droploottables.event.SingleVictoryEvent
import us.timinc.mc.cobblemon.droploottables.handler.BaseDropCatcher
import us.timinc.mc.cobblemon.droploottables.handler.CapturedHandler
import us.timinc.mc.cobblemon.droploottables.handler.DefeatedHandler
import us.timinc.mc.cobblemon.droploottables.handler.EvolvedHandler
import us.timinc.mc.cobblemon.droploottables.handler.HatchedHandler
import us.timinc.mc.cobblemon.droploottables.handler.KilledHandler
import us.timinc.mc.cobblemon.droploottables.handler.LevelUpHandler
import us.timinc.mc.cobblemon.droploottables.handler.ReleasedHandler
import us.timinc.mc.cobblemon.droploottables.handler.ResurrectedHandler
import us.timinc.mc.cobblemon.droploottables.handler.StarterChosenHandler
import us.timinc.mc.cobblemon.droploottables.handler.TickedHandler
import us.timinc.mc.cobblemon.droploottables.handler.VictoryHandler
import us.timinc.mc.cobblemon.timcore.AbstractConfig
import us.timinc.mc.cobblemon.timcore.AbstractMod
import us.timinc.mc.cobblemon.timcore.TimCoreEvents

const val MOD_ID: String = "droploottables"

object DropLootTables : AbstractMod<DropLootTables.DropLootTablesConfig>(MOD_ID, DropLootTablesConfig::class.java) {
    class DropLootTablesConfig : AbstractConfig() {
        val capturedDropTargets: List<String> = listOf("player_inventory")
        val defeatedDropTargets: List<String> = listOf("pokemon_world_position")
        val evolutionDropTargets: List<String> = listOf("player_inventory")
        val hatchedDropTargets: List<String> = listOf("player_inventory")
        val killedDropTargets: List<String> = listOf("pokemon_world_position")
        val levelUpDropTargets: List<String> = listOf("player_inventory")
        val releasedDropTargets: List<String> = listOf("player_inventory")
        val resurrectedDropTargets: List<String> = listOf("player_inventory", "pokemon_world_position")
        val starterChosenDropTargets: List<String> = listOf("player_inventory")
        val tickedDropTargets: List<String> = listOf("pokemon_world_position")
        val victoryDropTargets: List<String> = listOf("pokemon_world_position")
    }

    object DataKeys {
        object RegistryKeys {
            val DROPPER_TYPES = modResource("dropper_types")
        }

        object DropperTypes {
            val CAPTURED = modResource("captured")
            val DEFEATED = modResource("defeated")
            val EVOLVED = modResource("evolved")
            val HATCHED = modResource("hatched")
            val KILLED = modResource("killed")
            val LEVELED = modResource("leveled")
            val RELEASED = modResource("released")
            val RESURRECTED = modResource("resurrected")
            val STARTER_CHOSEN = modResource("starter_chosen")
            val TICKED = modResource("ticked")
            val VICTORY = modResource("victory")
        }

        object DropTargetTypes {
            val PLAYER_ENDER_STORAGE = modResource("player_ender_storage")
            val PLAYER_INVENTORY = modResource("player_inventory")
            val POKEMON_HELD_ITEM = modResource("pokemon_held_item")
            val POKEMON_HELD_ITEM_REPLACE = modResource("pokemon_held_item_replace")
            val POKEMON_WORLD_POSITION = modResource("pokemon_world_position")
        }

        object DropConditionKeys {
            val ABILITY = modResource("ability")
            val ASPECTS = modResource("aspects")
            val CAUGHT_BALL = modResource("caught_ball")
            val DYNAMAX_LEVEL = modResource("dynamax_level")
            val EGG_GROUPS = modResource("egg_group")
            val ELEMENTAL_TYPE = modResource("type")
            val EV = modResource("ev")
            val FRIENDSHIP = modResource("friendship")
            val GENDER = modResource("gender")
            val GMAX = modResource("gmax")
            val HELD_ITEM = modResource("held_item")
            val HIDDEN_ABILITY = modResource("hidden_ability")
            val IV = modResource("iv")
            val KNOWLEDGE_LEVEL = modResource("knowledge_level")
            val LABEL = modResource("label")
            val LEVEL = modResource("level")
            val MOVES = modResource("moves")
            val MOVE_TYPES = modResource("move_types")
            val NATURE = modResource("nature")
            val NICKNAME = modResource("nickname")
            val ORIGINAL_TRAINER = modResource("original_trainer")
            val PERSISTENT_DATA = modResource("persistent_data")
            val PERSISTENT_DATA_RANGE = modResource("persistent_data_range")
            val POKEMON_MATCHER = modResource("pokemon_matcher")
            val PROPERTIES = modResource("properties")
            val SHINY = modResource("shiny")
            val STATUS = modResource("status")
            val TEAM_MATCHER = modResource("team_matcher")
            val TERA_TYPE = modResource("tera_type")
            val TRADEABLE = modResource("tradeable")
        }

        object LootParamKeys {
            val DEFEATED_POKEMON = modResource("defeated_pokemon")
            val DEFEATING_POKEMON = modResource("defeating_pokemon")
            val FOCUS_PLAYER = modResource("focus_player")
            val FOCUS_POKEBALL = modResource("focus_pokeball")
            val FOCUS_POKEMON = modResource("focus_pokemon")
            val PREVIOUS_POKEMON = modResource("previous_pokemon")
        }
    }

    object DropperTypes {
        val CAPTURED = register(DataKeys.DropperTypes.CAPTURED, CapturedDropper.DROPPER_TYPE)
        val DEFEATED = register(DataKeys.DropperTypes.DEFEATED, DefeatedDropper.DROPPER_TYPE)
        val EVOLVED = register(DataKeys.DropperTypes.EVOLVED, EvolvedDropper.DROPPER_TYPE)
        val HATCHED = register(DataKeys.DropperTypes.HATCHED, HatchedDropper.DROPPER_TYPE)
        val KILLED = register(DataKeys.DropperTypes.KILLED, KilledDropper.DROPPER_TYPE)
        val LEVELED = register(DataKeys.DropperTypes.LEVELED, LevelUpDropper.DROPPER_TYPE)
        val RELEASED = register(DataKeys.DropperTypes.RELEASED, ReleasedDropper.DROPPER_TYPE)
        val RESURRECTED = register(DataKeys.DropperTypes.RESURRECTED, ResurrectedDropper.DROPPER_TYPE)
        val STARTER_CHOSEN = register(DataKeys.DropperTypes.STARTER_CHOSEN, StarterChosenDropper.DROPPER_TYPE)
        val TICKED = register(DataKeys.DropperTypes.TICKED, TickedDropper.DROPPER_TYPE)
        val VICTORY = register(DataKeys.DropperTypes.VICTORY, VictoryDropper.DROPPER_TYPE)

        fun <C : DropContext, T : Dropper<C>> register(
            id: ResourceLocation,
            dropperType: DropperType<C, T>,
        ): DropperType<C, T> = Registry.register(DropperType.REGISTRY, id, dropperType)
    }

    object LootParams {
        val params: MutableMap<ResourceLocation, LootContextParam<*>> = mutableMapOf()

        val DEFEATED_POKEMON: LootContextParam<Pokemon> = register(DataKeys.LootParamKeys.DEFEATED_POKEMON)
        val DEFEATING_POKEMON: LootContextParam<Pokemon> = register(DataKeys.LootParamKeys.DEFEATING_POKEMON)
        val FOCUS_PLAYER: LootContextParam<ServerPlayer> = register(DataKeys.LootParamKeys.FOCUS_PLAYER)
        val FOCUS_POKEBALL: LootContextParam<PokeBall> = register(DataKeys.LootParamKeys.FOCUS_POKEBALL)
        val FOCUS_POKEMON: LootContextParam<Pokemon> = register(DataKeys.LootParamKeys.FOCUS_POKEMON)
        val PREVIOUS_POKEMON: LootContextParam<Pokemon> = register(DataKeys.LootParamKeys.PREVIOUS_POKEMON)

        fun <T> register(resourceLocation: ResourceLocation): LootContextParam<T> {
            val lcp = LootContextParam<T>(resourceLocation)
            params[resourceLocation] = lcp
            return lcp
        }
    }

    object LootItemConditionTypes {
        val ABILITY_CONDITION =
            register(DataKeys.DropConditionKeys.ABILITY, AbilityCondition.CODEC)
        val ASPECTS_CONDITION =
            register(DataKeys.DropConditionKeys.ASPECTS, AspectsCondition.CODEC)
        val CAUGHT_BALL_CONDITION =
            register(DataKeys.DropConditionKeys.CAUGHT_BALL, CaughtBallCondition.CODEC)
        val DYNAMAX_LEVEL_CONDITION =
            register(DataKeys.DropConditionKeys.DYNAMAX_LEVEL, DynamaxLevelCondition.CODEC)
        val EGG_GROUP_CONDITION =
            register(DataKeys.DropConditionKeys.EGG_GROUPS, EggGroupCondition.CODEC)
        val ELEMENTAL_TYPES_CONDITION =
            register(DataKeys.DropConditionKeys.ELEMENTAL_TYPE, ElementalTypeCondition.CODEC)
        val EV_CONDITION =
            register(DataKeys.DropConditionKeys.EV, EvCondition.CODEC)
        val FRIENDSHIP_CONDITION =
            register(DataKeys.DropConditionKeys.FRIENDSHIP, FriendshipLevelCondition.CODEC)
        val GENDER_CONDITION =
            register(DataKeys.DropConditionKeys.GENDER, GenderCondition.CODEC)
        val GMAX_CONDITION =
            register(DataKeys.DropConditionKeys.GMAX, GmaxCondition.CODEC)
        val HELD_ITEM_CONDITION =
            register(DataKeys.DropConditionKeys.HELD_ITEM, HeldItemCondition.CODEC)
        val HIDDEN_ABILITY_CONDITION =
            register(DataKeys.DropConditionKeys.HIDDEN_ABILITY, HiddenAbilityCondition.CODEC)
        val IV_CONDITION =
            register(DataKeys.DropConditionKeys.IV, IvCondition.CODEC)
        val KNOWLEDGE_LEVEL_CONDITION =
            register(DataKeys.DropConditionKeys.KNOWLEDGE_LEVEL, KnowledgeLevelCondition.CODEC)
        val LABEL_CONDITION =
            register(DataKeys.DropConditionKeys.LABEL, LabelCondition.CODEC)
        val LEVEL_CONDITION =
            register(DataKeys.DropConditionKeys.LEVEL, LevelCondition.CODEC)
        val MOVES_CONDITION =
            register(DataKeys.DropConditionKeys.MOVES, MovesCondition.CODEC)
        val MOVE_TYPES_CONDITION =
            register(DataKeys.DropConditionKeys.MOVE_TYPES, MoveTypesCondition.CODEC)
        val NATURE_CONDITION =
            register(DataKeys.DropConditionKeys.NATURE, NatureCondition.CODEC)
        val NICKNAME_CONDITION =
            register(DataKeys.DropConditionKeys.NICKNAME, NicknameCondition.CODEC)
        val ORIGINAL_TRAINER_CONDITION =
            register(DataKeys.DropConditionKeys.ORIGINAL_TRAINER, OriginalTrainerCondition.CODEC)
        val PERSISTENT_DATA_CONDITION =
            register(DataKeys.DropConditionKeys.PERSISTENT_DATA, PersistentDataCondition.CODEC)
        val PERSISTENT_DATA_RANGE_CONDITION =
            register(DataKeys.DropConditionKeys.PERSISTENT_DATA_RANGE, PersistentDataRangeCondition.CODEC)
        val POKEMON_MATCHER_CONDITION =
            register(DataKeys.DropConditionKeys.POKEMON_MATCHER, PokemonMatcherCondition.CODEC)
        val PROPERTIES_CONDITION =
            register(DataKeys.DropConditionKeys.PROPERTIES, PropertiesCondition.CODEC)
        val SHINY_CONDITION =
            register(DataKeys.DropConditionKeys.SHINY, ShinyCondition.CODEC)
        val STATUS_CONDITION =
            register(DataKeys.DropConditionKeys.STATUS, StatusCondition.CODEC)
        val TEAM_MATCHER_CONDITION =
            register(DataKeys.DropConditionKeys.TEAM_MATCHER, TeamMatcherCondition.CODEC)
        val TERA_TYPE_CONDITION =
            register(DataKeys.DropConditionKeys.TERA_TYPE, TeraTypeCondition.CODEC)
        val TRADEABLE_CONDITION =
            register(DataKeys.DropConditionKeys.TRADEABLE, TradeableCondition.CODEC)

        fun <T : LootItemCondition> register(id: ResourceLocation, codec: MapCodec<T>): LootItemConditionType {
            return Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, id, LootItemConditionType(codec))
        }
    }

    object Events {
        val SINGLE_DEFEAT = EventObservable<SingleDefeatEvent>()
        val SINGLE_VICTORY = EventObservable<SingleVictoryEvent>()
    }

    init {
        DropperTypes
        LootItemConditionTypes

        registerReloadListener(DropperDataManager)

        CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.LOWEST) { evt ->
            val loser = evt.killed
            val winners = loser.facedOpponents

            val events = winners.map { winner ->
                SingleDefeatEvent(
                    winner.effectedPokemon,
                    loser.effectedPokemon,
                    evt.battle,
                )
            }
            Events.SINGLE_DEFEAT.post(*events.toTypedArray())
        }
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.LOWEST) { evt ->
            val events = evt.winners.flatMap(BattleActor::pokemonList).flatMap { winner ->
                evt.losers.flatMap(BattleActor::pokemonList).map { loser ->
                    SingleVictoryEvent(
                        winner.effectedPokemon,
                        loser.effectedPokemon,
                        evt.battle,
                    )
                }
            }
            Events.SINGLE_VICTORY.post(*events.toTypedArray())
        }

        Events.SINGLE_DEFEAT.subscribe(Priority.LOWEST, DefeatedHandler::handle)
        Events.SINGLE_VICTORY.subscribe(Priority.LOWEST, VictoryHandler::handle)
        CobblemonEvents.EVOLUTION_ACCEPTED.subscribe {
            if (it.pokemon.entity != null) return@subscribe
            it.pokemon.getOwnerPlayer()?.let { player -> EvolvedHandler.whoEvolvingWho[player.uuid] = it.pokemon.uuid }
        }
        CobblemonEvents.EVOLUTION_COMPLETE.subscribe(Priority.LOWEST, EvolvedHandler::handle)
        CobblemonEvents.FOSSIL_REVIVED.subscribe(Priority.LOWEST, ResurrectedHandler::handle)
        CobblemonEvents.HATCH_EGG_POST.subscribe(Priority.LOWEST, HatchedHandler::handle)
        CobblemonEvents.LEVEL_UP_EVENT.subscribe(Priority.LOWEST, LevelUpHandler::handle)
        CobblemonEvents.LOOT_DROPPED.subscribe(Priority.NORMAL, BaseDropCatcher::handle)
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.LOWEST, CapturedHandler::handle)
        CobblemonEvents.POKEMON_FAINTED.subscribe(Priority.LOWEST, KilledHandler::handle)
        CobblemonEvents.POKEMON_RELEASED_EVENT_POST.subscribe(Priority.LOWEST, ReleasedHandler::handle)
        CobblemonEvents.STARTER_CHOSEN.subscribe(Priority.LOWEST, StarterChosenHandler::handle)
        TimCoreEvents.POKEMON_TICKED.subscribe(Priority.LOWEST, TickedHandler::handle)
    }
}