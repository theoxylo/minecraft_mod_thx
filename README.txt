Minecraft Helicopter Mod, v014, 20120104



INTRODUCTION
------------
This version is v014, for use with Minecraft and ModLoader 1.0.0, single-player only.

You can download the compiled binary zip file from GitHub:


    https://github.com/downloads/theoxylo/minecraft_mod_thx/mod_thx_helicopter_1.0.0_v014-bin.zip


Just copy the zip file to your '.minecraft/mods' directory and start Minecraft normally (full Minecraft and ModLoader required).

After running at least once, check out the options in file '.minecraft/mods/mod_thx.options' for controls and other settings (see end of this file for annotated listing).

Please keep in mind that this is an early release of a mod still under construction. Even though no corruption issues have been reported, please back-up your worlds before using (always a good idea). SMP multi-player is not supported and has not been tested in any way. Please make sure that Minecraft and ModLoader are working properly before installing this mod -- see log ouput in .minecraft/ModLoader.txt

Please visit the forum thread to discuss, report issues, leave feedback, etc:

    http://www.minecraftforum.net/index.php?showtopic=763209



CRAFTING
---------
The current crafting recipe is very cheap and simple -- build a boat and add a roof (rotor assembly)!

XPX
PxP
PPP

Where X is nothing and P is wooden planks.



SKINS
-----
If you would like to change the appearance of the helicopter, you can modify or replace the file 'helicopter.png'. Please post your designs and image files on the forum topic!


DEVELOPMENT
-----------
Welcome to the Minecraft mod_thx Helicopter project! If you are still reading then we will assume you are a modder or would like to become one. Here's what you need to develop, build, test, and play:

1. Minecraft (full version): http://www.minecraft.net

2. ModLoader: http://www.minecraftforum.net/index.php?showtopic=75440

3. MCP: http://mcp.ocean-labs.de/index.php/Main_Page

4. Mod_thx source code and project files: https://github.com/theoxylo/minecraft_mod_thx

5. Java 6 SDK: http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-javase6-419409.html

6. An IDE or good text editor. Eclipse is recommended: http://www.eclipse.org/downloads



ACKNOWLEDGEMENTS
----------------
Thanks to vip3r for the helicopter skin.
Thanks to dannyoboy007 for managing the forum topic and recruiting.
Thanks to all forum participants.

Special thanks to the ModLoader and MCP projects, without which this project would not be possible! 

And of course, thanks to Notch and the Mojang team for the pure goodness that is Minecraft.



LICENSE & USAGE
---------------
The compiled mod is free software and comes without restrictions or guarantees. 

The source code is freely available, and attribution is appreciated if used or adapted (please include a link to either this topic or the GitHub project).



VIDEOS
------
Official YouTube playlist:

    http://www.youtube.com/playlist?list=PLC95BDA16181F6120



CHANGELOG
---------
v014: still for use with Minecraft and ModLoader 1.0.0
v014: added altitude lock as suggested by Gabbiman (default key P)
v014: added HUD/camera mode where model is hidden (default key C)
v014: added windshield to model with transparancy effects
v014: added helicopter skin improved by vip3r

v013: still for use with Minecraft and ModLoader 1.0.0
v013: fixed NPE crash on AI fire weapon

v012: for use with Minecraft and ModLoader 1.0.0
v012: enabled separate textures for all model faces
v012: replaced hud mode with in-game look-pitch control (key L)
v012: changed 3rd-person weapon aim to vehicle based
v012: added logic to separate rear-view control (hold Y key)
v012: now preserving view mode when entering/exiting
v012: extended auto-level function to look-pitch mode (key K)
v012: removed disappearing floor logic
v012: removed heavy weapons option

v011: for use with Minecraft and ModLoader beta 1.8.1
v011: enhanced model with "hi-rez" texture panel support
v011: restored right-click exit function
v011: fixed judder when auto-level turned off
v011: adjusted on-demand auto-level, applies to roll also (K key by default)

v010: for use with Minecraft and ModLoader beta 1.8.1
v010: now using DamageSource for rocket attack owner check
v010: removed explosion from rocket strike, added flame marker
v010: increased rocket entity damage, crash damage, max health
v010: removed helicopter death particle spawn and increased explosion
v010: removed right-click exit, use 'Y' key by default
v010: added map creation function, key 'O' by default

v009: for use with Minecraft and ModLoader beta 1.7.3
v009: adjusted attack and health values
v009: adjusted collision and damage threshold
v009: added follow/attack AI (attack empty helicopter to engage)
v009: added smoke to missile flight

v008: built for Minecraft and ModLoader beta 1.7.3
v008: updated README.txt :)

v007: now updating helicopter rotation rendering at 60 fps
v007: added rear view and 'key_look_back' option, U key by default
v007: some tuning to look-pitch mode
v007: rockets now damage trees and snow
v007: added 'disable_helicopter_item_image' option for Mo'Createures compatibility

