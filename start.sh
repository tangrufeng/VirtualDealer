#!/bin/sh
export VD_HOME=$(pwd)
export PATH=$JAVA_HOME/jre/bin:$PATH
export CLASSPATH=$JAVA_HOME/jre/lib:$VD_HOME/lib:$CLASSPATH
# add all the jar
for loop in `ls $VD_HOME/lib/*.jar`;do
export CLASSPATH=${loop}:${CLASSPATH}
done
nohup java -cp $CLASSPATH:vd1.3.jar org.tom.vd.Main &
