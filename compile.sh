#!/bin/sh
MCP=../
cp -r project/src-thx/client/* $MCP/src/minecraft/
cp -r project/src-thx/common/* $MCP/src/minecraft/
cp -r project/src-thx/server/* $MCP/src/minecraft_server/
cp -r project/src-thx/common/* $MCP/src/minecraft_server/
pushd $MCP
./recompile.sh
popd
