#!/bin/sh
pushd ..
./reobfuscate.sh

pushd reobf/minecraft
cp -r ../../port-thx/project/resources/* .
zip -r ../../port-thx/mod_thx_helicopter-CLIENT-mc125-v018-fml-r1.zip .
popd

pushd reobf/minecraft_server
zip -r ../../port-thx/mod_thx_helicopter-SERVER-mc125-v018-fml-r1.zip .
popd

popd
