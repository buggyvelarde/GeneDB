#!/bin/bash

SCRIPT_DIRECTORY=`dirname $(readlink -f $0)`
echo SCRIPT_DIRECTORY $SCRIPT_DIRECTORY

export PATH=/software/pathogen/external/applications/groovy/groovy/bin:$PATH
sh $SCRIPT_DIRECTORY/index.sh
