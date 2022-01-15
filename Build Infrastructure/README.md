In order to build Infrastructure dlls you need to 
1) checkout https://github.com/nasser/nostrand in this directory (read instructions in nostrand readme, (change target framework to net472 TODO, maybe optional check))

2) run `./nostrand/bin/x64/Release/net472/nos task/build` in current directory 

3) copy dlls from `nostrand/bin/x64/Release/net472` and `./Infrastructure` to `../Infrastructure`

4) In godot editor run scnene res://addons/ArcadiaGodot/scenes/arcadia_dev_menu.tscn and push button "Compile arcadia.internal.filesystem"
