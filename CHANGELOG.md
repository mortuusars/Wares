# Changelog

## 1.2.8 - 2024-04-20
- Breaking Package while sneaking will now prevent it from breaking and dropping items. 
- Packages are now automation friendly:
  - Placing one down is no longer requires sneaking.
  - Dispensers can also place a Package now.
  - Pushing a Package with piston will break it and drop contained items.
  - All configurable.

## 1.2.7 - 2024-03-05
- Fixed crash when R-Clicking Agreement in Delivery Table. 

## 1.2.6 - 2024-03-03
- Fixed errors in logs when right-clicking an agreement.

## 1.2.5 - 2024-01-01
- NBT-tag will now match properly when RequestedItem's TagMatching is "weak".

## 1.2.4 - 2023-12-23
- Added `batch_delivered`, `agreement_completed` and `agreement_expired` advancement conditions. Details on the [Wiki](https://github.com/mortuusars/Wares/wiki)
  - They are meant for modpack developers and are not used in the mod itself.
- Added config option to show infinity symbol for infinite agreements. Disabled by default.
- Potentially fixed occasional config reset to default settings. 

## 1.2.3 - 2023-12-11
- Added an option to config that allow taking outputs from the side of a delivery table. Off by default.
- Added config option to disable moving Completed Agreement to the output slots.
- Added several settings to the Delivery Table NBT data for modpack developers. More details on the [Wiki](https://github.com/mortuusars/Wares/wiki/Delivery-Table)

## 1.2.2 - 2023-11-10
- Added ability to keep item nbt-tags when opening Sealed Agreement. Can be configured.

## 1.2.1 - 2023-10-01
- Fixed Cardboard Box screen missing player's inventory slots in some cases.  
- Possible removal of some errors in logs.

## 1.2.0 - 2023-08-13
- Added 'TagMatching' field to the Requested Item. It allows specifying how NBT tags of items will be compared.
  - 'TagMatching:"ignore"' - tag does not matter
  - 'TagMatching:"weak"' - item is required to have defined tags (can have others)
  - 'TagMatching:"strong"' - item tag should be equal to the specified one
- Renamed 'DeliveriesRequirePackaging' to 'DeliveriesRequireBoxes' config value. Updated default values in several configs values comments.
- Fixed deliveries not delivering when 'Require Boxes' was disabled in the config.
- Fixed Zombie Packager not having a texture.

## 1.1.0 - 2023-07-23
- Tags can now be used as a requested item.
- Added sounds to Delivery Table opening and closing.
- When Delivery Agreement is completed - it will be placed into output slots (and therefore can be automatically extracted).
- Packages now show sender name in the item tooltip.
- Wandering Trader will now sell some Delivery Agreements.

- Fixed custom seal textures not working

## 1.0.2 - 2023-07-17
- Fixed crash when ctrl+picking a package block.
- Fixed Items not dropping from a package that was picked from a block (with BlockEntityTag).

## 1.0.1 - 2023-07-12
- Fixed items getting deleted instead of dropping when closing Cardboard Box inventory without packing.

## 1.0.0 - 2023-04-18
- Release