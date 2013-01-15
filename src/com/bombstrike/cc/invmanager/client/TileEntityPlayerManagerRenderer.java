package com.bombstrike.cc.invmanager.client;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;

import com.bombstrike.cc.invmanager.TileEntityPlayerManager;

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
			int metadata = entity.getBlockMetadata();
			render(x, y, z, (metadata & 0x8) > 0, ((TileEntityPlayerManager)entity).getConnections());
		} else {
			render(x, y, z, false, 0x0);
		}
	}
	
	public void render(double x, double y, double z, boolean lit, int connections) {
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		
		ForgeHooksClient.bindTexture("/com/bombstrike/cc/invmanager/gfx/blocks.png", 0);
		model.render(1.0F/16.0F, (lit && (connections > 0) ? STATUS.ACTIVE : (connections != 0 ? STATUS.ONLINE : STATUS.OFFLINE)), connections);

		GL11.glPopMatrix();
	}

}
