package com.bombstrike.cc.invmanager;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.api.transport.IPipedItem;

import com.bombstrike.cc.invmanager.client.PacketHandler;
import com.bombstrike.cc.invmanager.compat.ComputerCraft;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityPlayerManager extends TileEntity implements IPeripheral, IPipeEntry, IInventory {
	// available methods
	private String[] methodList = {
			"size",
			"read",
			"equipped",
			"move"
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
		
		// actually tell everyone
		for (ForgeDirection direction: ForgeDirection.VALID_DIRECTIONS) {
			int blockId = Utils.getBlockNeighbor(worldObj, xCoord, yCoord, zCoord, direction);
			if (blockId > 0 && Block.blocksList[blockId] != null) {
				Block.blocksList[blockId].onNeighborBlockChange(worldObj, xCoord, yCoord, zCoord, blockId);
			}
		}

		return this;
	}
	
	public EntityPlayer getPlayer() {
		return this.player;
	}
	
	public IInventory getInventory(String name) throws Exception {
		// the plate is only compatible with player and down directions
		if (name.equals("player") || name.equals("down")) {
			return Utils.getInventory(this, name);
		}

		return null;
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
		case 0: // size
			return cc.size(arguments);
		case 1: // inventory <int:slot>
			return cc.read(arguments);
		case 2: // equipped
			if (!isPlayerOn()) throw new Exception("no player connected");
			return new Integer[]{player.inventory.currentItem};
		case 3: // export
			return cc.move(arguments);
		default:
			throw new Exception("unknown method");
		}
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

	@Override
	public int getSizeInventory() {
		if (isPlayerOn()) {
			return player.inventory.getSizeInventory() - 4; // don't give access to armor
		}
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		if (isPlayerOn()) {
			return player.inventory.getStackInSlot(var1);
		}
		return null;
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2) {
		if (isPlayerOn()) {
			return player.inventory.decrStackSize(var1, var2);
		}
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		if (isPlayerOn()) {
			return player.inventory.getStackInSlotOnClosing(var1);
		}
		return null;
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		if (isPlayerOn()) {
			player.inventory.setInventorySlotContents(var1, var2);
		}
	}

	@Override
	public String getInvName() {
		if (isPlayerOn()) {
			return player.inventory.getInvName();
		}
		return null;
	}

	@Override
	public int getInventoryStackLimit() {
		if (isPlayerOn()) {
			return player.inventory.getInventoryStackLimit();
		}
		return 0;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		if (isPlayerOn()) {
			return player.inventory.isUseableByPlayer(var1);
		}
		return false;
	}

	@Override
	public void openChest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeChest() {
		// TODO Auto-generated method stub
		
	}
}
