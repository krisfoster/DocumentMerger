#!/usr/bin/env bash
mvn install:install-file -Dfile=./libs/idcupgrade.jar -DgroupId=oracle \
    -DartifactId=idcupgrade -Dversion=11.1.1.8 -Dpackaging=jar

mvn install:install-file -Dfile=libs/oracle.ucm.ridc-11.1.1.jar -DgroupId=oracle \
    -DartifactId=ridc -Dversion=11.1.1.8 -Dpackaging=jar
