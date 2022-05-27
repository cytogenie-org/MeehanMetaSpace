!#/bin/bash

pushd ../src/com
mkdir  classes
export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
javac -d classes  -classpath ../../lib/jnlp.jar:../../lib/MRJToolkit.jar:../../lib/AppleJavaExtensions.jar:../../lib/mmsBasics.jar:../lib/../jgoodies-common-0.9.9.jar:../../lib/jGoodiesLookAndFeel.jar:../../lib/commons-lang-2.0.jar:../../lib/quaqua.jar:../../lib/PgsLookAndFeel-1.1.1.jar:../../lib/commons-codec-1.10.jar:../../lib/swingx-all-1.6.4.jar MeehanMetaSpace/swing/*.java -source 1.7 -target 1.7
java -version
cd classes
jar cf ../../../lib/mmsSwing.jar com
cd ..
rm -r classes
popd
