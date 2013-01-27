package com.bombstrike.cc.invmanager.client.render;


import com.bombstrike.cc.invmanager.tileentity.TileEntityPlayerManager;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelPlayerManager extends ModelBase {
	// fields
	ModelRenderer plates[];
	ModelRenderer connections[];

	public ModelPlayerManager() {
		textureWidth = 128;
		textureHeight = 128;

		plates = new ModelRenderer[] {
			new ModelRenderer(this, 0, 0),
			new ModelRenderer(this, 0, 16),
			new ModelRenderer(this, 0, 32),
			new ModelRenderer(this, 56, 0),
			new ModelRenderer(this, 56, 16),
			new ModelRenderer(this, 56, 32)
		};
		for (ModelRenderer plate : plates) {
			plate.addBox(1F, 0F, 1F, 14, 2, 14);
			plate.setRotationPoint(0F, 0F, 0F);
			plate.setTextureSize(textureWidth, textureHeight);
		}
		
		// the four connections 0 is left, 1 is up, 2 is right, 3 is down
		connections = new ModelRenderer[]{
				new ModelRenderer(this, 56, 0),
				new ModelRenderer(this, 56, 0),
				new ModelRenderer(this, 56, 0),
				new ModelRenderer(this, 56, 0)
		};

		// left
		connections[0].addBox(5F, 0F, -1F, 6, 1, 1);
		connections[0].setRotationPoint(0F, 0F, 0F);
		setRotation(connections[0], 0.0F, -(float)Math.PI/2.0F, 0.0F);
		
		// top
		connections[1].addBox(5F, 0F, 0F, 6, 1, 1);
		connections[1].setRotationPoint(0F, 0F, 0F);
		
		// right
		connections[2].addBox(5F, 0F, -16F, 6, 1, 1);
		connections[2].setRotationPoint(0F, 0F, 0F);
		setRotation(connections[2], 0.0F, -(float)Math.PI/2.0F, 0.0F);
		
		// bottom
		connections[3].addBox(5F, 0F, 15F, 6, 1, 1);
		connections[3].setRotationPoint(0F, 0F, 0F);
		
		for (ModelRenderer connection : connections) {
			connection.setTextureSize(textureWidth, textureHeight);
		}
	}

	public void render(float scale, TileEntityPlayerManager.TYPE type, TileEntityPlayerManagerRenderer.STATUS status, int connectionsField) {
		int idx = 0;
		if (type == type.BASIC) idx = 3;
		
		switch (status) {
		default:
		case OFFLINE:
			plates[idx].render(scale);
			break;
		case ONLINE:
			plates[idx+1].render(scale);
			break;
		case ACTIVE:
			plates[idx+2].render(scale);
		}

		// the connectionsField int is a bitfield were bit 1 is left, bit 2 top, etc...
		if ((connectionsField & 0x1) > 0) connections[0].render(scale);
		if ((connectionsField & 0x2) > 0) connections[1].render(scale);
		if ((connectionsField & 0x4) > 0) connections[2].render(scale);
		if ((connectionsField & 0x8) > 0) connections[3].render(scale);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
