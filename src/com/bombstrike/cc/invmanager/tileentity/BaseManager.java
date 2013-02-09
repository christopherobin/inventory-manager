package com.bombstrike.cc.invmanager.tileentity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;

import com.bombstrike.cc.invmanager.Utils;
import com.bombstrike.cc.invmanager.compat.ComputerCraft;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class BaseManager extends TileEntity implements IPeripheral {
	/**
	 * This map contains the list of computers currently connected to this device
	 */
	protected Map<Integer, IComputerAccess> computers = null;
	/**
	 * This is an instance of our ComputerCraft class, that takes care of running
	 * the actual methods
	 */
	protected ComputerCraft cc = null;
	/**
	 * This is the list of methods that are provided to ComputerCraft
	 */
	final static protected String[] methodList = {
			"_size",
			"_read",
			"_equipped",
			"_move"
	};

	public BaseManager() {
		// attach and detach are called from the LUA thread, so we need a thread
		// safe class
		computers = new ConcurrentHashMap<Integer, IComputerAccess>();
	}
	
	/**
	 * This method returns a valid inventory for the string provided
	 * @param name Either one of "down", "up", "north", "south", "east", "west" and for the plate: "player"
	 * @return null if no inventory is found, otherwise an instance of IInventory
	 * @throws Exception
	 */
	public IInventory resolveInventory(String name) throws Exception {
		// the plate is only compatible with player and down directions
		if (name.equals("player") || name.equals("down")) {
			return Utils.getInventory(this, name);
		}

		return null;
	}

	/**
	 * Queue an event with no arguments on all connected computers
	 * @param event The event name
	 */
	public void queueEvent(String event) {
		queueEvent(event, null);
	}
	
	/**
	 * Queue an event with the given arguments on all connected computers
	 * @param event The event name
	 * @param argument Any of the Lua compatible object
	 */
	public void queueEvent(String event, Object[] arguments) {
		queueEvent(event, arguments, null);
	}
	
	/**
	 * Queue an event with the given arguments with either the target computer, or every
	 * computers if set to null
	 * @param event
	 * @param arguments
	 * @param targetComputer null to send to all computers, or a valid IComputerAccess instance
	 */
	public void queueEvent(String event, Object[] arguments, IComputerAccess targetComputer) {
		if (targetComputer == null) {
			for (IComputerAccess computer : computers.values()) {
				computer.queueEvent(event, arguments);
			}
		} else {
			targetComputer.queueEvent(event, arguments);
		}
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
		return null;
	}

	@Override
	public boolean canAttachToSide(int side) {
		// the default implementation accept connections from everywhere
		return true;
	}

	@Override
	public void attach(IComputerAccess computer) {
		computers.put(computer.getID(), computer);
	}

	@Override
	public void detach(IComputerAccess computer) {
		if (computers.containsKey(computer.getID())) {
			computers.remove(computer.getID());
		}
	}
}
