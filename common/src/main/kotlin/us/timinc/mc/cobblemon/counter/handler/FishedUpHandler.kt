package us.timinc.mc.cobblemon.counter.handler

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import us.timinc.mc.cobblemon.counter.CounterMod.CounterTypes
import us.timinc.mc.cobblemon.counter.extension.record
import us.timinc.mc.cobblemon.timcore.AbstractHandler
import us.timinc.mc.cobblemon.timcore.TimCore
import us.timinc.mc.cobblemon.timcore.event.EntityDidSpawnEvent
import us.timinc.mc.cobblemon.timcore.getType

object FishedUpHandler : AbstractHandler<EntityDidSpawnEvent<PokemonEntity>>() {
    override fun handle(evt: EntityDidSpawnEvent<PokemonEntity>) {
        if (evt.spawner.getType() != TimCore.DataKeys.SpawnerTypes.FISHING) return
        val player = evt.playerCause ?: return
        val pokemon = evt.entity.pokemon
        player.record(pokemon, CounterTypes.FISH)
    }
}
