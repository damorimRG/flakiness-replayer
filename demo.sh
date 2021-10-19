#!/bin/bash

## $> bash demo.sh

# defines the current directory
HERE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

## compiles the project to be tested and returns the full classpath
## for running tests.
function buildTarget(){
    SLUG=$1
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
     echo ${app_classes}:${test_classes}:$(cat $temp_file)
    )
}

## this command stores the entire implementation classpath (obtained
## from gradle) in variable $CP with the goal of running the Main
## function in the command line.
INSTR_CP=$(./gradlew printCP | grep "Classpath =" | cut -f2 -d=)

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
csv_times='output.csv'
random_seed='43252'

TARGET_CP=$(buildTarget "git@github.com:alibaba/fastjson.git")
echo $TARGET_CP

exit
### TO COMPLETE

java -cp ${INSTR_CP}:${TARGET_DP} instr.MainDriver --overHead ${overhead} -minimalDelay ${minimal_delay} --csvTimes ${csv_times} --randomSeed ${random_seed}
