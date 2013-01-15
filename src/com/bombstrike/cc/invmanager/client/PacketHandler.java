package com.bombstrike.cc.invmanager.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.bombstrike.cc.invmanager.TileEntityPlayerManager;

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
			PACKET type = PACKET.valueOf(input.readUTF());
			switch (type) {
			case TILEDESCRIPTION:
				//InventoryManager.logger.info("Received packet 250 from server containing a tile description");
				// read data
				int x = input.readInt();
				int y = input.readInt();
				int z = input.readInt();
				int connections = input.readInt();
				// set data
				World world = ((EntityPlayer)player).worldObj;
				TileEntity te = world.getBlockTileEntity(x, y, z);
				if (te instanceof TileEntityPlayerManager) {
					((TileEntityPlayerManager)te).setConnections(connections);
				}
				break;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

}
