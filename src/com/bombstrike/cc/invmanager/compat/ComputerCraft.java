package com.bombstrike.cc.invmanager.compat;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.transport.IPipeEntry;

import com.bombstrike.cc.invmanager.TileEntityPlayerManager;
import com.bombstrike.cc.invmanager.Utils;

// those are the methods called from computer craft
public class ComputerCraft {
	private TileEntityPlayerManager tileEntity;
	
	public ComputerCraft(TileEntityPlayerManager entity) {
		tileEntity = entity;
	}
	
	public Object[] exportItem(Object[] arguments, IInventory from) throws Exception {
		int size = from.getSizeInventory();
		
		if (arguments.length == 0 || !(arguments[0] instanceof Double)) {
			throw new Exception("missing/invalid argument: slot (int)");
		}
		
		int position = ((Double)arguments[0]).intValue();
		// out of bounds
		if (position < 0 || position >= size) throw new Exception("inventory indice out of bounds");
		// do we have a valid target?
		TileEntity neighbor = tileEntity.worldObj.getBlockTileEntity(tileEntity.xCoord, tileEntity.yCoord-1, tileEntity.zCoord);
		if (!(neighbor instanceof IInventory) && !(neighbor instanceof IPipeEntry)) throw new Exception("bottom block is not an inventory nor a pipe"); // no valid target :(
		// if a pipe, does it accept items?
		if (neighbor instanceof IPipeEntry && !((IPipeEntry)neighbor).acceptItems()) throw new Exception("this pipe cannot accept items");
		// retrieve details
		ItemStack stack = from.decrStackSize(position, 64);
		if (stack != null) {
			int amountExtracted = stack.stackSize;
			// send it to our neighbor
			if (neighbor instanceof IPipeEntry) {
				((IPipeEntry)neighbor).entityEntering(stack, ForgeDirection.DOWN);
			} else if (neighbor instanceof IInventory) {
				IInventory inventory = (IInventory)neighbor;
				Utils.Manager manager = Utils.getInventoryManager(inventory);
				amountExtracted = manager.add(stack);
				// send back any extra
				if (stack.stackSize > 0) {
					from.setInventorySlotContents(position, stack);
				}
			}

			Map<String, Object> itemInfo = new HashMap<String, Object>();
			itemInfo.put("id", stack.itemID);
			itemInfo.put("amount", stack.stackSize);
			itemInfo.put("exported", amountExtracted);
			itemInfo.put("name", stack.getItemName());
			itemInfo.put("display", stack.getDisplayName());
			itemInfo.put("damage", stack.getItemDamage());
			
			return new Object[]{itemInfo};
		}
		return new Integer[]{0, 0};
	}
	
	public Object[] importItem(Object[] arguments, IInventory target) throws Exception {
		int size = target.getSizeInventory();
		
		if (arguments.length == 0 || !(arguments[0] instanceof Double)) {
			throw new Exception("missing/invalid argument: from (int)");
		}
		
		int from = ((Double)arguments[0]).intValue();
		// out of bounds
		int to = -1;
		if (arguments.length > 1) {
			if (!(arguments[1] instanceof Double)) {
				throw new Exception("missing/invalid argument: to (int)");
			}
			to = ((Double)arguments[1]).intValue();
		}
		if (to < 0 || to >= size) throw new Exception("target indice out of bounds");
		// do we have a valid target?
		TileEntity neighbor = tileEntity.worldObj.getBlockTileEntity(tileEntity.xCoord, tileEntity.yCoord-1, tileEntity.zCoord);
		if (neighbor instanceof IPipeEntry) throw new Exception("cannot manually import from a pipe");
		if (!(neighbor instanceof IInventory)) throw new Exception("bottom block is not an inventory"); // no valid target :(
		// retrieve neighbor inventory
		IInventory inventory = (IInventory)neighbor;
		int invSize = inventory.getSizeInventory();
		if (from < 0 || from >= invSize) throw new Exception("inventory indice out of bounds");

		// retrieve details
		ItemStack stack = inventory.decrStackSize(from, 64);
		if (stack != null) {
			int amountExtracted = stack.stackSize;
			// send it to the player
			Utils.Manager manager = Utils.getInventoryManager(target);
			amountExtracted = manager.add(stack);

			// send back any extra
			if (stack.stackSize > 0) {
				inventory.setInventorySlotContents(from, stack);
			}

			Map<String, Object> itemInfo = new HashMap<String, Object>();
			itemInfo.put("id", stack.itemID);
			itemInfo.put("amount", stack.stackSize);
			itemInfo.put("imported", amountExtracted);
			itemInfo.put("name", stack.getItemName());
			itemInfo.put("display", stack.getDisplayName());
			itemInfo.put("damage", stack.getItemDamage());

			return new Object[]{itemInfo};
		}
		return new Integer[]{0, 0};
	}
	
	public Object[] readInventory(Object[] arguments, IInventory target) throws Exception {
		int size = target.getSizeInventory();
		
		if (arguments.length == 0 || !(arguments[0] instanceof Double)) {
			throw new Exception("missing/invalid argument: slot (int)");
		}
		
		int position = ((Double)arguments[0]).intValue();
		// out of bounds
		if (position < 0 || position >= size) throw new Exception("inventory indice out of bounds");;
		// retrieve details
		ItemStack stack = target.getStackInSlot(position);
		if (stack != null) {
			Map<String, Object> itemInfo = new HashMap<String, Object>();
			itemInfo.put("id", stack.itemID);
			itemInfo.put("amount", stack.stackSize);
			itemInfo.put("name", stack.getItemName());
			itemInfo.put("display", stack.getDisplayName());
			itemInfo.put("damage", stack.getItemDamage());

			return new Object[]{itemInfo};
		}
		return new Integer[]{0, 0};
	}

}
