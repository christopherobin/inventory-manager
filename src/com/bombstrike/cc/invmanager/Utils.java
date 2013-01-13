package com.bombstrike.cc.invmanager;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class Utils {
	static public Manager getInventoryManager(IInventory inventory) {
		return new Manager(inventory);
	}
	
	static public TileEntity getTileNeighbor(World world, int x, int y, int z, ForgeDirection direction) {
		//InventoryManager.logger.info("Getting Tile Entity at {" + (x + direction.offsetX) + "," + (y + direction.offsetY) + "," + (z + direction.offsetZ) + "}");
		TileEntity neighbor = world.getBlockTileEntity(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
		return neighbor;
	}
	
	public static class Manager {
		IInventory inventory = null;
		public Manager(IInventory inventory) {
			this.inventory = inventory;
		}
		
		public ItemStack add(ItemStack stack) {
			return add(stack, -1);
		}
		
		public ItemStack add(ItemStack stack, int slot) {
			int invSize = inventory.getSizeInventory();
			ItemStack destStack;

			int amountLeft = stack.stackSize;
			int amountToSet;
			int i = (slot > 0 && slot < invSize ? slot : 0);

			for (; i < invSize && amountLeft > 0; i++) {
				// search for an available inventory
				destStack = inventory.getStackInSlot(i);
				if (destStack == null) {
					// check amount to send, based on stack limit
					amountToSet = Math.min(amountLeft, inventory.getInventoryStackLimit());
					// set distant slot content
					inventory.setInventorySlotContents(i, new ItemStack(stack.getItem(), amountToSet));
					// reduce amount left
					amountLeft -= inventory.getInventoryStackLimit();
				} else if (destStack.itemID == stack.itemID) {
					// retrieve the space available in that slot
					int spaceAvailable = (inventory.getInventoryStackLimit() - destStack.stackSize);
					if (spaceAvailable == 0) continue; // no use going any further
					// find how much we can set
					amountToSet = Math.min(destStack.stackSize + spaceAvailable, destStack.stackSize + amountLeft);
					// send to inventory
					inventory.setInventorySlotContents(i, new ItemStack(stack.getItem(), amountToSet));
					// reduce amount left
					amountLeft -= spaceAvailable;
				}
			}

			// if a slot was specified and we could only move part of the stuff, try again from the start
			if (slot > 0 && amountLeft > 0 && amountLeft != stack.stackSize) {
				add(new ItemStack(stack.getItem(), amountLeft), 0);
			}
			
			// no item left
			if (amountLeft <= 0) return null;
			return new ItemStack(stack.getItem(), amountLeft);
		}
	}
}
