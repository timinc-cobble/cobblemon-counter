package us.timinc.mc.cobblemon.counter.handler

import com.cobblemon.mod.common.api.events.pokemon.HatchEggEvent
import us.timinc.mc.cobblemon.counter.CounterMod.CounterTypes
import us.timinc.mc.cobblemon.counter.extension.record

object EggHatchHandler {
    fun handle(evt: HatchEggEvent.Post) {
        val player = evt.player
        val pokemon = evt.pokemon
        player.record(pokemon, CounterTypes.HATCH)
    }
}