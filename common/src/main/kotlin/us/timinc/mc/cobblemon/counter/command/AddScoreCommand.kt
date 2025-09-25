package us.timinc.mc.cobblemon.counter.command

import com.cobblemon.mod.common.api.permission.PermissionLevel
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import us.timinc.mc.cobblemon.counter.extension.getCounterManager

object AddScoreCommand : CounterCommand("add", PermissionLevel.ALL_COMMANDS) {
    override fun run(commandContext: Data, rawContext: CommandContext<CommandSourceStack>): Int {
        with(commandContext) {
            val manager = player.getCounterManager()
            scoreType.adjustScore(manager, counterType, species.resourceIdentifier, form.name, score)

            giveFeedback(
                Component.translatable(
                    "cobbled_counter.command.feedback.add_score",
                    player.name,
                    score,
                    Component.translatable("cobbled_counter.part.counter_type.${counterType.type}"),
                    Component.translatable("cobbled_counter.part.score_type.${scoreType.type}")
                ), rawContext
            )

            return Command.SINGLE_SUCCESS
        }
    }
}