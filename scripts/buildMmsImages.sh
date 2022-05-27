!#/bin/bash

pushd ../src/com
mkdir classes
cd classes
mkdir com
cd com
mkdir MeehanMetaSpace
cd MeehanMetaSpace
mkdir swing
cd swing
mkdir images
cd ../../../
cp ../MeehanMetaSpace/swing/images/* com/MeehanMetaSpace/swing/images
jar cf ../../../lib/mmsImages.jar com
cd ..
rm -r classes
popd
