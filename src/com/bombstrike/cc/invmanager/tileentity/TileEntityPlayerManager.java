package com.bombstrike.cc.invmanager.tileentity;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.bombstrike.cc.invmanager.InventoryManager;
import com.bombstrike.cc.invmanager.Utils;
import com.bombstrike.cc.invmanager.compat.ComputerCraft;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityPlayerManager extends BaseManager implements IPeripheral, IInventory {
	public enum TYPE {
		BASIC,
		COMPUTER
	};

	protected EntityPlayer player = null;
	protected int connections = 0;
	protected TYPE type;
	protected ConcurrentLinkedQueue<FutureTask<Object[]>> callQueue;
	
	public TileEntityPlayerManager() {
		this(TYPE.BASIC);
	}
	
	public TileEntityPlayerManager(TYPE type) {
		callQueue = new ConcurrentLinkedQueue<FutureTask<Object[]>>();
		this.type = type;
	}
	
	public TYPE getPlateType() {
		return type;
	}
	
	public boolean isPlayerOn() {
		return (player != null);
	}
	
	public boolean checkMode() {
		return (type == TYPE.BASIC) || (type == TYPE.COMPUTER && connections > 0); 
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		FutureTask<Object[]> task;
		while ((task = callQueue.poll()) != null) {
			task.run();
		}
	}
	
	public TileEntityPlayerManager setPlayer(EntityPlayer player) {
		if (!checkMode()) return null;
		
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

	public IInventory resolveInventory(String name) throws Exception {
		if (!checkMode()) return null;

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
	public Packet getDescriptionPacket() {
		NBTTagCompound data = new NBTTagCompound("data");
		writeToNBT(data);
		Packet132TileEntityData packet = new Packet132TileEntityData(xCoord, yCoord, zCoord, 0, data);
		
		return packet;
	}
	
	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet) {
		super.onDataPacket(net, packet);
		readFromNBT(packet.customParam1);
	}
	
	public void setConnections(int data) {
		connections = data;
		// if on the server, update the client
		if (!worldObj.isRemote) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	public int getConnections() {
		return connections;
	}
	
	public FutureTask<Object[]> queueCallable(Callable<Object[]> callable) {
		FutureTask<Object[]> task = new FutureTask<Object[]>(callable);
		callQueue.add(task);
		return task;
	}
	
	@Override
	public Object[] callMethod(IComputerAccess computer, int method,
			final Object[] arguments) throws Exception {
		// lazy constructor
		if (cc == null) cc = new ComputerCraft(this);
		// resolve calls
		Callable<Object[]> callable;
		switch (method) {
		case 0: // size
			callable = new Callable<Object[]>() {
				@Override public Object[] call() throws Exception { return cc.size(arguments); }
			};
			break;
		case 1: // inventory <int:slot>
			callable = new Callable<Object[]>() {
				@Override public Object[] call() throws Exception { return cc.read(arguments); }
			};
			break;
		case 2: // equipped
			if (!isPlayerOn()) throw new Exception("no player connected");
			callable = new Callable<Object[]>() {
				@Override public Object[] call() throws Exception { return new Integer[]{player.inventory.currentItem}; }
			};
			break;
		case 3: // export
			callable = new Callable<Object[]>() {
				@Override public Object[] call() throws Exception { return cc.move(arguments); }
			};
			break;
		default:
			throw new Exception("unknown method");
		}

		FutureTask<Object[]> task = queueCallable(callable);
		return task.get();
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
	public int getSizeInventory() {
		if (!checkMode()) return 0;

		if (isPlayerOn()) {
			return getPlayer().inventory.getSizeInventory() - 4; // don't give access to armor
		}
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		if (!checkMode()) return null;

		if (isPlayerOn()) {
			return getPlayer().inventory.getStackInSlot(var1);
		}
		return null;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if (!checkMode()) return null;

		if (isPlayerOn()) {
			return getPlayer().inventory.decrStackSize(slot, amount);
		}
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		if (!checkMode()) return null;

		if (isPlayerOn()) {
			return player.inventory.getStackInSlotOnClosing(var1);
		}
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		if (!checkMode()) return;

		if (isPlayerOn()) {
			player.inventory.setInventorySlotContents(slot, stack);
		}
	}

	@Override
	public String getInvName() {
		if (!checkMode()) return null;

		if (isPlayerOn()) {
			return player.inventory.getInvName();
		}
		return null;
	}

	@Override
	public int getInventoryStackLimit() {
		if (!checkMode()) return 0;

		if (isPlayerOn()) {
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
		
	}

	@Override
	public void closeChest() {
		
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbtData) {
		nbtData.setString("type", type.name());
		nbtData.setInteger("connections", connections);
		super.writeToNBT(nbtData);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtData) {
		super.readFromNBT(nbtData);
		if (nbtData.hasKey("type")) {
			type = TYPE.valueOf(nbtData.getString("type"));
		}
		if (nbtData.hasKey("connections")) {
			connections = nbtData.getInteger("connections");
		}
	}
}
