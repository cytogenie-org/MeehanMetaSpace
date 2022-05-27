!#/bin/bash
pushd ../src/com
mkdir  classes
export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
javac -d classes  -classpath ../../lib/jnlp.jar:../../lib/MRJToolkit.jar:../../lib/AppleJavaExtensions.jar:../../lib/commons-lang-2.0.jar MeehanMetaSpace/*.java MeehanMetaSpace/monitor/*.java -source 1.7 -target 1.7
java -version
cd classes
jar cf ../../../lib/mmsBasics.jar com
cd ..
rm -r classes
popd
