
@Grab('com.xlson.groovycsv:groovycsv:1.0')

import java.math.BigDecimal
import java.text.DecimalFormat

import static com.xlson.groovycsv.CsvParser.parseCsv

final int INDEX_PARTY = 0 
final int INDEX_LAST_NAME = 1
final int INDEX_FIRST_NAME = 2
final int INDEX_BUSINESS_NAME = 3
final int INDEX_CITY = 4
final int INDEX_PROVINCE = 5
final int INDEX_TOTAL = 6

class Info {
    def party = ""
    def firstName = ""
    def lastName = ""
    def city = ""
    def total = 0

    static String q(def s) {
        def dq = '"'
        return "${dq}${s}${dq}"
    }

    static String getHeader() {
        return "${q("Party")},${q("Last Name")},${q("First Name")},${q("City")},${q("Total")}"
    }

    String toString() {
        return "${q(party)},${q(lastName)},${q(firstName)},${q(city)},${q(total)}"
    }
}

def cleanTotal = { total ->
    return total.replaceAll(/,/, "")
}
assert "1200.00" == cleanTotal("1,200.00")
assert "1200.00" == cleanTotal("1200.00")
assert "1200" == cleanTotal("1,200")
assert "1200" == cleanTotal("1200")

def cleanCity = { def city ->
    def result = city

    def m1 = (city =~ /(.*),.*/)

    if (m1.matches()) {
        result = m1[0][1]
    }

    def m2 = (city =~ /(.+) R.*R.*/)

    if (m2.matches()) {
        result = m2[0][1]
    }

    result = result.replaceAll(/\./, '')
    result = result.replaceAll(/\'/, '')
    result = result.replaceAll(/\s\s+/, ' ')
    result = result.replaceAll(/RR\s?#\d/, '')
    result = result.replaceAll(/PO.*/, '')
    result = result.trim()

    result
}
assert "Summerside" == cleanCity("Summerside, PE")
assert "Summerside" == cleanCity("Summerside RR 1")
assert "Summerside" == cleanCity("Summerside RR 1, PE")
assert "New Haven" == cleanCity("New  Haven")
assert "St Catherines" == cleanCity("St. Catherines")
assert "OLeary" == cleanCity("O'Leary")
assert "Cornwall" == cleanCity("RR #2 Cornwall")
assert "Cornwall" == cleanCity("RR#1 Cornwall")

def parseFile = { def text, def infos  ->
    def results = []
    results.addAll(infos)

    try {
        def data = parseCsv text

        data.each { def line ->
            def party = line.getAt(INDEX_PARTY)
            def firstName = line.getAt(INDEX_FIRST_NAME)
            def lastName = line.getAt(INDEX_LAST_NAME)
            def city = cleanCity(line.getAt(INDEX_CITY))
            def total = cleanTotal(line.getAt(INDEX_TOTAL))

            if (firstName || lastName) {
                def info = new Info(party: party, firstName: firstName, 
                                    lastName: lastName, city: city, total: total) 
                results << info
            } else {
                System.err.println "TRACER: skipping " + line
            }
        }
    } catch(ArrayIndexOutOfBoundsException ex) {
        System.err.println("TRACER caught ex : ${text}")
    }

    return results
}

def findTotal = { def contribs ->
    def totalStr = "0"

    if (contribs != null && (! contribs.isEmpty())) {
        try {
            def total = contribs.inject(new BigDecimal(0), { result, item ->
                 result.add(new BigDecimal(item.total)) 
            })
            def format = new DecimalFormat("##.00")  
            totalStr = format.format(total)
        } catch(Exception ex) {
            System.err.println "TRACER caught exception: " + ex
        }
    }

    return totalStr
}

def q = { def s ->
    def dq = '"'
    return "${dq}${s}${dq}"
}

def processCity = { def contribsForCity, def city ->
    def pcTotal = findTotal(contribsForCity.findAll { it.party == "PC" })
    def liberalTotal = findTotal(contribsForCity.findAll { it.party == "Liberal" })
    def ndpTotal = findTotal(contribsForCity.findAll { it.party == "NDP" })
    def greenTotal = findTotal(contribsForCity.findAll { it.party == "Green" })

    println "[${q(city)},${pcTotal},${liberalTotal},${ndpTotal},${greenTotal}],"
}

def processMap = { def map ->
    def keys = map.keySet().sort()

    keys.each { city ->
        processCity(map[city], city)
    }
}

// --------- main 

if (args.size() < 1) {
    println "Usage: check usage"
    System.exit(-1)
}

def infile = args[0]

def isHeader = true
def header = ""
def infos = []

new File(infile).eachLine { line ->
    if (isHeader) {
        header = line
        isHeader = false
    } else {
        def text = "${header}\n${line}\n"
        infos = parseFile(text, infos)
    } 
}

def map = infos.groupBy { it.city }
processMap(map)

