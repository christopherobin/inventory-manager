package com.bombstrike.cc.invmanager.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;

import com.bombstrike.cc.invmanager.InventoryManager;
import com.bombstrike.cc.invmanager.tileentity.TileEntityPlayerManager;

public class TileEntityPlayerManagerRenderer extends TileEntitySpecialRenderer {
	private ModelPlayerManager model = new ModelPlayerManager();
	
	enum STATUS { OFFLINE, ONLINE, ACTIVE };

	public TileEntityPlayerManagerRenderer() {

	}

	private int previous = 0;
	@Override
	public void renderTileEntityAt(TileEntity entity, double x, double y,
			double z, float partialTicks) {
		if (entity instanceof TileEntityPlayerManager) {
			TileEntityPlayerManager playerManagerEntity = (TileEntityPlayerManager)entity;
			int metadata = playerManagerEntity.getBlockMetadata();
			
			STATUS status = STATUS.OFFLINE;
			if (playerManagerEntity.hasFuel()) {
				status = STATUS.ONLINE;
				if ((metadata & 0x8) > 0) {
					status = STATUS.ACTIVE;
				}
			}
			render(x, y, z, status, ((TileEntityPlayerManager)entity).getConnections());
		} else {
			render(x, y, z, STATUS.ONLINE, 0x0);
		}
	}
	
	public void render(double x, double y, double z, STATUS status, int connections) {
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		
		ForgeHooksClient.bindTexture("/com/bombstrike/cc/invmanager/gfx/blocks.png", 0);
		
		model.render(1.0F/16.0F, status, connections);

		GL11.glPopMatrix();
	}

}
