package com.bombstrike.cc.invmanager.compat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import buildcraft.api.transport.IPipeEntry;

import com.bombstrike.cc.invmanager.Utils;
import com.bombstrike.cc.invmanager.Utils.Manager;
import com.bombstrike.cc.invmanager.tileentity.BaseManager;

import dan200.computer.api.IComputerAccess;

// those are the methods called from computer craft
public class ComputerCraft {
	private BaseManager tileEntity;
	
	/**
	 * This class bind a task to a computer, and is used to queue calls and move them
	 * between threads
	 */
	public class Task {
		protected IComputerAccess computer;
		protected Callable<Object> callable;
		
		public Task(IComputerAccess computer, Callable<Object> callable) {
			this.computer = computer;
			this.callable = callable;
		}
		
		public Object call() throws Exception {
			return callable.call();
		}
		
		public IComputerAccess getComputer() {
			return computer;
		}
	}
	
	public ComputerCraft(BaseManager baseManager) {
		tileEntity = baseManager;
	}
	
	/**
	 * Send an object into a pipe
	 * @param arguments
	 * @return
	 * @throws Exception
	 */
	public Object send(Object[] arguments) throws Exception {
		IInventory source;
		IPipeEntry target;
		int sourceSlot, quantity;
		String targetDirection;
		
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
		// retrieve slot index and compensate
		sourceSlot = ((Double)arguments[1]).intValue() - 1;
		if (sourceSlot < 0 || sourceSlot >= source.getSizeInventory()) throw new Exception("source slot indice out of bounds");
		
		// target inventory
		if (!(arguments[2] instanceof String)) throw new Exception("invalid argument for target inventory");
		targetDirection = (String)arguments[2];
		target = Utils.getPipe(tileEntity, targetDirection);
		if (target == null) throw new Exception("the pipe \"" + (String)arguments[2] + "\" doesn't exists");
		
		// quantity
		if (arguments.length > 3) {
			if (!(arguments[3] instanceof Double)) throw new Exception("invalid argument for quantity");
			quantity = ((Double)arguments[3]).intValue();
			if (quantity < 1 || quantity > source.getInventoryStackLimit()) quantity = source.getInventoryStackLimit();
		}
		
		ItemStack sourceStack = source.getStackInSlot(sourceSlot);
		if (sourceStack != null) {
			ItemStack pipeStack = sourceStack.copy();
			pipeStack.stackSize = Math.min(pipeStack.stackSize, quantity);
			sourceStack.stackSize -= pipeStack.stackSize;
			target.entityEntering(pipeStack, Utils.directionFromName(targetDirection));
			
			// remove stack from source inventory if necessary
			if (sourceStack.stackSize == 0) {
				source.setInventorySlotContents(sourceSlot, null);
			}
			
			Map<String, Object> itemInfo = new HashMap<String, Object>();
			itemInfo.put("id", sourceStack.itemID);
			itemInfo.put("amount", sourceStack.stackSize);
			itemInfo.put("sent", pipeStack.stackSize);
			itemInfo.put("name", sourceStack.getItemName());
			itemInfo.put("display", sourceStack.getDisplayName());
			itemInfo.put("damage", sourceStack.getItemDamage());
			
			return itemInfo;
		}
		
		return null;
	}
	
	/**
	 * Move objects between inventories
	 * @param arguments
	 * @return
	 * @throws Exception
	 */
	public Object move(Object[] arguments) throws Exception {
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
		sourceSlot = ((Double)arguments[1]).intValue() - 1;
		if (sourceSlot < 0 || sourceSlot >= source.getSizeInventory()) throw new Exception("source slot indice out of bounds");
		
		// target inventory
		if (!(arguments[2] instanceof String)) throw new Exception("invalid argument for target inventory");
		target = tileEntity.resolveInventory((String)arguments[2]);
		if (target == null) throw new Exception("the inventory \"" + (String)arguments[2] + "\" doesn't exists");
		
		// target slot
		if (arguments.length > 3) {
			if (!(arguments[3] instanceof Double)) throw new Exception("invalid argument for target inventory slot");
			targetSlot = ((Double)arguments[3]).intValue() - 1;
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
			
			return itemInfo;
		}
		
		return null;
	}

	public Object read(Object[] arguments) throws Exception {
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
		sourceSlot = ((Double)arguments[1]).intValue() - 1;
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

			return itemInfo;
		}
		return 0;
	}
	
	public Object[] isInventory(Object[] arguments) throws Exception {
		IInventory target;
		
		// parse aguments
		if (arguments.length < 1) {
			throw new Exception("missing arguments");
		}
		
		if (!(arguments[0] instanceof String)) throw new Exception("invalid argument for source inventory");
		target = tileEntity.resolveInventory((String)arguments[0]);
		
		return new Boolean[]{target == null ? false : true};
	}
	
	public Object[] isPipe(Object[] arguments) throws Exception {
		IPipeEntry target;
		
		// parse aguments
		if (arguments.length < 1) {
			throw new Exception("missing arguments");
		}
		
		if (!(arguments[0] instanceof String)) throw new Exception("invalid argument for source inventory");
		target = Utils.getPipe(tileEntity, (String)arguments[0]);
		
		return new Boolean[]{target == null ? false : true};
	}

	public Object size(Object[] arguments) throws Exception {
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
		
		return source.getSizeInventory();
	}
}
