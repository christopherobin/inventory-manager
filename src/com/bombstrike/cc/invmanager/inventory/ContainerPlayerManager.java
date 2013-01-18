package com.bombstrike.cc.invmanager.inventory;

import com.bombstrike.cc.invmanager.tileentity.TileEntityPlayerManager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerPlayerManager extends Container {
	protected TileEntityPlayerManager entity;

	public ContainerPlayerManager(InventoryPlayer inventoryPlayer,
			TileEntityPlayerManager entity) {
		this.entity = entity;

		// fuel slots
		addSlotToContainer(new SlotCatalyst((IInventory) entity.getInventory(),
				0, 80, 17, false));
		
		// storage slots
		addSlotToContainer(new SlotCatalyst((IInventory) entity.getInventory(),
				1, 44, 17, true));
		addSlotToContainer(new SlotCatalyst((IInventory) entity.getInventory(),
				2, 44, 35, true));
		addSlotToContainer(new SlotCatalyst((IInventory) entity.getInventory(),
				3, 44, 53, true));

		// bind the player inventory to the interface
		bindPlayerInventory(inventoryPlayer);
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		// main inventory
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
						8 + j * 18, 84 + i * 18));
			}
		}

		// hot bar
		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return entity.isUseableByPlayer(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		ItemStack stack = null;
		Slot slotObject = (Slot) inventorySlots.get(slot);

		// null checks and checks if the item can be stacked (maxStackSize > 1)
		if (slotObject != null && slotObject.getHasStack()) {
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			// merges the item into player inventory since its in the tileEntity
			if (slot < 4) {
				if (!this.mergeItemStack(stackInSlot, 4, 40, true)) {
					return null;
				}
			}
			// places it into the tileEntity is possible since its in the player
			// inventory
			else if (!this.mergeItemStack(stackInSlot, 1, 4, false)) {
				return null;
			}

			if (stackInSlot.stackSize == 0) {
				slotObject.putStack(null);
			} else {
				slotObject.onSlotChanged();
			}

			if (stackInSlot.stackSize == stack.stackSize) {
				return null;
			}
			slotObject.onPickupFromSlot(player, stackInSlot);
		}
		return stack;
	}
	
	@Override
	protected boolean mergeItemStack(ItemStack stack, int from, int to, boolean reverse) {
        boolean result = false;
        int i = from;

        if (reverse)
        {
            i = to - 1;
        }

        Slot slot;
        ItemStack targetStack;

        if (stack.isStackable())
        {
            while (stack.stackSize > 0 && (!reverse && i < to || reverse && i >= from))
            {
                slot = (Slot)this.inventorySlots.get(i);
                targetStack = slot.getStack();

                if (targetStack != null && targetStack.itemID == stack.itemID && (!stack.getHasSubtypes() || stack.getItemDamage() == targetStack.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack, targetStack))
                {
                    int var9 = targetStack.stackSize + stack.stackSize;

                    if (var9 <= stack.getMaxStackSize())
                    {
                        stack.stackSize = 0;
                        targetStack.stackSize = var9;
                        slot.onSlotChanged();
                        result = true;
                    }
                    else if (targetStack.stackSize < stack.getMaxStackSize())
                    {
                        stack.stackSize -= stack.getMaxStackSize() - targetStack.stackSize;
                        targetStack.stackSize = stack.getMaxStackSize();
                        slot.onSlotChanged();
                        result = true;
                    }
                }

                if (reverse)
                {
                    --i;
                }
                else
                {
                    ++i;
                }
            }
        }

        if (stack.stackSize > 0)
        {
            if (reverse)
            {
                i = to - 1;
            }
            else
            {
                i = from;
            }

            while (!reverse && i < to || reverse && i >= from)
            {
                slot = (Slot)this.inventorySlots.get(i);
                targetStack = slot.getStack();

                if (targetStack == null && slot.isItemValid(stack))
                {
                    slot.putStack(stack.copy());
                    slot.onSlotChanged();
                    stack.stackSize = 0;
                    result = true;
                    break;
                }

                if (reverse)
                {
                    --i;
                }
                else
                {
                    ++i;
                }
            }
        }

        return result;
	}

}
