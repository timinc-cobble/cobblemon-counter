package us.timinc.mc.cobblemon.counter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.reactive.CancelableObservable
import com.cobblemon.mod.common.api.reactive.EventObservable
import com.cobblemon.mod.common.api.scheduling.ScheduledTask
import com.cobblemon.mod.common.api.scheduling.ServerTaskTracker
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreTypes
import com.cobblemon.mod.common.client.tooltips.TooltipManager
import com.cobblemon.mod.common.item.group.CobblemonItemGroups
import com.cobblemon.mod.common.platform.events.PlatformEvents
import net.minecraft.commands.synchronization.SingletonArgumentInfo
import net.minecraft.world.item.Item.Properties
import us.timinc.mc.cobblemon.counter.api.ClientCounterManager
import us.timinc.mc.cobblemon.counter.api.CounterType
import us.timinc.mc.cobblemon.counter.api.CounterTypeRegistry
import us.timinc.mc.cobblemon.counter.api.ScoreTypeRegistry
import us.timinc.mc.cobblemon.counter.command.*
import us.timinc.mc.cobblemon.counter.command.argument.CounterTypeArgument
import us.timinc.mc.cobblemon.counter.command.argument.ScoreTypeArgument
import us.timinc.mc.cobblemon.counter.data.SpeciesFormOverride
import us.timinc.mc.cobblemon.counter.event.BreakStreakEvent
import us.timinc.mc.cobblemon.counter.event.RecordEvent
import us.timinc.mc.cobblemon.counter.handler.*
import us.timinc.mc.cobblemon.counter.item.CounterItem
import us.timinc.mc.cobblemon.counter.item.CounterTooltipGenerator
import us.timinc.mc.cobblemon.counter.scoretype.CountScoreType
import us.timinc.mc.cobblemon.counter.scoretype.StreakScoreType
import us.timinc.mc.cobblemon.counter.spawning.CountSpawningCondition
import us.timinc.mc.cobblemon.timcore.*

const val MOD_ID: String = "cobbled_counter"

object CounterMod : AbstractMod<CounterMod.CounterConfig>(MOD_ID, CounterConfig::class.java) {
    class CounterConfig : AbstractConfig() {
        init {
            CounterTypes
            ScoreTypes
        }

        val ignoreFormFor: Set<String> = emptySet()
        val noBroadcastFor: Set<String> = emptySet()
        val minimumStreakForBroadcast: Int = 1
    }

    val broadcastList: Set<String>
        get() = CounterTypeRegistry.types().filterNot(config.noBroadcastFor::contains).toSet()

    fun breakStreakOnForm(counterType: CounterType): Boolean = !config.ignoreFormFor.contains(counterType.type)

    object PlayerInstancedDataStores {
        val COUNTER = PlayerInstancedDataStoreTypes.register(
            PlayerInstancedDataStoreType(
                modResource("counter"),
                ClientCounterManager::decode,
                ClientCounterManager::runAction,
                ClientCounterManager::runActionIncremental
            )
        )
    }

    object SaveTasks {
        val SAVE_COUNTER = ScheduledTask.Builder()
            .execute { Cobblemon.playerDataManager.saveAllOfOneType(PlayerInstancedDataStores.COUNTER) }.delay(30f)
            .interval(120f).infiniteIterations().tracker(ServerTaskTracker).build()
    }

    object CounterTypes {
        val CAPTURE = CounterTypeRegistry.registerCounterType(CounterType("capture"))
        val KO = CounterTypeRegistry.registerCounterType(CounterType("ko"))
        val RESURRECTION = CounterTypeRegistry.registerCounterType(CounterType("resurrection"))
        val FISH = CounterTypeRegistry.registerCounterType(CounterType("fish"))
        val HATCH = CounterTypeRegistry.registerCounterType(CounterType("hatch"))
        val SNACK = CounterTypeRegistry.registerCounterType(CounterType("snack"))
    }

    object ScoreTypes {
        val COUNT = ScoreTypeRegistry.registerScoreType(CountScoreType("count"))
        val STREAK = ScoreTypeRegistry.registerScoreType(StreakScoreType("streak"))
    }

    object Items {
        val COUNTER = registerItem(
            "counter", ItemContainer({ CounterItem(Properties().stacksTo(1)) }, CobblemonItemGroups.UTILITY_ITEMS_KEY)
        )
    }

    object Events {
        @JvmField
        val RECORD_PRE = CancelableObservable<RecordEvent.Pre>()

        @JvmField
        val RECORD_POST = EventObservable<RecordEvent.Post>()

        @JvmField
        val BREAK_STREAK_PRE = CancelableObservable<BreakStreakEvent.Pre>()

        @JvmField
        val BREAK_STREAK_POST = EventObservable<BreakStreakEvent.Post>()
    }

    object Commands {
        init {
            registerCommandArgument(
                CommandArgumentContainer(
                    modResource("counter_type"),
                    CounterTypeArgument::class.java,
                    SingletonArgumentInfo.contextFree(::CounterTypeArgument)
                )
            )
            registerCommandArgument(
                CommandArgumentContainer(
                    modResource("score_type"),
                    ScoreTypeArgument::class.java,
                    SingletonArgumentInfo.contextFree(::ScoreTypeArgument)
                )
            )
        }

        val GET_SCORE_COMMAND = registerCommand(GetScoreCommand)
        val GET_SPECIES_SCORE_COMMAND = registerCommand(GetSpeciesScoreCommand)
        val GET_FORM_SCORE_COMMAND = registerCommand(GetFormScoreCommand)
        val ADD_SCORE_COMMAND = registerCommand(AddScoreCommand)
        val REDUCE_SCORE_COMMAND = registerCommand(ReduceScoreCommand)
        val SET_SCORE_COMMAND = registerCommand(SetScoreCommand)
    }

    init {
        PlatformEvents.SERVER_STARTING.subscribe(Priority.LOWEST, ServerStartingHandler::handle)
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.LOWEST, CatchHandler::handle)
        CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.LOWEST, BattleFaintedHandler::handle)
        CobblemonEvents.FOSSIL_REVIVED.subscribe(Priority.LOWEST, FossilRevivedHandler::handle)
        TimCoreEvents.POKEMON_ENTITY_DID_SPAWN.subscribe(Priority.LOWEST, FishedUpHandler::handle)
        TimCoreEvents.POKEMON_ENTITY_DID_SPAWN.subscribe(Priority.LOWEST, SnackedHandler::handle)
        CobblemonEvents.HATCH_EGG_POST.subscribe(Priority.LOWEST, EggHatchHandler::handle)
        registerSpawningCondition(CountSpawningCondition::class.java)
        PlayerInstancedDataStores
        SaveTasks
        registerReloadListener(SpeciesFormOverride.Manager)
        Items
        TooltipManager.registerTooltipGenerator(CounterTooltipGenerator)
        Commands
    }
}