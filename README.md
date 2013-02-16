# Inventory Manager

Inventory manager is a mod for minecraft that adds a bunch of devices/peripherals for manipulating/fusing player and chest inventories.
More details on the original forum post: http://www.computercraft.info/forums2/index.php?/topic/8910-wip-cc-1481-mc-147-inventory-manager/

## Player Manager

The player manager is a plate, connected to a computer on the side, and an inventory under it, that can detect players walking on it and manipulate their inventory.

![Recipe](http://i.imgur.com/eXjygCN.png)

## Automatic Player Manager

This version of the player manager doesn't connect to computers, but can still be used by BuildCraft pipes, RedPower Sorting Machines and other mods manipulating inventories.

![Recipe](http://i.imgur.com/yCMZUJr.png)

## Inventory Manager

This block can connect to any inventory and pipe in 6 directions (beside the direction is connected from).

![Recipe](http://i.imgur.com/Ib6gbut.png)

## API

Ok so there is the current state of the API, if you have any remark please feel free to make a comment:

### Methods:

`size(direction)`  
Direction is a valid direction or inventory name, see below for a list of directions

`read(direction, slot)`  
Read the item at the specified slot, returning details about the stack in this slot. Slot IDs start from 1 to the inventory size.
See below for the details on the details table.

`move(directionFrom, directionTo, slotFrom = nil, slotTo = nil, amount = STACK_SIZE)`  
Moves a stack from the source inventory to a target inventory, with an optionally specified source and target slot, as well as a specified amount.
If slotFrom is not provided, finds the first item in the inventory and moves it.
If slotTo is not provided, finds the first available spot, if specified, try that spot first, then try the other slots.
Returns the same details as read() with an additional property moved that contains the amount of items moved by the command.

`send(directionFrom, directionTo, slotFrom = 1, amount = STACK_SIZE)`  
Send a stack into a pipe, allowing you to chose from which slot and how much to send.
If slotFrom is not provided, finds the first item in the inventory and moves it.
Returns the same details as read() with an additional property sent that contains the amount of items sent into the pipe.

`isInventory(direction)`  
Is the block at the given direction is a valid inventory?

`isPipe(direction)`  
Is the block at the given direction is a valid pipe?

### Types:

`direction`  
Either one of __"player"__, __"north"__, __"east"__, __"south"__, __"west"__, __"up"__, __"down"__. The plate only accepts __"player"__ and __"down"__, the inventory manager accept anything but __"player"__.

`item`  
A description of a stack, returned by _read()_

+  `id` The ID of the item
+  `name` The raw name of the item in the MC engine
+  `display` The actual translated name of the item (for display purposes)
+  `amount` The amount of items in the stack
+  `damage` The damage of the item

