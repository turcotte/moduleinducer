#!/bin/bash          
echo Start
swipl <<EOF
[aleph]. 
[ilpBackground.pl].
read_all(moduleInducer), induce, halt.
EOF
echo Done!
