2023-12-11 - 1.2.3
- Added a configurable option to allow taking outputs from the side of a delivery table. Off by default.

2023-11-10 - 1.2.2
- Added ability to keep item nbt-tags when opening Sealed Agreement. Can be configured.

2023-10-01 - 1.2.1
- Fixed Cardboard Box screen missing player's inventory slots in some cases.  
- Possible removal of some errors in logs.

2023-08-13 - 1.2.0
- Added 'TagMatching' field to the Requested Item. It allows specifying how NBT tags of items will be compared.
  - 'TagMatching:"ignore"' - tag does not matter
  - 'TagMatching:"weak"' - item is required to have defined tags (can have others)
  - 'TagMatching:"strong"' - item tag should be equal to the specified one
- Renamed 'DeliveriesRequirePackaging' to 'DeliveriesRequireBoxes' config value. Updated default values in several configs values comments.
- Fixed deliveries not delivering when 'Require Boxes' was disabled in the config.
- Fixed Zombie Packager not having a texture.

2023-07-23 - 1.1.0
- Tags can now be used as a requested item.
- Added sounds to Delivery Table opening and closing.
- When Delivery Agreement is completed - it will be placed into output slots (and therefore can be automatically extracted).
- Packages now show sender name in the item tooltip.
- Wandering Trader will now sell some Delivery Agreements.

- Fixed custom seal textures not working
