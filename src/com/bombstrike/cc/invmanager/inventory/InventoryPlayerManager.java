package com.bombstrike.cc.invmanager.inventory;

import com.bombstrike.cc.invmanager.InventoryManager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public class InventoryPlayerManager extends InventoryBasic {

	public InventoryPlayerManager() {
		super("inventory.playerManager", 4);
	}
}
