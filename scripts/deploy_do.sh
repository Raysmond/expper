#!/bin/bash

gradle -Pprod bootRepackage

scp -6 build/libs/expper-0.0.1-SNAPSHOT.jar root@[2400:6180:0:d0::5f:d001%eth0]:/root/expper
