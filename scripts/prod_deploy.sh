#!/bin/bash

gradle -Pprod clean bootRepackage

scp -6 build/libs/expper-0.0.1-SNAPSHOT.war root@[2400:8900::f03c:91ff:fe67:b261]:/root/apps/expper
