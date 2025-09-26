package us.timinc.mc.cobblemon.counter.api

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.storage.player.InstancedPlayerData
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.getPlayer
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import us.timinc.mc.cobblemon.counter.CounterMod
import us.timinc.mc.cobblemon.counter.CounterMod.PlayerInstancedDataStores
import us.timinc.mc.cobblemon.counter.CounterMod.breakStreakOnForm
import us.timinc.mc.cobblemon.counter.CounterMod.config
import us.timinc.mc.cobblemon.counter.api.Streak.Companion.IGNORED_SPECIES
import us.timinc.mc.cobblemon.counter.data.SpeciesFormOverride
import us.timinc.mc.cobblemon.counter.event.BreakStreakEvent
import us.timinc.mc.cobblemon.counter.event.RecordEvent
import java.util.*

class CounterManager(
    override val uuid: UUID,
    override val counters: Map<CounterType, Counter> = CounterTypeRegistry.counterTypes().associateWith { Counter() },
) : AbstractCounterManager(), InstancedPlayerData {
    companion object {
        val CODEC: Codec<CounterManager> = RecordCodecBuilder.create { instance ->
            instance.group(PrimitiveCodec.STRING.fieldOf("uuid").forGetter { it.uuid.toString() },
                Codec.unboundedMap(PrimitiveCodec.STRING, Counter.CODEC).fieldOf("counters").forGetter { manager ->
                    manager.counters.keys.map { key -> key.type }
                        .associateWith { key -> manager.counters[CounterTypeRegistry.findByType(key)]!!.clone() }
                }).apply(instance) { uuid, counters ->
                CounterManager(UUID.fromString(uuid), CounterTypeRegistry.counterTypes().associateWith { counterType ->
                    counters[counterType.type]?.clone() ?: Counter()
                })
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun breakStreak(
        counterType: CounterType,
        speciesId: ResourceLocation? = null,
        formName: String? = null,
        originatingPokemon: Pokemon? = null,
        silently: Boolean = false,
    ): Boolean {
        if (!silently) {
            var doBreak = false
            CounterMod.Events.BREAK_STREAK_PRE.postThen(BreakStreakEvent.Pre(
                this, counterType, BreakStreakEvent.Cause(
                    speciesId, formName, originatingPokemon
                )
            ), ifSucceeded = {
                doBreak = true
            })

            if (!doBreak) {
                return false
            }
        }

        val counter = getCounter(counterType)
        if (speciesId != null && formName != null) {
            counter.streak = Streak(
                speciesId, formName, 1
            )
        } else {
            counter.streak = Streak()
        }

        CounterMod.Events.BREAK_STREAK_POST.post(
            BreakStreakEvent.Post(
                this, counterType, BreakStreakEvent.Cause(
                    speciesId, formName, originatingPokemon
                )
            )
        )

        return true
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun record(
        pokemon: Pokemon,
        counterType: CounterType
    ) {
        val initialSpeciesId = pokemon.species.resourceIdentifier
        val initialFormName = pokemon.form.name

        val formOverride = SpeciesFormOverride.Manager.findMatch(pokemon)

        val speciesId = if (formOverride === null) initialSpeciesId else ResourceLocation.parse(formOverride.species)
        val formName =
            if (!breakStreakOnForm(counterType)) "untracked" else if (formOverride === null) initialFormName else formOverride.form

        var doRecord = false
        CounterMod.Events.RECORD_PRE.postThen(RecordEvent.Pre(
            this, counterType, speciesId, formName, pokemon
        ), ifSucceeded = {
            doRecord = true
        })
        if (!doRecord) return

        val counter = getCounter(counterType)
        val speciesRecord = counter.count.getOrPut(speciesId) { mutableMapOf() }
        speciesRecord[formName] = speciesRecord.getOrDefault(formName, 0) + 1

        var streakChanged = false
        if (counter.streak.wouldBreak(speciesId, formName)) {
            if (breakStreak(counterType, speciesId, formName, pokemon)) {
                streakChanged = true
            }
        } else {
            counter.streak.count++
            streakChanged = true
        }

        val player = uuid.getPlayer() ?: return

        val patchData = ClientCounterManager(
            mutableMapOf(
                counterType to Counter(
                    mutableMapOf(
                        speciesId to mutableMapOf(formName to speciesRecord[formName]!!)
                    ), if (streakChanged) counter.streak else Streak(IGNORED_SPECIES)
                )
            ),
            config.broadcast
        )

        player.sendPacket(
            SetClientPlayerDataPacket(
                type = PlayerInstancedDataStores.COUNTER, playerData = patchData, isIncremental = true
            )
        )

        CounterMod.Events.RECORD_POST.post(
            RecordEvent.Post(
                this, counterType, speciesId, formName, pokemon
            )
        )
    }

    fun setStreakScore(counterType: CounterType, speciesId: ResourceLocation, formName: String, score: Int) {
        val player = uuid.getPlayer() ?: return

        val counter = getCounter(counterType)
        counter.streak = Streak(speciesId, formName, score)

        val patchData = ClientCounterManager(
            mutableMapOf(
                counterType to Counter(
                    mutableMapOf(), counter.streak
                )
            ),
            config.broadcast
        )

        player.sendPacket(
            SetClientPlayerDataPacket(
                type = PlayerInstancedDataStores.COUNTER, playerData = patchData, isIncremental = true
            )
        )
    }

    fun setCountScore(counterType: CounterType, speciesId: ResourceLocation, formName: String, score: Int) {
        val player = uuid.getPlayer() ?: return

        val counter = getCounter(counterType)
        val speciesRecord = counter.count.getOrPut(speciesId) { mutableMapOf() }
        speciesRecord[formName] = score

        val patchData = ClientCounterManager(
            mutableMapOf(
                counterType to Counter(
                    mutableMapOf(
                        speciesId to mutableMapOf(formName to speciesRecord[formName]!!)
                    ), Streak(IGNORED_SPECIES)
                )
            ),
            config.broadcast
        )

        player.sendPacket(
            SetClientPlayerDataPacket(
                type = PlayerInstancedDataStores.COUNTER, playerData = patchData, isIncremental = true
            )
        )
    }

    override fun toClientData(): ClientCounterManager {
        val cloned: MutableMap<CounterType, Counter> = mutableMapOf()
        counters.forEach { (type, counter) -> cloned[type] = counter.clone() }
        return ClientCounterManager(cloned, emptySet())
    }
}