v006: still on Minecraft and ModLoader beta 1.6.6
v006: added tail rotor
v006: added water check and float
v006: added blue camo texture (customize by replacing helicopter.png)
v006: fixed rendering delta time bug (backward rotor)

v005: now on Minecraft and ModLoader beta 1.6.6
v005: using delta time for smoother entity updates
v005: improved missile movement and collision, increased damage
v005: added heavy mining weapon mode - power boost to projectiles
v005: increased render distance of projectiles
v005: variable rotor speed linked to throttle
v005: increased number of rockets to 12 per reload
v005: added drone shutdown (exit) ability - Y
v005: change descend key default to match sneak - LSHIFT
v005: added HUD targeting/camera mode - toggle key L by default
v005: added auto pitch level - single activate key K by default
v005: improved helicopter entry - pilot yaw adjusted to helicopter
v005: improved helicopter exit - now to left of helicopter
v005: fixed helicopter inventory item right click
v005: fixed self collision with missiles and rockets
v005: fixed texture mapping to missile model
v005: fixed velocity effects from firing rockets

v004: now on Minecraft beta 1.6.5 and ModLoader v6
v004: fixed pitch-up rendering
v004: added pitch-down  transparent panel
v004: rockets, default R to fire
v004: enabled vehicle damage
v004: added collision sound
v004: changed helicopter placement on item usage
v004: improved missile flight and detonation
v004: fixed bogus fall damage to pilot
v004: change vehicle exit
v004: made helicopter item stackable to 16
v004: now using delta time for variable framerates

v003: now using ModLoader v4 for Minecraft 1.5_01
v003: added rotor spin up/down
v003: added model windshield
v003: changed to pilot aim default
v003: fixed 'wrong location' startup error
v003: added flag for auto-level
v003: added rotor speed property



OPTIONS AND SETTINGS
--------------------
# These are stored in .minecraft/mods/mod_thx.options
# please restart the game to apply changes

# The default controls are meant to utilize existing Minecraft defaults
# please customize to your liking

# helicopter cyclic (basic WASD move controls)
# these move the helicopter by tilting it, except when look-pitch
# is active in which case W/S control the throttle
key_forward=W
key_left=A
key_back=S
key_right=D

# helicopter collective (throttle, up/down, jump/sneak)
key_ascend=SPACE
key_descend=LSHIFT

# helicopter rudder (rotate/turn right/left) only useful when
# enable_drone_mode=true OR enable_look_yaw=false - see below
key_rotate_right=H
key_rotate_left=G

# toggles look-pitch on/off and briefly displays damage level,
# world corrdinates and altitude (measured in blocks)
key_look_pitch=L

# exits the helicopter 1.9 blocks to the left - use with caution while flying!
# also used to cut power when in drone (RC) mode
key_exit=Y

# fires a powerful missile that will take out a few stone blocks and up to
# 27 weaker blocks - useful for mining, landscaping, combat, hunting
key_fire_missile=M

# fires a weak but fast rocket, hold for burst up to 12 before reloading
key_fire_rocket=R

# quickly returns pitch to zero - useful with full throttle to avoid crash
key_auto_level=K

# always allow helicopter to naturally return to zero pitch automatically
# seting this to false engages an auto-pilot mechanism to hold pitch constant
enable_auto_level=true

# returns throttle to zero when not actively ascending or descending
# if you disable, your rotor speed will indicate the current throttle
enable_auto_throttle_zero=true

# puts the helicopter into RC remote-control/drone mode - make sure to map
# rudder/rotate controls since pilot look will not control pitch or yaw
enable_drone_mode=false

# use pilot look to set helicopter yaw/rotation/rudder/steering
enable_look_yaw=true

# include a moving rotor with the model 
# rotor speed varies with throttle - the visual effect is highly
# dependent upon framerate, so experiment to find a pleasant result
# or disable if all else fails
enable_rotor=true
# set rotors relative speed
rotor_speed_percent=70

# work-around for Mo'Creatures ModLoader gui.png override bug
disable_helicopter_item_image=true

# hold for rear view
key_look_back=U

# creat map for current location, will show previous data if available
key_create_map=O

# keep helicopter at current altitude, press again to unlock
key_lock_alt=P

# switch to hud/camera mode where model is hidden
key_hud_mode=C



GIT CHEATSHEET
--------------
To create a new branch:
cd workspace_git_mod_thx
git checkout -b vXXX_candidate
git push origin vXXX_candidate

To make changes:
vi README.txt
vi ./project/src-thx/net/minecraft/src/mod_Thx.java
git commit -a -m "my changes"
git push origin

To merge a branch:
git checkout master
git merge vXXX_candidate
git push origin

To create a tag:
git tag mod_Thx-beta_YYY_vXXX
git push origin --tags

To delete a branch, local and remote:
git branch -d vXXX_candidate
git push origin :vXXX_candidate
