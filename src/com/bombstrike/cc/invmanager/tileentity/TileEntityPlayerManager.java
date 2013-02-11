package com.bombstrike.cc.invmanager.tileentity;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

import com.bombstrike.cc.invmanager.Utils;
import com.bombstrike.cc.invmanager.Utils.Manager;

import dan200.computer.api.IPeripheral;

public class TileEntityPlayerManager extends BaseManager implements IPeripheral, IInventory {
	public enum TYPE {
		BASIC,
		COMPUTER
	};

	protected EntityPlayer player = null;
	protected TYPE type;
	
	public TileEntityPlayerManager() {
		this(TYPE.BASIC);
	}
	
	public TileEntityPlayerManager(TYPE type) {
		super();
		this.type = type;
	}
	
	public TYPE getPlateType() {
		return type;
	}
	
	public boolean isPlayerOn() {
		return (player != null);
	}
	
	public boolean checkMode() {
		return (type == TYPE.BASIC) || (type == TYPE.COMPUTER && getComputerConnections() > 0); 
	}
	
	public TileEntityPlayerManager setPlayer(EntityPlayer player) {
		if (!checkMode()) return null;
		
		this.player = player;
		
		// actually tell everyone
		for (ForgeDirection direction: ForgeDirection.VALID_DIRECTIONS) {
			int blockId = Utils.getBlockNeighbor(worldObj, xCoord, yCoord, zCoord, direction);
			if (blockId > 0 && Block.blocksList[blockId] != null) {
				Block.blocksList[blockId].onNeighborBlockChange(worldObj, xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ, blockId);
			}
		}
		
		// tell every computers that someone step on
		queueEvent("player", new Boolean[]{player != null});

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
	public boolean canAttachToSide(int side) {
		if (side == 1) return false; // doesn't make sense to attach when under
		return true;
	}

	/**
	 * Inventory implementation
	 */
	
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
	public void openChest() {
		
	}

	@Override
	public void closeChest() {
		
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbtData) {
		nbtData.setString("type", type.name());
		super.writeToNBT(nbtData);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtData) {
		super.readFromNBT(nbtData);
		if (nbtData.hasKey("type")) {
			type = TYPE.valueOf(nbtData.getString("type"));
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return false;
	}
	
	@Override
	public int addItem(ItemStack stack, boolean doAdd, ForgeDirection from) {
		// the plate only accept items from the bottom
		if (from != ForgeDirection.DOWN) return 0;
		// don't do the computer stuff on the basic plate
		if (type == TYPE.BASIC) {
			if (player == null) {
				return 0;
			}

			// just add items to the player inventory
			Manager manager = new Manager(player.inventory);
			if (doAdd) {
				return manager.add(stack.copy());
			} else {
				return manager.available(stack);
			}
		} else {
			return super.addItem(stack, doAdd, from);
		}
	}
}
