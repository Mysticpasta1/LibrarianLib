package com.teamwizardry.librarianlib.client.core

import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.util.math.Vec3d
import java.awt.Color

/**
 * Created by TheCodeWarrior
 */
fun VertexBuffer.pos(pos: Vec3d): VertexBuffer = this.pos(pos.xCoord, pos.yCoord, pos.zCoord)
fun VertexBuffer.color(color: Color): VertexBuffer = this.color(color.red/255f, color.green/255f, color.blue/255f, color.alpha/255f)

