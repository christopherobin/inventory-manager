package com.bombstrike.cc.invmanager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.bombstrike.cc.invmanager.tileentity.TileEntityPlayerManager;

import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class Utils {
	private static final Map<String, ForgeDirection> directionMap;
    static {
        Map<String, ForgeDirection> tempMap = new HashMap<String, ForgeDirection>(6);
        tempMap.put("down", ForgeDirection.DOWN);
        tempMap.put("up", ForgeDirection.UP);
        tempMap.put("north", ForgeDirection.NORTH);
        tempMap.put("south", ForgeDirection.SOUTH);
        tempMap.put("west", ForgeDirection.WEST);
        tempMap.put("east", ForgeDirection.EAST);
        directionMap = Collections.unmodifiableMap(tempMap);
    }
	
	static public Manager getInventoryManager(IInventory inventory) {
		return new Manager(inventory);
	}
	
	static public TileEntity getTileNeighbor(World world, int x, int y, int z, ForgeDirection direction) {
		TileEntity neighbor = world.getBlockTileEntity(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
		return neighbor;
	}
	
	static public int getBlockNeighbor(World world, int x, int y, int z, ForgeDirection direction) {
		int neighbor = world.getBlockId(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
		return neighbor;
	}
	
	static public IInventory getInventory(TileEntityPlayerManager entity, String name) throws Exception {
		if (name.contentEquals("player")) {
			if (entity.isPlayerOn()) {
				return entity.getPlayer().inventory;
			}
			throw new Exception("no player connected");
		}

		if (directionMap.containsKey(name)) {
			TileEntity te = Utils.getTileNeighbor(entity.worldObj, entity.xCoord, entity.yCoord, entity.zCoord, directionMap.get(name));
			if (te != null && te instanceof IInventory) {
				int blockID = getBlockNeighbor(entity.worldObj, entity.xCoord, entity.yCoord, entity.zCoord, directionMap.get(name)); 
				// check for double chest
				if (te instanceof TileEntityChest) {
					ForgeDirection directions[] = {ForgeDirection.SOUTH, ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.WEST};
					for (ForgeDirection direction : directions) {
						if (getBlockNeighbor(te.worldObj, te.xCoord, te.yCoord, te.zCoord, direction) == blockID) {
							if ((direction.ordinal() % 2) == 0) {
								return new InventoryLargeChest("container.chestDouble", (IInventory)getTileNeighbor(te.worldObj, te.xCoord, te.yCoord, te.zCoord, direction), (IInventory)te);
							} else {
								return new InventoryLargeChest("container.chestDouble", (IInventory)te, (IInventory)getTileNeighbor(te.worldObj, te.xCoord, te.yCoord, te.zCoord, direction));
							}
						}
					}
				}
				return (IInventory)te;
			}
		}

		return null;
	}
	
	public static class Manager {
		IInventory inventory = null;
		public Manager(IInventory inventory) {
			this.inventory = inventory;
		}
		
		private boolean stackBelongInSlot(ItemStack stack, int slot) {
			// we need to prevent peoples from doing weird things to the player
			if (inventory instanceof InventoryPlayer) {
				// armor slots are 36 to 39
				if (slot > 35) {
					// item must be an armor
					if (!(stack.getItem() instanceof ItemArmor)) return false;
					if (((ItemArmor)stack.getItem()).armorType != (3 - (slot - 36))) return false;
					if (inventory.getStackInSlot(slot) != null) return false;
				}
			}
			return true;
		}
		
		public int add(ItemStack stack) {
			return add(stack, -1, stack.stackSize);
		}
		
		public int add(ItemStack stack, int slot, int quantity) {
			int invSize = inventory.getSizeInventory();
			ItemStack destStack;
			
			int originalAmount = stack.stackSize;
			if (stack.stackSize > quantity) {
				stack.stackSize = quantity;
			} else {
				quantity = stack.stackSize;
			}
			
			int amountLeft = stack.stackSize;
			int amountToSet;
			int i = (slot > 0 && slot < invSize ? slot : 0);

			for (; i < invSize && amountLeft > 0; i++) {
				// ignore invalid slots for that item
				if (!stackBelongInSlot(stack, i)) continue;
				// search for an available inventory
				destStack = inventory.getStackInSlot(i);
				if (destStack == null) {
					// check amount to send, based on stack limit
					amountToSet = Math.min(amountLeft, inventory.getInventoryStackLimit());
					// set distant slot content
					ItemStack newStack = stack.copy();
					newStack.stackSize = amountToSet;
					inventory.setInventorySlotContents(i, newStack);
					// reduce amount left
					amountLeft -= inventory.getInventoryStackLimit();
				} else if (destStack.itemID == stack.itemID && (!stack.getHasSubtypes() || stack.getItemDamage() == destStack.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack, destStack)) {
					// retrieve the space available in that slot
					int spaceAvailable = (inventory.getInventoryStackLimit() - destStack.stackSize);
					if (spaceAvailable == 0) continue; // no use going any further
					// find how much we can set
					amountToSet = Math.min(destStack.stackSize + spaceAvailable, destStack.stackSize + amountLeft);
					// send to inventory
					destStack.stackSize = amountToSet;
					//inventory.setInventorySlotContents(i, new ItemStack(stack.getItem(), amountToSet));
					// reduce amount left
					amountLeft -= spaceAvailable;
				}
			}

			if (amountLeft < 0) amountLeft = 0;
			stack.stackSize = amountLeft;
			
			// if a slot was specified and we could only move part of the stuff, try again from the start
			if (slot > 0 && amountLeft > 0 && amountLeft != stack.stackSize) {
				stack.stackSize = amountLeft;
				amountLeft = add(stack, 0, amountLeft);
			}
			
			// no item left to move
			stack.stackSize = (originalAmount - quantity) + amountLeft;
			return originalAmount - amountLeft;
		}
	}
}
