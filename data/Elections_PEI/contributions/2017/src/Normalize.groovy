
@Grab('com.xlson.groovycsv:groovycsv:1.0')

import static com.xlson.groovycsv.CsvParser.parseCsv

import java.text.NumberFormat

final int INDEX_LAST_NAME = 0
final int INDEX_FIRST_NAME = 1
final int INDEX_CITY = 2
final int INDEX_TOTAL = 3

class Info {
    def party = ""
    def firstName = ""
    def lastName = ""
    def city = ""
    def total = 0
    def businessName = ""
    def province = ""

    static String q(def s) {
        def dq = '"'
        return "${dq}${s}${dq}"
    }

    String toString() {
        return "${q(party)},${q(lastName)},${q(firstName)},${q(businessName)},${q(city)},${q(province)},${q(total)}"
    }
}

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

def parseFile = { def party, def text, def infos  ->
    def results = []
    results.addAll(infos)

    try {
        def data = parseCsv text

        data.each { def line ->
            def firstName = line.getAt(INDEX_FIRST_NAME)
            def lastName = line.getAt(INDEX_LAST_NAME)
            def city = line.getAt(INDEX_CITY)
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

// --------- main

if (args.size() < 2) {
    println "Usage: groovy Normalize.groovy infile party"
    System.exit(-1)
}

def infile = args[0]
def party = args[1]

def isHeader = true
def header = ""
def lines = []

new File(infile).eachLine { line ->
    if (isHeader) {
        isHeader = false
        header = line
    } else {
        def text = "${header}\n${line}\n"
        lines = parseFile(party, text, lines)
    }
}

// println Info.getHeader()
lines.each { println it.toString() }
