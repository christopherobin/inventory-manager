package com.bombstrike.cc.invmanager.client.gui;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import com.bombstrike.cc.invmanager.inventory.ContainerPlayerManager;
import com.bombstrike.cc.invmanager.inventory.InventoryPlayerManager;
import com.bombstrike.cc.invmanager.tileentity.TileEntityPlayerManager;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

public class GuiPlayerManager extends GuiContainer {

	public GuiPlayerManager(InventoryPlayer inventory, TileEntityPlayerManager entity) {
		super(new ContainerPlayerManager(inventory, entity));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2,
			int var3) {
		int texture = mc.renderEngine.getTexture("/com/bombstrike/cc/invmanager/gfx/plate.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(texture);
		drawTexturedModalRect((width - xSize) / 2, (height - ySize) / 2, 0, 0, xSize, ySize);
	}

}
