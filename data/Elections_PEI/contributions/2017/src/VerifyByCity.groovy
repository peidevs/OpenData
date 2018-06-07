
@Grab('com.xlson.groovycsv:groovycsv:1.0')

import static com.xlson.groovycsv.CsvParser.parseCsv

import java.text.NumberFormat
import java.math.*

import java.math.BigDecimal
import java.text.DecimalFormat

final int INDEX_PARTY = 0
final int INDEX_FIRST_NAME = 1
final int INDEX_LAST_NAME = 2
final int INDEX_BUSINESS_NAME = 3
final int INDEX_CITY = 4
final int INDEX_PROVINCE = 5
final int INDEX_TOTAL = 6

class Info {
    def party = ""
    def firstName = ""
    def lastName = ""
    def businessName = ""
    def city = ""
    def province = ""
    def total = 0

    static String q(def s) {
        def dq = '"'
        return "${dq}${s}${dq}"
    }

    String toString() {
        return "${q(party)},${q(lastName)},${q(firstName)},${q(businessName)},${q(city)},${q(province)},${q(total)}"
    }
}

assert '"5150"' == Info.q('5150')
assert '""' == Info.q('')

def cleanTotal(def s) {
    def result = 0

    if (s) {
        def trimStr = s.replaceAll(/\$/, '').replaceAll('"', '').trim()
        result = NumberFormat.getInstance().parse(trimStr)
    }

    return result
}

assert 500 == cleanTotal('$500.00' as String)
assert 1500 == cleanTotal('"$1,500.00"' as String)

def parseText = { def text, def infos  ->
    def results = []
    results.addAll(infos)

    try {
        def data = parseCsv text

        data.each { def line ->
            def party = line.getAt(INDEX_PARTY)
            def firstName = line.getAt(INDEX_FIRST_NAME)
            def lastName = line.getAt(INDEX_LAST_NAME)
            def businessName = line.getAt(INDEX_BUSINESS_NAME)
            def city = line.getAt(INDEX_CITY)
            def province = line.getAt(INDEX_PROVINCE)
            def total = cleanTotal(line.getAt(INDEX_TOTAL))

            if (firstName || lastName || businessName) {
                def info = new Info(party: party, firstName: firstName, lastName: lastName,
                                    city: city, province: province, total: total)
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

def getAllInfos = { def allFile ->
    def infos = []

    def isHeader = true
    def header

    new File(allFile).eachLine { line ->
        if (isHeader) {
            isHeader = false
            header = line
        } else {
            def text = "${header}\n${line}\n"
            infos = parseText(text, infos)
        }
    }

    return infos
}

def getPartyInfos = { def partyFile, def header ->
    def infos = []

    new File(partyFile).eachLine { line ->
        def text = "${header}\n${line}\n"
        infos = parseText(text, infos)
    }

    return infos
}

def getTotal = { def infos, def party ->
    def partyInfos = infos.findAll { it.party == party }

    def total = partyInfos.inject(BigDecimal.ZERO) { value, item ->
        return value.add(new BigDecimal(item.total))
    }

    return total
}

def currencyFormat = { def n ->
    return NumberFormat.getCurrencyInstance().format(n)
}

// -- unit test
def testValA = getTotal([new Info(party: "Green", total: "41.40")], "Green")
def testValB = getTotal([new Info(party: "PC", total: "11.11"), new Info(party: "PC", total: "11.11"), new Info(party: "PC", total: "11.11")] , "PC")

assert '$41.40' == currencyFormat(testValA)
assert '$33.33' == currencyFormat(testValB)
// -- unit test

def findTotal = { def contribs ->
    def totalStr = "0"

    if (contribs != null && (! contribs.isEmpty())) {
        try {
            def total = contribs.inject(new BigDecimal(0), { result, item ->
                 result.add(new BigDecimal(item)) 
            })
            def format = new DecimalFormat("##.00")  
            totalStr = format.format(total)
        } catch(Exception ex) {
            System.err.println "TRACER caught exception: " + ex
        }
    }

    return totalStr
}

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

def findAllFileTotal = { def city, def allInfos ->
    def valInfos = allInfos.findAll { city == cleanCity(it.city) } 

    def vals = valInfos.collect { it.total } 
    def total  = findTotal(vals)

    return total 
}

def verify = { def allFile, def cityDataFile ->
    def allInfos = getAllInfos(allFile)

    new File(cityDataFile).eachLine { line ->
        def matcher = (line =~ /\["(.+)",(.+),(.+),(.+),(.+)\],/)  
        assert matcher.matches()

        def city = matcher[0][1]
        def val1 = matcher[0][2]
        def val2 = matcher[0][3]
        def val3 = matcher[0][4]
        def val4 = matcher[0][5]

        def cityDataTotal = findTotal([val1, val2, val3, val4]) 
        def allFileTotal = findAllFileTotal(city, allInfos)

        assert cityDataTotal == allFileTotal
    }
}

// --------- main

int numArgs = 2

if (args.size() < numArgs) {
    println "Usage: check usage"
    System.exit(-1)
}

def allFile = args[0]
def cityDataFile = args[1]

verify(allFile, cityDataFile)

println "TRACER bycity verified!"
