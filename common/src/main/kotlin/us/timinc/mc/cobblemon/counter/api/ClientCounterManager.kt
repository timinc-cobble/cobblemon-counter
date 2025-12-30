package us.timinc.mc.cobblemon.counter.api

import com.cobblemon.mod.common.api.storage.player.client.ClientInstancedPlayerData
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.client.Minecraft
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import us.timinc.mc.cobblemon.counter.CounterMod
import us.timinc.mc.cobblemon.counter.CounterMod.PlayerInstancedDataStores
import us.timinc.mc.cobblemon.counter.CounterMod.config
import kotlin.math.min

class ClientCounterManager(
    override val counters: MutableMap<CounterType, Counter>,
    val broadcasts: Set<String>,
    val minimumStreakToBroadcast: Int,
) : AbstractCounterManager(), ClientInstancedPlayerData {
    override fun encode(buf: RegistryFriendlyByteBuf) {
        buf.writeMap(
            counters,
            { _, key -> buf.writeString(key.type) },
            { _, value -> value.encode(buf) }
        )
        buf.writeCollection(
            broadcasts
        ) { _, value -> buf.writeString(value) }
        buf.writeInt(minimumStreakToBroadcast)
    }

    companion object {
        var clientCounterData = ClientCounterManager(mutableMapOf(), setOf(), 0)

        fun decode(buf: RegistryFriendlyByteBuf): SetClientPlayerDataPacket {
            val map = buf.readMap(
                { buf.readString().let(CounterTypeRegistry::findByType) },
                { Counter().also { it.decode(buf) } }
            )
            val broadcasts = buf.readList { buf.readString() }
            val minimumStreakToBroadcast = buf.readInt()
            return SetClientPlayerDataPacket(
                PlayerInstancedDataStores.COUNTER,
                ClientCounterManager(map, broadcasts.toSet(), minimumStreakToBroadcast)
            )
        }

        fun runAction(data: ClientInstancedPlayerData) {
            if (data !is ClientCounterManager) return
            clientCounterData = data
        }

        fun runActionIncremental(data: ClientInstancedPlayerData) {
            if (data !is ClientCounterManager) return

            for ((counterType, counter) in data.counters.entries) {
                val changedStreak = counter.streak.changed()
                val targetClientCounter = clientCounterData.counters[counterType]
                    ?: throw Error("Unregistered counter type sent by server ${counterType.type}")
                for ((speciesId, speciesRecord) in counter.count) {
                    val clientSpeciesRecord = targetClientCounter.count.getOrPut(speciesId, ::mutableMapOf)
                    for ((formName, count) in speciesRecord) {
                        val clientBroadcastOn = !config.noBroadcastFor.contains(counterType.type)
                        val serverBroadcastOn = data.broadcasts.contains(counterType.type)
                        if (clientBroadcastOn && serverBroadcastOn && counter.streak.count >= min(
                                config.minimumStreakForBroadcast,
                                data.minimumStreakToBroadcast
                            )
                        ) {
                            val player = Minecraft.getInstance().player ?: return
                            player.displayClientMessage(
                                Component.translatable(
                                    "cobbled_counter.broadcast.${counterType.type}",
                                    Component.translatable("cobblemon.species.${speciesId.path}.name"),
                                    if (formName == "Normal" || formName == "untracked") "" else Component.translatable(
                                        "cobbled_counter.item.counter.tooltip.form",
                                        formName
                                    ),
                                    count,
                                    if (changedStreak) {
                                        Component.translatable(
                                            "cobbled_counter.broadcast.details.streak",
                                            counter.streak.count
                                        )
                                    } else ""
                                ),
                                config.broadcastLocation == CounterMod.CounterConfig.BroadcastLocations.ACTION_BAR
                            )
                        }

                        clientSpeciesRecord[formName] = count
                    }
                }
                if (changedStreak) targetClientCounter.streak = counter.streak
            }
        }
    }
}