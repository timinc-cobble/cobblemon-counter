package us.timinc.mc.cobblemon.counter.spawning

import com.cobblemon.mod.common.api.spawning.condition.AppendageCondition
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import net.minecraft.server.level.ServerPlayer
import us.timinc.mc.cobblemon.counter.extension.getCounterManager

class CountSpawningCondition : AppendageCondition {
    @Suppress("MemberVisibilityCanBePrivate")
    val counts: List<CountRequirement>? = null

    @Suppress("MemberVisibilityCanBePrivate")
    val streaks: List<CountRequirement>? = null

    override fun fits(spawnablePosition: SpawnablePosition): Boolean {
        val player = spawnablePosition.cause.entity as? ServerPlayer ?: return true
        val manager = player.getCounterManager()

        if (streaks !== null) streaks.forEach { req ->
            if (manager.getStreakScore(req.type, req.speciesRl, req.form) < req.amount) return false
        }

        if (counts !== null) counts.forEach { req ->
            if (manager.getCountScore(req.type, req.speciesRl, req.form) < req.amount) return false
        }

        return true
    }
}