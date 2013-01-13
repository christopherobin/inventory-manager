package com.bombstrike.cc.invmanager;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.bombstrike.cc.invmanager.client.PacketHandler;
import com.bombstrike.cc.invmanager.compat.ComputerCraft;

import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.IMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityPlayerManager extends TileEntity implements IPeripheral, IPipeEntry {
	// available methods
	private String[] methodList = {
			"inventory",
			"size",
			"read",
			"equipped",
			"export",
			"import"
	};
	private EntityPlayer player = null;
	private IComputerAccess computer = null;
	private ComputerCraft cc;
	private int connections = 0;
	
	public TileEntityPlayerManager() {
		cc = new ComputerCraft(this);
	}
	
	public boolean isPlayerOn() {
		return (player != null);
	}
	
	public TileEntityPlayerManager setPlayer(EntityPlayer player) {
		this.player = player;

		// tell the computer
		if (computer != null) {
			computer.queueEvent("playerAvailable");
		}

		return this;
	}
	
	public EntityPlayer getPlayer() {
		return this.player;
	}
	
	@Override
	public String getType() {
		return "playerInvManager";
	}

	@Override
	public String[] getMethodNames() {
		return methodList;
	}
	
	@Override
	public Packet getDescriptionPacket() {
		ByteArrayOutputStream data = new ByteArrayOutputStream(Integer.SIZE * 5);
		DataOutputStream writer = new DataOutputStream(data);

		try {
			writer.writeUTF(PacketHandler.PACKET.TILEDESCRIPTION.name());
			writer.writeInt(xCoord);
			writer.writeInt(yCoord);
			writer.writeInt(zCoord);
			writer.writeInt(connections);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = InventoryManager.CHANNEL;
		packet.data = data.toByteArray();
		packet.length = data.size();
		
	    //InventoryManager.logger.info("Sending description packet");
		return packet;
	}
	
	public void setConnections(int data) {
		connections = data;
		// if on the server, update the client
		if (!worldObj.isRemote) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	public int getConnections() {
		return connections;
	}
	
	@Override
	public Object[] callMethod(IComputerAccess computer, int method,
			Object[] arguments) throws Exception {
		// resolve calls
		switch (method) {
		case 0: // inventory
			
		case 1: // size
			if (!isPlayerOn()) throw new Exception("no player connected");
			return new Integer[]{player.inventory.getSizeInventory()};
		case 2: // inventory <int:slot>
			if (!isPlayerOn()) throw new Exception("no player connected");
			return cc.readInventory(arguments, player.inventory);
		case 3: // equipped
			if (!isPlayerOn()) throw new Exception("no player connected");
			return new Integer[]{player.inventory.currentItem};
		case 4: // export
			if (!isPlayerOn()) throw new Exception("no player connected");
			return cc.exportItem(arguments, player.inventory);
		case 5: // import
			if (!isPlayerOn()) throw new Exception("no player connected");
			return cc.importItem(arguments, player.inventory);
		}
		
		return null;
	}

	@Override
	public boolean canAttachToSide(int side) {
		if (side == 1) return false; // doesn't make sense to attach when under
		return true;
	}

	@Override
	public void attach(IComputerAccess computer) {
		this.computer = computer;
	}

	@Override
	public void detach(IComputerAccess computer) {
		this.computer = null;
	}

	@Override
	public void entityEntering(ItemStack payload, ForgeDirection orientation) {
		// find some room and try to put the item, otherwise send it back
		if (!isPlayerOn()) {
			// send back everything
			InventoryManager.logger.info("Item entering (" + payload.itemID + "@" + payload.stackSize + ") from direction {" + orientation.offsetX + "," + orientation.offsetY + "," + orientation.offsetZ + "}");
			TileEntity from = Utils.getTileNeighbor(worldObj, xCoord, yCoord, zCoord, orientation.getOpposite());
			if (from != null && from instanceof IPipeEntry) {
				((IPipeEntry)from).entityEntering(payload, orientation.getOpposite());
			}
		}
	}

	@Override
	public void entityEntering(IPipedItem item, ForgeDirection orientation) {
		// find some room and try to put the item, otherwise send it back
		if (!isPlayerOn()) {
			// send back everything
			InventoryManager.logger.info("Item entering (" + item.getItemStack().itemID + "@" + item.getItemStack().stackSize + ") from direction {" + orientation.getOpposite().offsetX + "," + orientation.getOpposite().offsetY + "," + orientation.getOpposite().offsetZ + "}");
			TileEntity from = Utils.getTileNeighbor(worldObj, xCoord, yCoord, zCoord, orientation.getOpposite());
			InventoryManager.logger.info("Neighbor entity: " + from);
			if (from != null && from instanceof IPipeEntry) {
				InventoryManager.logger.info("Sending items back");
				((IPipeEntry)from).entityEntering(item, orientation.getOpposite());
			}
		}
	}

	@Override
	public boolean acceptItems() {
		return false;
	}
}
