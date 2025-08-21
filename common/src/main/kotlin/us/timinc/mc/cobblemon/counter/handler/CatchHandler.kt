package us.timinc.mc.cobblemon.counter.handler

import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import us.timinc.mc.cobblemon.counter.CounterMod.CounterTypes
import us.timinc.mc.cobblemon.counter.extension.record

object CatchHandler {
    fun handle(pokemonCapturedEvent: PokemonCapturedEvent) {
        val player = pokemonCapturedEvent.player
        val pokemon = pokemonCapturedEvent.pokemon
        player.record(pokemon, CounterTypes.CAPTURE)
    }
}