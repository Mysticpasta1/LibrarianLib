
// todo once mcmultipart is 1.11
//package com.teamwizardry.librarianlib.common.base.multipart
//
//import com.teamwizardry.librarianlib.common.util.saving.AbstractSaveHandler
//import mcmultipart.multipart.Multipart
//import net.minecraft.nbt.NBTTagCompound
//import net.minecraft.network.PacketBuffer
//
///**
// * Created by TheCodeWarrior
// */
//open class PartMod : Multipart() {
//
//    /**
//     * Override this function to store special data not stored in @Save fields in NBT.
//     *
//     * [sync] implies that this is being used to send to clientside.
//     */
//    open fun writeCustomNBT(cmp: NBTTagCompound, sync: Boolean) {
//        // NO-OP
//    }
//
//    /**
//     * Override this function to read special data not stored in @Save fields from NBT.
//     */
//    open fun readCustomNBT(cmp: NBTTagCompound) {
//        // NO-OP
//    }
//
//    /**
//     * Override this function to write special data not stored in @Save fields to bytes.
//     *
//     * [sync] implies that this is being used to send to clientside.
//     */
//    open fun writeCustomBytes(buf: PacketBuffer, sync: Boolean) {
//        // NO-OP
//    }
//
//    /**
//     * Override this function to read special data not stored in @Save fields from bytes.
//     */
//    open fun readCustomBytes(buf: PacketBuffer) {
//        // NO-OP
//    }
//
//    override fun readFromNBT(tag: NBTTagCompound) {
//        readCustomNBT(tag)
//        AbstractSaveHandler.readAutoNBT(javaClass, tag)
//        super.readFromNBT(tag)
//    }
//
//    override fun writeToNBT(tag: NBTTagCompound): NBTTagCompound {
//        writeCustomNBT(tag, false)
//        AbstractSaveHandler.writeAutoNBT(javaClass, tag)
//        super.writeToNBT(tag)
//        return tag
//    }
//
//    override fun readUpdatePacket(buf: PacketBuffer) {
//        readCustomBytes(buf)
//        AbstractSaveHandler.readAutoBytes(javaClass, buf)
//    }
//
//    override fun writeUpdatePacket(buf: PacketBuffer) {
//        writeCustomBytes(buf, true)
//        AbstractSaveHandler.writeAutoBytes(javaClass, buf, true)
//    }
//}
