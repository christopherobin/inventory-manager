package com.bombstrike.cc.invmanager.inventory;

import com.bombstrike.cc.invmanager.item.ItemCatalyst;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotCatalyst extends Slot {

	public SlotCatalyst(IInventory par1iInventory, int par2, int par3, int par4) {
		super(par1iInventory, par2, par3, par4);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return (stack.getItem() instanceof ItemCatalyst);
	}
	
	@Override
	public int getSlotStackLimit() {
		return 1; // only one catalyst at a time
	}

}
