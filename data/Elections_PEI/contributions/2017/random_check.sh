#!/bin/bash

echo "-----------------------------------"
echo "random check: Cameron"

grep -i Cameron data/standard/all_2017.csv 
echo "" 
grep -i Cameron data/original/EPEI_2017/*.csv
echo "" 

echo "-----------------------------------"
echo "random check: Roche"

grep -i Roche data/standard/all_2017.csv 
echo "" 
grep -i Roche data/original/EPEI_2017/*.csv
echo "" 

echo "-----------------------------------"
echo "random check: Broderick"

grep -i Broderick data/standard/all_2017.csv 
echo "" 
grep -i Broderick data/original/EPEI_2017/*.csv
echo "" 

echo "-----------------------------------"
echo "random check: Docherty"

grep -i Docherty data/standard/all_2017.csv 
echo "" 
grep -i Docherty data/original/EPEI_2017/*.csv
echo "" 

echo "Ready."
