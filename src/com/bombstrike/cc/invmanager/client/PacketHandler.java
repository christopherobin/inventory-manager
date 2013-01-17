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
		DataInputStream input = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
			NBTTagCompound nbtData = (NBTTagCompound)NBTTagCompound.readNamedTag(input);
			if (nbtData.getName().equals(PACKET.TILEDESCRIPTION.name())) {
				String name = nbtData.getString("id");
				if (name.equals(InventoryManager.blockPlayerManager.getBlockName())) {
					int x = nbtData.getInteger("x");
					int y = nbtData.getInteger("y");
					int z = nbtData.getInteger("z");
					// retrieve tile entity
					World world = ((EntityPlayer)player).worldObj;
					TileEntity te = world.getBlockTileEntity(x, y, z);
					if (te instanceof TileEntityPlayerManager) {
						// feed the entity whatever data is there
						((TileEntityPlayerManager)te).readFromNBT(nbtData);
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

}
