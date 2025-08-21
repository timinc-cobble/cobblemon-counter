package us.timinc.mc.cobblemon.counter.extension

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer
import us.timinc.mc.cobblemon.counter.CounterMod.PlayerInstancedDataStores
import us.timinc.mc.cobblemon.counter.CounterMod.debugger
import us.timinc.mc.cobblemon.counter.api.CounterManager
import us.timinc.mc.cobblemon.counter.api.CounterType

fun ServerPlayer.getCounterManager(): CounterManager {
    return Cobblemon.playerDataManager.get(this, PlayerInstancedDataStores.COUNTER) as CounterManager
}

fun ServerPlayer.record(pokemon: Pokemon, counterType: CounterType) {
    debugger.debug("Player ${name.string}|$uuid ${counterType.type}'d a ${pokemon.species.resourceIdentifier}|${pokemon.form.name}")
    val counterManager = getCounterManager()
    counterManager.record(pokemon, counterType)
}