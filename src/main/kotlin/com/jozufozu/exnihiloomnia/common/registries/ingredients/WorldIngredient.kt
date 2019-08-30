package com.jozufozu.exnihiloomnia.common.registries.ingredients

import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.jozufozu.exnihiloomnia.common.lib.LibRegistries
import com.jozufozu.exnihiloomnia.common.registries.RegistryLoader
import com.jozufozu.exnihiloomnia.common.registries.ingredients.ExplicitWorldIngredient.Info
import com.jozufozu.exnihiloomnia.common.util.contains
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.JsonUtils
import net.minecraft.util.NonNullList
import net.minecraftforge.oredict.OreDictionary
import java.util.function.Predicate

abstract class WorldIngredient: Predicate<IBlockState>, Comparable<WorldIngredient> {

    abstract val stacks: NonNullList<ItemStack>

    override fun compareTo(other: WorldIngredient): Int {
        return when {
            this is ExplicitWorldIngredient -> when (other) {
                is ExplicitWorldIngredient -> if (block === other.block) {
                    when (info) {
                        is Info.Data -> when (other.info) {
                            is Info.Data -> info.data - other.info.data
                            is Info.Variants -> -other.info.predicates.size
                            is Info.None -> 1
                        }
                        is Info.Variants -> when (other.info) {
                            is Info.Data -> info.predicates.size
                            is Info.Variants -> info.predicates.size - other.info.predicates.size
                            is Info.None -> 1
                        }
                        is Info.None -> -1
                    }
                } else
                    Block.getIdFromBlock(block) - Block.getIdFromBlock(other.block)
                else -> 1
            }
            other is ExplicitWorldIngredient -> -1
            else -> 0
        }
    }

    companion object {
        fun deserialize(ingredient: JsonObject): WorldIngredient {
            val isBlock = LibRegistries.BLOCK in ingredient
            val isOre = LibRegistries.OREDICT in ingredient

            if (isBlock && isOre) throw JsonSyntaxException("blockInput can have either \"${LibRegistries.BLOCK}\" or \"${LibRegistries.OREDICT}\", but not both")
            when {
                isOre -> return deserializeOre(ingredient)
                isBlock -> {
                    val blockName = JsonUtils.getString(ingredient, LibRegistries.BLOCK)
                    val block = Block.getBlockFromName(blockName) ?: throw JsonSyntaxException("$blockName is not a valid block")

                    val hasData = LibRegistries.DATA in ingredient
                    val hasVariants = LibRegistries.VARIANTS in ingredient

                    if (hasData && hasVariants) throw JsonSyntaxException("blockInput can have \"${LibRegistries.DATA}\" or \"${LibRegistries.VARIANTS}\", but not both")
                    return when {
                        hasData -> ExplicitWorldIngredient(block, Info.Data(JsonUtils.getInt(ingredient, LibRegistries.DATA)))
                        hasVariants -> deserializeWithBlockStateVariants(ingredient, block, blockName)
                        else -> ExplicitWorldIngredient(block)
                    }
                }
                else -> throw JsonSyntaxException("blockInput must have either \"${LibRegistries.BLOCK}\" or \"${LibRegistries.OREDICT}\"")
            }
        }

        private fun deserializeOre(ingredient: JsonObject): OreWorldIngredient {
            val oredict = JsonUtils.getString(ingredient, LibRegistries.OREDICT)

            if (!OreDictionary.doesOreNameExist(oredict)) throw JsonSyntaxException("Nothing called '$oredict' exists in the ore dictionary")

            return OreWorldIngredient(oredict)
        }

        private fun deserializeWithBlockStateVariants(ingredient: JsonObject, block: Block, blockName: String): ExplicitWorldIngredient {
            val variants = JsonUtils.getJsonObject(ingredient, LibRegistries.VARIANTS)

            val predicates = ArrayList<Predicate<IBlockState>>()

            val ctx = RegistryLoader.pushCtx("Blockstate Properties")
            for ((key, value) in variants.entrySet()) {
                RegistryLoader.restoreCtx(ctx)
                val property = block.blockState.getProperty(key)

                if (property == null) {
                    RegistryLoader.warn("'$key' is not a valid property of '$blockName', ignoring")
                    continue
                }

                RegistryLoader.pushCtx("Property: $key")

                if (!value.isJsonPrimitive || !value.isJsonArray) {
                    RegistryLoader.warn("Value of property must be either a single value or an array of values; ignoring")
                    continue
                }

                if (value.isJsonPrimitive) {
                    val optional = property.parseValue(value.asString)

                    if (!optional.isPresent) {
                        RegistryLoader.warn("${value.asString} is not a valid value for this property; ignoring")
                        continue
                    }

                    val comparable = optional.get()

                    predicates.add(Predicate { comparable == it.properties[property] })
                } else {
                    val possibleValues = ArrayList<Comparable<*>>()
                    for (element in value.asJsonArray) {
                        if (!value.isJsonPrimitive) {
                            RegistryLoader.warn("Value of property must be either a single value or an array of values")
                            continue
                        }

                        val optional = property.parseValue(value.asString)

                        if (!optional.isPresent) {
                            RegistryLoader.warn("${value.asString} is not a valid value for this property")
                            continue
                        }

                        possibleValues.add(optional.get())
                    }

                    predicates.add(Predicate { state -> possibleValues.any { it == state.properties[property] } })
                }
            }

            return ExplicitWorldIngredient(block, Info.Variants(predicates))
        }
    }
}