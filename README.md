# Inventory Manager

Inventory manager is a mod for minecraft that adds a bunch of devices/peripherals for manipulating/fusing player and chest inventories.

## Player Manager

The player manager is a plate, connected to a computer on the side, and an inventory under it, that can detect players walking on it and manipulate their inventory.

![Recipe](http://i.imgur.com/eXjygCN.png)

## Automatic Player Manager

This version of the player manager doesn't connect to computers, but can still be used by BuildCraft pipes, RedPower Sorting Machines and other mods manipulating inventories.

![Recipe](http://i.imgur.com/yCMZUJr.png)

## API

Ok so there is the current state of the API, if you have any remark please feel free to make a comment:

### Methods:

`int size(string:name)`  
Returns the size of the inventory specified

`table:item read(string:name, int:slot)`  
Read the item at the specified slot, returning an item object

`table:status move(string:invFrom, int:slotFrom, string:invTo, int:slotTo = 0, int:amount = invFrom.STACK_SIZE)`  
Replaces the export and import methods (used in the youtube video), moves items from an inventory to another. slotTo and amount are optionnal and if the destination slot is full, it will try moving it anywhere else that has room.

### Types:

`string:name`  
Either one of __"player"__, __"north"__, __"east"__, __"south"__, __"west"__, __"up"__, __"down"__. The plate only accepts __"player"__ and __"down"__, the inventory manager accept anything but __"player"__.

`table:item`  
An item description, as it is in the stack, with the following fields:

+  `int:id` The ID of the item
+  `string:name` The raw name of the item in the MC engine
+  `string:display` The actual translated name of the item (for display purposes)
+  `int:amount` The amount of items in the stack
+  `int:damage` The damage of the item


`table:status`  
The status of the move command, contains all the fields of the item object, with the following fields:

+  `int:moved` The amount of items that were moved by the command
+  `int:left` The amount of items left (if the container was almost full and not all items were moved)

