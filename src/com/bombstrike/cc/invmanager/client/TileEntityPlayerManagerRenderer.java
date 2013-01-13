package com.bombstrike.cc.invmanager.client;

import org.lwjgl.opengl.GL11;

import com.bombstrike.cc.invmanager.InventoryManager;
import com.bombstrike.cc.invmanager.TileEntityPlayerManager;


import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.ForgeHooksClient;

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
			render(x, y, z, (metadata & 0x8) > 0, metadata & 0x7, ((TileEntityPlayerManager)entity).getConnections());
		} else {
			render(x, y, z, false, 0, 0xF);
		}
	}
	
	public void render(double x, double y, double z, boolean lit, int orientation, int connections) {
		GL11.glPushMatrix();
		
		// rotate block based on orientation
		int angle;
		switch (orientation) {
		case 2:
			angle = 0;
			break;
		case 3:
			angle = 180;
			break;
		case 4:
			angle = 90;
			break;
		case 5:
		default:
			angle = 270;
			break;
		}

		GL11.glTranslated(x, y, z);
		
		// do the rotation
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		GL11.glRotatef(angle, 0, 1, 0);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		
		ForgeHooksClient.bindTexture("/com/bombstrike/cc/invmanager/gfx/blocks.png", 0);
		model.render(1.0F/16.0F, (lit && (connections > 0) ? STATUS.ACTIVE : (connections != 0 ? STATUS.ONLINE : STATUS.OFFLINE)), connections);

		GL11.glPopMatrix();
	}

}
