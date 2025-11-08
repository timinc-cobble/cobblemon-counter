package us.timinc.mc.cobblemon.counter.handler

import com.cobblemon.mod.common.api.spawning.context.FishingSpawningContext
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerPlayer
import us.timinc.mc.cobblemon.counter.CounterMod.CounterTypes
import us.timinc.mc.cobblemon.counter.extension.record
import us.timinc.mc.cobblemon.timcore.AbstractHandler
import us.timinc.mc.cobblemon.timcore.event.EntityDidSpawnEvent

object FishedUpHandler : AbstractHandler<EntityDidSpawnEvent<PokemonEntity>>() {
    override fun handle(evt: EntityDidSpawnEvent<PokemonEntity>) {
        val ctx = evt.ctx
        if (ctx !is FishingSpawningContext) return

        val player = ctx.cause.entity
        if (player !is ServerPlayer) return

        val pokemon = evt.entity.pokemon
        player.record(pokemon, CounterTypes.FISH)
    }
}
