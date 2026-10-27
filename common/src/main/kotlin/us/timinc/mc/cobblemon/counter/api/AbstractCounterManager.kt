package us.timinc.mc.cobblemon.counter.api

import net.minecraft.resources.ResourceLocation
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import us.timinc.mc.cobblemon.counter.CounterMod.breakStreakOnForm
import us.timinc.mc.cobblemon.counter.data.SpeciesFormOverride

abstract class AbstractCounterManager {
    abstract val counters: Map<CounterType, Counter>
    fun getCounter(counterType: CounterType): Counter {
        return counters[counterType]
            ?: throw Error("${counterType.type} was not registered with the CounterRegistry before")
    }

    fun getStreak(counterType: CounterType): Streak {
        return getCounter(counterType).streak
    }

    fun getStreakScore(counterType: CounterType, species: ResourceLocation? = null, form: String? = null): Int {
        val streak = getStreak(counterType)
        if (species === null) return streak.count
        val formOverride = form?.let { SpeciesFormOverride.Manager.findMatch(species, it) }
        val actualSpecies = formOverride?.species?.asIdentifierDefaultingNamespace() ?: species
        if (form === null || !breakStreakOnForm(counterType)) return if (actualSpecies == streak.species) streak.count else 0

        val actualForm = formOverride?.form ?: form
        return if (actualSpecies == streak.species && actualForm == streak.form) streak.count else 0
    }

    fun getCountScore(counterType: CounterType, species: ResourceLocation? = null, form: String? = null): Int {
        if (species == null) return getCounter(counterType).count.values.fold(0) { total, speciesEntry ->
            total + speciesEntry.values.fold(0) { speciesTotal, speciesCount -> speciesTotal + speciesCount }
        }
        val formOverride = form?.let { SpeciesFormOverride.Manager.findMatch(species, it) }
        val actualSpecies = formOverride?.species?.asIdentifierDefaultingNamespace() ?: species
        val speciesRecord = getCounter(counterType).count[actualSpecies] ?: return 0
        if (form === null || !breakStreakOnForm(counterType)) return speciesRecord.values.fold(0) { speciesTotal, speciesCount -> speciesTotal + speciesCount }

        val actualForm = formOverride?.form ?: form
        return speciesRecord.getOrDefault(actualForm, 0)
    }
}
