package com.bombstrike.cc.invmanager.compat;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.transport.IPipeEntry;

import com.bombstrike.cc.invmanager.Utils;
import com.bombstrike.cc.invmanager.Utils.Manager;
import com.bombstrike.cc.invmanager.tileentity.TileEntityPlayerManager;

// those are the methods called from computer craft
public class ComputerCraft {
	private TileEntityPlayerManager tileEntity;
	
	public ComputerCraft(TileEntityPlayerManager entity) {
		tileEntity = entity;
	}
	
	public Object[] move(Object[] arguments) throws Exception {
		// variables
		IInventory source;
		IInventory target;
		int sourceSlot, targetSlot = 0, quantity;
		
		// parse aguments
		if (arguments.length < 3) {
			throw new Exception("missing arguments");
		}
		
		// source inventory
		if (!(arguments[0] instanceof String)) throw new Exception("invalid argument for source inventory");
		source = tileEntity.resolveInventory((String)arguments[0]);
		if (source == null) throw new Exception("the inventory \"" + (String)arguments[0] + "\" doesn't exists");
		// set the default quantity value to be the size of a stack
		quantity = source.getInventoryStackLimit();
		
		// source slot
		if (!(arguments[1] instanceof Double)) throw new Exception("invalid argument for source inventory slot");
		sourceSlot = ((Double)arguments[1]).intValue();
		if (sourceSlot < 0 || sourceSlot >= source.getSizeInventory()) throw new Exception("source slot indice out of bounds");
		
		// target inventory
		if (!(arguments[2] instanceof String)) throw new Exception("invalid argument for target inventory");
		target = tileEntity.resolveInventory((String)arguments[2]);
		if (target == null) throw new Exception("the inventory \"" + (String)arguments[2] + "\" doesn't exists");
		
		// target slot
		if (arguments.length > 3) {
			if (!(arguments[3] instanceof Double)) throw new Exception("invalid argument for target inventory slot");
			targetSlot = ((Double)arguments[3]).intValue();
			if (targetSlot < 0 || targetSlot >= target.getSizeInventory()) throw new Exception("target slot indice out of bounds");
		}
		
		// quantity
		if (arguments.length > 4) {
			if (!(arguments[4] instanceof Double)) throw new Exception("invalid argument for quantity");
			quantity = ((Double)arguments[4]).intValue();
			if (quantity < 1 || quantity > source.getInventoryStackLimit()) quantity = source.getInventoryStackLimit();
		}
		
		ItemStack sourceStack = source.getStackInSlot(sourceSlot);
		if (sourceStack != null) {
			Manager manager = Utils.getInventoryManager(target);
			int moved = manager.add(sourceStack, targetSlot, quantity);
			// remove stack from source inventory if necessary
			if (sourceStack.stackSize == 0) {
				source.setInventorySlotContents(sourceSlot, null);
			}
			
			Map<String, Object> itemInfo = new HashMap<String, Object>();
			itemInfo.put("id", sourceStack.itemID);
			itemInfo.put("amount", sourceStack.stackSize);
			itemInfo.put("moved", moved);
			itemInfo.put("name", sourceStack.getItemName());
			itemInfo.put("display", sourceStack.getDisplayName());
			itemInfo.put("damage", sourceStack.getItemDamage());
			
			return new Object[]{itemInfo};
		}
		
		return new Integer[]{};
	}

	public Object[] read(Object[] arguments) throws Exception {
		// variables
		IInventory source;
		int sourceSlot;
		
		// parse aguments
		if (arguments.length < 2) {
			throw new Exception("missing arguments");
		}
		
		// source inventory
		if (!(arguments[0] instanceof String)) throw new Exception("invalid argument for source inventory");
		source = tileEntity.resolveInventory((String)arguments[0]);
		if (source == null) throw new Exception("the inventory \"" + (String)arguments[0] + "\" doesn't exists");
		
		// source slot
		if (!(arguments[1] instanceof Double)) throw new Exception("invalid argument for source inventory slot");
		sourceSlot = ((Double)arguments[1]).intValue();
		if (sourceSlot < 0 || sourceSlot >= source.getSizeInventory()) throw new Exception("source slot indice out of bounds");
		
		// retrieve details
		ItemStack stack = source.getStackInSlot(sourceSlot);
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

	public Object[] size(Object[] arguments) throws Exception {
		// variables
		IInventory source;
		
		// parse aguments
		if (arguments.length < 1) {
			throw new Exception("missing arguments");
		}
		
		// source inventory
		if (!(arguments[0] instanceof String)) throw new Exception("invalid argument for source inventory");
		source = tileEntity.resolveInventory((String)arguments[0]);
		if (source == null) throw new Exception("the inventory \"" + (String)arguments[0] + "\" doesn't exists");
		
		return new Integer[]{source.getSizeInventory()};
	}
}
