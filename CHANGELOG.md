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
