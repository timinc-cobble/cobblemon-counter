package us.timinc.mc.cobblemon.counter.data

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.ProfilerFiller
import us.timinc.mc.cobblemon.timcore.AbstractReloadListener
import us.timinc.mc.cobblemon.timcore.PokemonMatcher

data class SpeciesFormOverride(
    val matcher: PokemonMatcher,
    val species: String,
    val form: String,
) {
    var id: ResourceLocation? = null

    companion object {
        val CODEC: Codec<SpeciesFormOverride> = RecordCodecBuilder.create { instance ->
            instance.group(
                PokemonMatcher.CODEC.fieldOf("matcher").forGetter(SpeciesFormOverride::matcher),
                Codec.STRING.fieldOf("species").forGetter(SpeciesFormOverride::species),
                Codec.STRING.fieldOf("form").forGetter(SpeciesFormOverride::form)
            ).apply(instance, ::SpeciesFormOverride)
        }
    }

    fun matches(pokemon: Pokemon): Boolean = matcher.matches(pokemon)

    object Manager : AbstractReloadListener(Gson(), "species_form_override") {
        private val overrides: MutableList<SpeciesFormOverride> = mutableListOf()

        override fun apply(
            objectMap: MutableMap<ResourceLocation, JsonElement>,
            resourceManager: ResourceManager,
            profilerFiller: ProfilerFiller,
        ) {
            overrides.clear()
            objectMap.entries.forEach { (id, json) ->
                val override = CODEC.parse(JsonOps.INSTANCE, json).orThrow
                override.id = id
                overrides.add(override)
            }
        }

        private fun parseOverride(json: JsonObject): SpeciesFormOverride = CODEC.parse(JsonOps.INSTANCE, json).orThrow

        fun findMatch(pokemon: Pokemon): SpeciesFormOverride? = overrides.find { it.matches(pokemon) }
        fun findMatch(speciesId: ResourceLocation, formName: String): SpeciesFormOverride? {
            val pokemon = Pokemon()
            pokemon.species = PokemonSpecies.getByIdentifier(speciesId) ?: return null
            pokemon.form = pokemon.species.forms.find { it.name == formName } ?: return null

            return findMatch(pokemon)
        }
    }
}