package com.bombstrike.cc.invmanager.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.bombstrike.cc.invmanager.InventoryManager;
import com.bombstrike.cc.invmanager.tileentity.TileEntityPlayerManager;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {
	public enum PACKET {
		TILEDESCRIPTION
	};

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		// nothing
	}

}
