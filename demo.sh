#!/bin/bash

## call format: $> bash demo.sh

# defines the current directory
HERE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

## compiles the project to be tested and returns the full classpath
## for running tests.
function buildTarget(){
    SLUG=$1
    mkdir -p ${HERE}/downloads
    (cd ${HERE}/downloads;
     git clone --depth 1 $SLUG
     DIRNAME=$(echo $SLUG | rev | cut -f1 -d/ | cut -f2 -d\. | rev)
     cd $DIRNAME
     ## assuming this is maven!
     mvn_target=`pwd`
     ######  -Dmaven.repo.local=${PROJECT_DEPENDENCIES}  <-- need to fix this later. -Marcelo
     MVNOPTIONS="-Ddependency-check.skip=true -Dgpg.skip=true -DfailIfNoTests=false -Dskipinstallnodenpm -Dskip.npm -Dskip.yarn -Dlicense.skip -Dcheckstyle.skip -Drat.skip -Denforcer.skip -Danimal.sniffer.skip -Dmaven.javadocskip -Dfindbugs.skip -Dwarbucks.skip -Dmodernizer.skip -Dimpsort.skip -Dmdep.analyze.skip -Dpgpverify.skip -Dxml.skip -Dcobertura.skip=true -Dfindbugs.skip=true -Dsurefire.runOrder=reversealphabetical"     
     mvn $MVNOPTIONS clean compile test-compile
     app_classes=${mvn_target}/target/classes
     test_classes=${mvn_target}/target/test-classes
     temp_file=$(mktemp)
     mvn dependency:build-classpath -Dmdep.outputFile=$temp_file 
     ## return of the bash function
     ##echo ${app_classes}:${test_classes}:$(cat $temp_file)
     ## for debugging --->
     echo ${app_classes}:${test_classes}:$(cat $temp_file) > /tmp/target.classpath
    )
}

## this command stores the entire implementation classpath (obtained
## from gradle) in variable $CP with the goal of running the Main
## function in the command line.
INSTR_CP=$(./gradlew printCP | grep "Classpath =" | cut -f2 -d= | sed 's/^ *//g')

## clean-build the instrumentation project
./gradlew clean compileTestJava

####
#
# run instrumentation on an example project
#
####

# parameters
overhead='0.1'
minimal_delay='10'
csv_times='classes.csv' ## <- rename file
random_seed='43252'

buildTarget "git@github.com:alibaba/fastjson.git"

java -cp "${HERE}/build/classes/java/main:${INSTR_CP}" instr.MainDriver \
     --overHead ${overhead} \
     --minimalDelay ${minimal_delay} \
     --randomSeed ${random_seed}
