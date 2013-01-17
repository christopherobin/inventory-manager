package com.bombstrike.cc.invmanager.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemCatalyst extends Item {
	public ItemCatalyst(int itemId) {
		super(itemId);
		setMaxDamage(10);
		setTextureFile("/com/bombstrike/cc/invmanager/gfx/items.png");
		setItemName("playerManagerCatalyst");
		setCreativeTab(CreativeTabs.tabMaterials);
		setIconIndex(0);
		setIconCoord(0, 0);
	}
}
