INTRODUCTION
------------
If you just want to get the latest version of this prototype mod to try it out, you can download the zip file from GitHub:

    https://github.com/downloads/theoxylo/minecraft_mod_thx/mod_thx_helicopter_beta_1.7.3_v008.zip 

Just copy the zip file to your '.minecraft\mods' directory and start Minecraft normally (full Minecraft and ModLoader required).

After running at least once, check out the options in file '.minecraft\mods\mod_thx.options' for controls and other settings (see end of this file for annotated listing).

Please keep in mind that this is an early build prototype of a mod under construction. Even though no corruption issues have been reported, please back-up your worlds before using (always a good idea). SMP multi-player is not supported and has not been tested in any way. Please make sure that Minecraft and ModLoader are working properly before installing this mod. 

Please visit the forum thread to discuss, report issues, leave feedback, etc:

    http://www.minecraftforum.net/topic/246531-reqidea-helicopter-mod-gracefully-take-to-the-skies



DEVELOPMENT
-----------
Welcome to the Minecraft mod_thx Helicopter project! If you are still reading then we will assume you are a modder or would like to become one. Here's what you need to develop, build, test, and play:

1. Minecraft (full version): http://www.minecraft.net

2. ModLoader: http://www.minecraftforum.net/viewtopic.php?f=25&t=80246#p1223009

3. MCP: http://mcp.ocean-labs.de/index.php/Main_Page

4. Mod_thx source code and project files: git://github.com/theoxylo/minecraft_mod_thx.git

5. Java 6 SDK: http://www.oracle.com/technetwork/java/javase/downloads/jdk-6u25-download-346242.html

6. An IDE or good text editor. Eclipse is recommended: http://www.eclipse.org/downloads


ACKNOWLEDGEMENTS
----------------
Thanks to DannyBoy007 for managing the forum topic and recruiting.
Thanks to all forum participants.
Special thanks to the ModLoader and MCP projects, without which this project would not be possible! 
And of course, thanks to Notch and the Mojang team for the pure goodness that is Minecraft.


CHANGELOG
---------
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


ISSUES
------
Issue 1: helicopter sinks in water and can even fly under water -- it should float instead


OPTIONS AND SETTINGS
--------------------
# These are stored in .minecraft/mods/mod_thx.options
# please restart the game to apply changes

# The default controls are meant to utilize existing Minecraft defaults
# please customize to your liking

# helicopter cyclic (basic WASD move controls)
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

# toggles Heads Up Display (or camera) mode and briefly displays
# world corrdinates and altitude (measured in blocks)
key_hud_mode=L

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

# makes the missile and rockets very powerful for mining - use with caution!
enable_heavy_weapons=false

# hides the bottom panel of our beloved helicopter model to allow looking down
enable_look_down_trans=true

# use pilot look to set helicopter pitch - not fully tested, please report
enable_look_pitch=false

# use pilot look to set helicopter yaw/rotation/rudder/steering
enable_look_yaw=true

# use pilot look to aim missiles and rockets
enable_pilot_aim=true

# include a moving rotor with the model 
# rotor speed varies with throttle - the visual effect is highly
# dependent upon framerate, so experiment to find a pleasant result
# or disable if all else fails
enable_rotor=true
# set rotors relative speed
rotor_speed_percent=70

# work-around for Mo'Creatures ModLoader gui.png override bug
disable_helicopter_item_image=true

# toggle rear view
key_look_back=U
