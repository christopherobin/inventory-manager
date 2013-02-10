package com.bombstrike.cc.invmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler {
	public void prepareApis() {
		try {
			// create target directory if necessary
			File targetFile = new File("mods/invmanager-lua/invmanager");
			if (!targetFile.getParentFile().exists()) {
				targetFile.getParentFile().mkdirs();
			}
			
			long targetSize = 0;
			if (targetFile.exists()) {
				targetSize = targetFile.length();
			}
			
			// retrieve where our mod is stored
			File modFolder = Loader.instance().activeModContainer().getSource();
			String filePath = "com/bombstrike/cc/invmanager/lua/invmanager";

			if (modFolder.isFile()) {
				// we are inside a zip, we'll need to do crazy stuff
				ZipFile zipFile = new ZipFile(modFolder);
				ZipEntry entry = zipFile.getEntry(filePath);
				if (entry != null) {
					if (entry.getSize() != targetSize) {
						// retrieve the input stream
						InputStream input = zipFile.getInputStream(entry);
						
						// prepare target
						FileOutputStream output = new FileOutputStream(targetFile);
						
						byte buffer[] = new byte[input.available()];
						int read = input.read(buffer, 0, input.available());
						output.write(buffer, 0, read);
						
						// close everything
						output.close();
						input.close();
					}
				}
				
				zipFile.close();
			} else {
				// we are in a folder, just copy it
				File sourceFile = new File(modFolder.getPath() + File.separator + filePath);
				if (sourceFile.exists()) {
					if (sourceFile.length() != targetSize) {
						InventoryManager.logger.info("Updating " + targetFile.getCanonicalPath());

						FileInputStream input = new FileInputStream(sourceFile);
						FileOutputStream output = new FileOutputStream(targetFile);

						byte buffer[] = new byte[input.available()];
						int read = input.read(buffer, 0, input.available());
						output.write(buffer, 0, read);
						
						// close everything
						output.close();
						input.close();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void registerTileEntityRenderers() {
		
	}
	
	public void registerRenderInformation() {
		
	}
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		return null;
	}

}
