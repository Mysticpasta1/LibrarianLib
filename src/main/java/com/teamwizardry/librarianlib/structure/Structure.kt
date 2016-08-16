package com.teamwizardry.librarianlib.structure

import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.Arrays

import net.minecraftforge.fml.relauncher.ReflectionHelper

import net.minecraft.block.BlockPane
import net.minecraft.block.BlockQuartz
import net.minecraft.block.BlockRedstoneComparator
import net.minecraft.block.BlockRedstoneRepeater
import net.minecraft.block.BlockRedstoneWire
import net.minecraft.block.BlockSlab
import net.minecraft.block.BlockStairs
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.Mirror
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Rotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.gen.structure.template.Template
import net.minecraft.world.gen.structure.template.Template.BlockInfo

import com.google.common.collect.HashMultimap
import com.google.common.collect.ImmutableList
import com.google.common.collect.Multimap

class Structure(loc: ResourceLocation) {

    var matchedRotation: Rotation
    protected var template: Template
    protected var templateBlocks: List<BlockInfo>? = null
    var blockAccess: TemplateBlockAccess
        protected set
    var origin = BlockPos.ORIGIN
    var min = BlockPos.ORIGIN
        protected set
    var max = BlockPos.ORIGIN
        protected set

    init {
        val stream = Structure::class.java.getResourceAsStream("/assets/" + loc.resourceDomain + "/schematics/" + loc.resourcePath + ".nbt")
        if (stream != null) {
            try {
                parse(stream)
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun blockInfos(): List<BlockInfo> {
        return templateBlocks ?: emptyList()
    }

    fun match(world: World, checkPos: BlockPos): StructureMatchResult? {
        val none = match(world, checkPos, Rotation.NONE)
        val reverse = match(world, checkPos, Rotation.CLOCKWISE_180)
        val cw = match(world, checkPos, Rotation.CLOCKWISE_90)
        val ccw = match(world, checkPos, Rotation.COUNTERCLOCKWISE_90)

        var finalList = none
        matchedRotation = Rotation.NONE

        if (finalList == null || reverse != null && reverse.allErrors.size < finalList.allErrors.size) {
            finalList = reverse
            matchedRotation = Rotation.CLOCKWISE_180
        }
        if (finalList == null || cw != null && cw.allErrors.size < finalList.allErrors.size) {
            finalList = cw
            matchedRotation = Rotation.CLOCKWISE_90
        }
        if (finalList == null || ccw != null && ccw.allErrors.size < finalList.allErrors.size) {
            finalList = ccw
            matchedRotation = Rotation.COUNTERCLOCKWISE_90
        }

        return finalList
    }

    fun match(world: World, checkPos: BlockPos, rot: Rotation): StructureMatchResult? {
        val result = StructureMatchResult(checkPos.subtract(origin), rot, this)

        val infos = templateBlocks ?: return null

        for (info in infos) {

            if (info.pos == origin)
                continue

            val worldPos = this.transformedBlockPos(info.pos.subtract(origin), Mirror.NONE, rot).add(checkPos)

            val worldState = world.getBlockState(worldPos)
            val templateState = info.blockState
            var match = true

            if (worldState.block !== templateState.block) {
                if (worldState.block === Blocks.AIR) {
                    result.airErrors.add(info.pos)
                } else {
                    result.nonAirErrors.add(info.pos)
                }
                match = false
            } else {
                val worldProps = worldState.propertyNames
                for (prop in templateState.propertyNames) {
                    if (IGNORE.contains(prop))
                        continue

                    if (!worldProps.contains(prop)) {
                        result.propertyErrors.add(info.pos)
                        match = false
                        break
                    }

                    var propsMatch = false
                    val worldValue = worldState.getValue<kotlin.Comparable<Any>>(prop as IProperty<Comparable<Any>>)
                    val templateValue = templateState.getValue<kotlin.Comparable<Any>>(prop)

                    propsMatch = propsMatch || worldValue === templateValue // if the properties are equal

                    if (!propsMatch) {
                        for (list in EQUIVALENTS.get(prop)) { // get equivalents for given property
                            if (list.contains(worldValue) && list.contains(templateValue)) {
                                propsMatch = true // if both are in an equivalent list
                                break
                            }
                        }
                    }

                    if (!propsMatch) {
                        result.propertyErrors.add(info.pos)
                        match = false
                        break
                    }
                }
            }

            if (match)
                result.matches.add(info.pos)
            else
                result.allErrors.add(info.pos)
        }

        return result
    }

    protected fun parse(stream: InputStream) {
        template = Template()
        try {
            templateBlocks = ReflectionHelper.findField(Template::class.java, "blocks", "field_186270_a").get(template) as List<BlockInfo>
        } catch (e1: IllegalArgumentException) {
            e1.printStackTrace()
        } catch (e1: IllegalAccessException) {
            e1.printStackTrace()
        }

        blockAccess = TemplateBlockAccess(template)
        try {
            val tag = CompressedStreamTools.readCompressed(stream)
            template.read(tag)

            var list = tag.getTagList("palette", 10)

            var paletteID = -1

            for (i in 0..list.tagCount() - 1) {
                val compound = list.getCompoundTagAt(i)

                if ("minecraft:structure_block" == compound.getString("Name")) {
                    paletteID = i
                    break
                }
            }

            var minX = Integer.MAX_VALUE
            var minY = Integer.MAX_VALUE
            var minZ = Integer.MAX_VALUE
            var maxX = Integer.MIN_VALUE
            var maxY = Integer.MIN_VALUE
            var maxZ = Integer.MIN_VALUE

            if (paletteID >= 0) {
                list = tag.getTagList("blocks", 10)
                for (i in 0..list.tagCount() - 1) {
                    val compound = list.getCompoundTagAt(i)
                    val posList = compound.getTagList("pos", 3)
                    val pos = BlockPos(posList.getIntAt(0), posList.getIntAt(1), posList.getIntAt(2))

                    if (compound.getInteger("state") == paletteID) {
                        origin = pos
                    }

                    minX = Math.min(minX, pos.x)
                    minY = Math.min(minY, pos.y)
                    minZ = Math.min(minZ, pos.z)

                    maxX = Math.max(maxX, pos.x)
                    maxY = Math.max(maxY, pos.y)
                    maxZ = Math.max(maxZ, pos.z)
                }
            }

            min = BlockPos(minX, minY, minZ)
            max = BlockPos(maxX, maxY, maxZ)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    private fun transformedBlockPos(pos: BlockPos, mirrorIn: Mirror, rotationIn: Rotation): BlockPos {
        var i = pos.x
        val j = pos.y
        var k = pos.z
        var flag = true

        when (mirrorIn) {
            Mirror.LEFT_RIGHT -> k = -k
            Mirror.FRONT_BACK -> i = -i
            else -> flag = false
        }

        when (rotationIn) {
            Rotation.COUNTERCLOCKWISE_90 -> return BlockPos(k, j, -i)
            Rotation.CLOCKWISE_90 -> return BlockPos(-k, j, i)
            Rotation.CLOCKWISE_180 -> return BlockPos(-i, j, -k)
            else -> return if (flag) BlockPos(i, j, k) else pos
        }
    }

    companion object {

        val IGNORE: List<IProperty<*>> = ArrayList(Arrays.asList(
                BlockSlab.HALF,
                BlockStairs.SHAPE,
                BlockStairs.FACING,
                BlockPane.EAST, BlockPane.WEST, BlockPane.NORTH, BlockPane.SOUTH,
                BlockRedstoneWire.EAST, BlockRedstoneWire.WEST, BlockRedstoneWire.NORTH, BlockRedstoneWire.SOUTH, BlockRedstoneWire.POWER,
                BlockRedstoneComparator.FACING, BlockRedstoneComparator.MODE, BlockRedstoneComparator.POWERED,
                BlockRedstoneRepeater.FACING, BlockRedstoneRepeater.DELAY, BlockRedstoneRepeater.LOCKED))

        /**
         * For properties that shouldn't be ignored, but have several equivalent values
         * Property -> list of interchangeable values
         */
        val EQUIVALENTS: Multimap<IProperty<*>, List<*>> = HashMultimap.create<IProperty<*>, List<*>>()

        init {
            EQUIVALENTS.put(BlockQuartz.VARIANT, Arrays.asList(BlockQuartz.EnumType.LINES_X, BlockQuartz.EnumType.LINES_Y, BlockQuartz.EnumType.LINES_Z))
        }
    }
}
