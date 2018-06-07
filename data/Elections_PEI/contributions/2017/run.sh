#!/bin/bash

HEADER_CSV=$PWD/data/header.csv
SRC=src
ORIGINAL=data/original
FULL=data/full
TRIM=data/trim
STANDARD=data/standard
VIZ=$PWD/viz

# --------- copy to easier filenames

mkdir -p $FULL/tmp
rm -rf $FULL
mkdir $FULL

cp $ORIGINAL/EPEI_2017/2017\ Green\ Party\ of\ PEI-Table\ 1.csv  $FULL/green_2017.csv
cp $ORIGINAL/EPEI_2017/2017\ Liberal\ Party\ of\ PEI-Table\ 1.csv  $FULL/liberal_2017.csv
cp $ORIGINAL/EPEI_2017/2017\ NDP\ Party\ of\ PEI-Table\ 1.csv  $FULL/ndp_2017.csv
cp $ORIGINAL/EPEI_2017/2017\ PC\ Party\ of\ PEI-Table\ 1.csv  $FULL/pc_2017.csv

# --------- trim header and summary

mkdir -p $TRIM/tmp
rm -rf $TRIM
mkdir $TRIM

tail -n +7 $FULL/green_2017.csv > tmp.csv
grep -iv "total contributions" tmp.csv | grep -v ",,,\"" > $TRIM/green_2017.csv

tail -n +7 $FULL/liberal_2017.csv > tmp.csv
grep -iv "total contributions" tmp.csv | grep -v ",,,\"" > $TRIM/liberal_2017.csv

tail -n +7 $FULL/ndp_2017.csv > tmp.csv
grep -iv "total contributions" tmp.csv | grep -v ",,,\"" > $TRIM/ndp_2017.csv

tail -n +7 $FULL/pc_2017.csv > tmp.csv
grep -iv "total contributions" tmp.csv | grep -v ",,,\"" > $TRIM/pc_2017.csv

rm tmp.csv

# --------- normalize into data with no header

mkdir -p $STANDARD/tmp
rm -rf $STANDARD
mkdir $STANDARD

groovy $SRC/Normalize.groovy $TRIM/green_2017.csv Green > $STANDARD/green_2017.csv
groovy $SRC/Normalize.liberal.groovy $TRIM/liberal_2017.csv > $STANDARD/liberal_2017.csv
groovy $SRC/Normalize.groovy $TRIM/ndp_2017.csv NDP > $STANDARD/ndp_2017.csv
groovy $SRC/Normalize.groovy $TRIM/pc_2017.csv PC > $STANDARD/pc_2017.csv

# --------- join standardized, per-party files into 'all_2017.csv' (with header)

cd $STANDARD
cat $HEADER_CSV green_2017.csv liberal_2017.csv ndp_2017.csv pc_2017.csv > all_2017.csv
cd -

# --------- verify

groovy $SRC/Verify.groovy $STANDARD/all_2017.csv $STANDARD/green_2017.csv $HEADER_CSV Green 
groovy $SRC/Verify.groovy $STANDARD/all_2017.csv $STANDARD/liberal_2017.csv $HEADER_CSV Liberal 
groovy $SRC/Verify.groovy $STANDARD/all_2017.csv $STANDARD/ndp_2017.csv $HEADER_CSV NDP 
groovy $SRC/Verify.groovy $STANDARD/all_2017.csv $STANDARD/pc_2017.csv $HEADER_CSV PC 

# ---------- generate

groovy $SRC/GenerateTableViz.groovy $STANDARD/all_2017.csv $VIZ/template_table.html $VIZ/table.html

echo "Ready."
