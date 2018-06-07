#!/bin/bash

SRC=src
STANDARD=data/standard
VIZ=$PWD/viz

groovy $SRC/GenerateTableViz.groovy $STANDARD/all_2017.csv $VIZ/template_table.html $VIZ/table.html

echo "Ready."
