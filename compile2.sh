#!/bin/sh

THE_CLASSPATH=
for i in `ls ./*.jar`
do
THE_CLASSPATH=${THE_CLASSPATH}:${i}
done

javac -d target/ -cp "${THE_CLASSPATH}" -sourcepath src/ src/main/java/com/mapr/bandit/BanditHittepa2.java
