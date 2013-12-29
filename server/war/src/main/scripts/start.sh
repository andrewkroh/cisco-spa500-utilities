#!/bin/sh

cd $(dirname $0)

SCRIPT_HOME=$(pwd)

wildfly-8.0.0.CR1/bin/standalone.sh \
    -P ./phones.properties \
    -Dphone.config.dir=$SCRIPT_HOME/config \
    -Djboss.bind.address=0.0.0.0
