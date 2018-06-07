#!/bin/bash

SRC=src
STANDARD=data/standard
VIZ=$PWD/viz

groovy $SRC/CrossRef.groovy $STANDARD/all_2017.csv 

echo "Ready."
