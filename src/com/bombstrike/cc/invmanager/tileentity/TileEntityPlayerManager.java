package com.bombstrike.cc.invmanager.tileentity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.api.transport.IPipedItem;

import com.bombstrike.cc.invmanager.InventoryManager;
import com.bombstrike.cc.invmanager.Utils;
import com.bombstrike.cc.invmanager.client.PacketHandler;
import com.bombstrike.cc.invmanager.compat.ComputerCraft;
import com.bombstrike.cc.invmanager.inventory.InventoryPlayerManager;
import com.bombstrike.cc.invmanager.item.ItemCatalyst;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityPlayerManager extends TileEntity implements IPeripheral, IPipeEntry, IInventory {
	// available methods
	protected String[] methodList = {
			"size",
			"read",
			"equipped",
			"move"
	};
	protected EntityPlayer player = null;
	protected IComputerAccess computer = null;
	protected ComputerCraft cc;
	protected int connections = 0;
	protected InventoryPlayerManager inventory;
	
	public TileEntityPlayerManager() {
		cc = new ComputerCraft(this);
		inventory = new InventoryPlayerManager();
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
				Block.blocksList[blockId].onNeighborBlockChange(worldObj, xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ, blockId);
			}
		}

		return this;
	}
	
	public EntityPlayer getPlayer() {
		return this.player;
	}
	
	public InventoryPlayerManager getInventory() {
		return inventory;
	}
	
	public boolean hasFuel() {
		// if we are connected to a computer
		if (connections > 0) {
			return true;
		}
		
		if (inventory.getStackInSlot(0) != null && inventory.getStackInSlot(0).getItem() instanceof ItemCatalyst) {
			return true;
		}
		
		return false;
	}
	
	public IInventory resolveInventory(String name) throws Exception {
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
		ByteArrayOutputStream data = new ByteArrayOutputStream(64);
		DataOutputStream writer = new DataOutputStream(data);

		try {
			NBTTagCompound tile = new NBTTagCompound(PacketHandler.PACKET.TILEDESCRIPTION.name());
			writeToNBT(tile);
			NBTTagCompound.writeNamedTag(tile, writer);
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
		if (isPlayerOn() && hasFuel()) {
			return getPlayer().inventory.getSizeInventory() - 4; // don't give access to armor
		}
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		if (isPlayerOn() && hasFuel()) {
			return getPlayer().inventory.getStackInSlot(var1);
		}
		return null;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if (isPlayerOn() && hasFuel()) {
			inventory.getStackInSlot(0).damageItem(amount, null);
			return getPlayer().inventory.decrStackSize(slot, amount);
		}
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		if (isPlayerOn() && hasFuel()) {
			return player.inventory.getStackInSlotOnClosing(var1);
		}
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		if (isPlayerOn() && hasFuel()) {
			inventory.getStackInSlot(0).damageItem(stack.stackSize, null);
			player.inventory.setInventorySlotContents(slot, stack);
		}
	}

	@Override
	public String getInvName() {
		if (isPlayerOn() && hasFuel()) {
			return player.inventory.getInvName();
		}
		return null;
	}

	@Override
	public int getInventoryStackLimit() {
		if (isPlayerOn() && hasFuel()) {
			return player.inventory.getInventoryStackLimit();
		}
		return 0;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : player.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void openChest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeChest() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbtData) {
		NBTTagList nbtInventory = new NBTTagList("inventory");
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null) {
				NBTTagCompound nbtStack = new NBTTagCompound();
				stack.writeToNBT(nbtStack);
				nbtStack.setInteger("slot", i);
				nbtInventory.appendTag(nbtStack);
			}
		}
		nbtData.setTag("inventory", nbtInventory);
		nbtData.setInteger("connections", connections);
		super.writeToNBT(nbtData);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtData) {
		super.readFromNBT(nbtData);
		if (nbtData.hasKey("inventory")) {
			NBTTagList list = (NBTTagList)nbtData.getTag("inventory");
			for (int i = 0; i < inventory.getSizeInventory() && i < list.tagCount(); i++) {
				if (list.tagAt(i) instanceof NBTTagCompound) {
					NBTTagCompound nbtStack = (NBTTagCompound)list.tagAt(i);
					int slot = nbtStack.getInteger("slot");
					ItemStack stack = ItemStack.loadItemStackFromNBT(nbtStack);
					if (stack != null) {
						inventory.setInventorySlotContents(slot, stack);
					}
				}
			}
		}
		if (nbtData.hasKey("connections")) {
			connections = nbtData.getInteger("connections");
		}
	}
}
