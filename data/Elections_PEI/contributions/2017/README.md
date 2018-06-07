
### Elections PEI data set: "lists of contributions in excess of $250 for year 2017"

* Original data is [here](http://www.electionspei.ca/index.php?number=1046908&lang=E)
    * extracted data is located [here](data/original/EPEI_2017) in this repository.
* This project "contains information licenced under the [Elections PEI Open Data Licence](http://www.gov.pe.ca/photos/original/EPEI_ODLICENCE.pdf)". 

### Code & Visualization

* [run.sh](run.sh) begins with the original data and ultimately generates the validation. Steps:
    * remove headers from original CSV files
    * normalize the data into a standard format
    * join CSV files into `all_2017.csv`
    * perform verification to double-check the amounts in the new file match the amounts in the original file, per party
    * generate [table.html](viz/table.html)
        * this is a list of all donations in excess of $250
    * generate [crossref.html](viz/crossref.html)
        * this is a list of donations in excess of $250 to multiple parties, where the names are similar
    * generate [bycity.html](viz/bycity.html)
        * this is a stepped-area chart that shows, for each city, the breakdown of party contributions
 
### License

* This repository is licensed under [Apache License 2.0](https://github.com/peidevs/OpenData/blob/master/LICENSE).
