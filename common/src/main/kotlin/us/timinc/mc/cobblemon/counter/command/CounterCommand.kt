package us.timinc.mc.cobblemon.counter.command

import com.cobblemon.mod.common.api.permission.PermissionLevel
import com.cobblemon.mod.common.command.argument.FormArgumentType
import com.cobblemon.mod.common.command.argument.SpeciesArgumentType
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import net.minecraft.server.level.ServerPlayer
import us.timinc.mc.cobblemon.counter.CounterMod
import us.timinc.mc.cobblemon.counter.api.CounterType
import us.timinc.mc.cobblemon.counter.api.ScoreType
import us.timinc.mc.cobblemon.counter.command.argument.CounterTypeArgument
import us.timinc.mc.cobblemon.counter.command.argument.ScoreTypeArgument
import us.timinc.mc.cobblemon.timcore.AbstractCommand

abstract class CounterCommand(name: String, permissionLevel: PermissionLevel = PermissionLevel.ALL_COMMANDS) :
    AbstractCommand<CounterCommand.Data>(
        name,
        permissionLevel,
        CounterMod,
        listOf(
            Commands.argument("player", EntityArgument.player()),
            Commands.argument("counterType", CounterTypeArgument.type()),
            Commands.argument("scoreType", ScoreTypeArgument.type()),
            Commands.argument("species", SpeciesArgumentType.species()),
            Commands.argument("form", FormArgumentType.form()),
            Commands.argument("score", IntegerArgumentType.integer(0))
        )
    ) {
    class Data(
        val player: ServerPlayer,
        val counterType: CounterType,
        val scoreType: ScoreType,
        val species: Species,
        val form: FormData,
        val score: Int,
    )

    override fun parseContext(ctx: CommandContext<CommandSourceStack>): Data =
        Data(
            ctx.getArgument("player", EntitySelector::class.java).findSinglePlayer(ctx.source),
            ctx.getArgument("counterType", CounterType::class.java),
            ctx.getArgument("scoreType", ScoreType::class.java),
            ctx.getArgument("species", Species::class.java),
            ctx.getArgument("form", FormData::class.java),
            IntegerArgumentType.getInteger(ctx, "score")
        )
}