package us.timinc.mc.cobblemon.counter.command

import com.cobblemon.mod.common.api.permission.PermissionLevel
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import us.timinc.mc.cobblemon.counter.CounterMod
import us.timinc.mc.cobblemon.counter.api.CounterType
import us.timinc.mc.cobblemon.counter.api.ScoreType
import us.timinc.mc.cobblemon.counter.command.argument.CounterTypeArgument
import us.timinc.mc.cobblemon.counter.command.argument.ScoreTypeArgument
import us.timinc.mc.cobblemon.counter.extension.getCounterManager
import us.timinc.mc.cobblemon.timcore.AbstractCommand

object GetScoreCommand : AbstractCommand<GetScoreCommand.Data>(
    "get",
    PermissionLevel.NONE,
    CounterMod,
    listOf(
        Commands.argument("player", EntityArgument.player()),
        Commands.argument("counterType", CounterTypeArgument.type()),
        Commands.argument("scoreType", ScoreTypeArgument.type())
    )
) {
    data class Data(
        val player: ServerPlayer,
        val counterType: CounterType,
        val scoreType: ScoreType,
    )

    override fun parseContext(ctx: CommandContext<CommandSourceStack>): Data =
        Data(
            ctx.getArgument("player", EntitySelector::class.java).findSinglePlayer(ctx.source),
            ctx.getArgument("counterType", CounterType::class.java),
            ctx.getArgument("scoreType", ScoreType::class.java)
        )

    override fun run(commandContext: Data, rawContext: CommandContext<CommandSourceStack>): Int {
        with(commandContext) {
            val manager = player.getCounterManager()
            val score = scoreType.getScore(manager, counterType)

            giveFeedback(
                Component.translatable(
                    "cobbled_counter.command.feedback.get_score",
                    player.name,
                    score,
                    Component.translatable("cobbled_counter.part.counter_type.${counterType.type}"),
                    Component.translatable("cobbled_counter.part.score_type.${scoreType.type}")
                ), rawContext
            )

            return score
        }
    }
}