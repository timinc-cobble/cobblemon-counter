package us.timinc.mc.cobblemon.counter.handler

import com.cobblemon.mod.common.api.events.pokemon.FossilRevivedEvent
import us.timinc.mc.cobblemon.counter.CounterMod.CounterTypes
import us.timinc.mc.cobblemon.counter.extension.record

object FossilRevivedHandler {
    fun handle(fossilRevivedEvent: FossilRevivedEvent) {
        val pokemon = fossilRevivedEvent.pokemon
        val player = fossilRevivedEvent.player ?: return
        player.record(pokemon, CounterTypes.RESURRECTION)
    }
}
