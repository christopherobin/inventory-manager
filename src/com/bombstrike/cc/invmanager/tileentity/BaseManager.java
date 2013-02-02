package com.bombstrike.cc.invmanager.tileentity;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;

import com.bombstrike.cc.invmanager.Utils;
import com.bombstrike.cc.invmanager.compat.ComputerCraft;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class BaseManager extends TileEntity implements IPeripheral {
	protected IComputerAccess computer = null;
	protected ComputerCraft cc = null;
	// available methods
	final static protected String[] methodList = {
			"_size",
			"_read",
			"_equipped",
			"_move"
	};

	public BaseManager() {
		
	}
	
	public IInventory resolveInventory(String name) throws Exception {
		// the plate is only compatible with player and down directions
		if (name.equals("player") || name.equals("down")) {
			return Utils.getInventory(this, name);
		}

		return null;
	}
	
	/*********************************
	 *  IPeripheral Implementation
	 *********************************/

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getMethodNames() {
		return BaseManager.methodList;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method,
			Object[] arguments) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canAttachToSide(int side) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void attach(IComputerAccess computer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void detach(IComputerAccess computer) {
		// TODO Auto-generated method stub
		
	}
}
