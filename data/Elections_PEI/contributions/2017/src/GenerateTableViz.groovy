
@Grab('com.xlson.groovycsv:groovycsv:1.0')

import static com.xlson.groovycsv.CsvParser.parseCsv

import java.text.NumberFormat
import java.math.*


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

def sq = { def s ->
    return "'" + s + "'"
}

assert "'5150'" == sq("5150")

def cleanSingleQuote = { def s ->
    return s.replaceAll(/\'/, /\\'/)
}

assert "O\\'Leary" == cleanSingleQuote("O'Leary")

def getColor = { def party ->
    def colorMap = ['Green' : 'green', 'Liberal' : 'red', 'NDP' : 'orange', 'PC' : 'blue']
    return colorMap[party]
}

def doTransform = { def infos ->
    def buffer = new StringBuilder()

    infos.each { def info ->
        info.with {
            def v, f

            v = total
            f = sq(currencyFormat(v))
            def totalStr = "{v: ${v}, f: ${f}}"

            v = party
            f = "'<div style=\"color: ${getColor(party)};\">${v}</div>'"
            partyStr = "{v: '${v}', f: ${f}}"

            def cleanFirstName = cleanSingleQuote(firstName)
            def cleanLastName = cleanSingleQuote(lastName)
            def cleanBusinessName = cleanSingleQuote(businessName)
            def cleanCity = cleanSingleQuote(city)

            buffer.append("[${partyStr},${sq(cleanLastName)},${sq(cleanFirstName)},${sq(cleanBusinessName)},${sq(cleanCity)},${sq(province)},${totalStr}],")
            buffer.append("\n")
        }
    }

    buffer.toString()
}

def generate = { def allFile, def templateFile, def destFile ->
    def token = "ELECTIONS_PEI_DATA_ROWS"
    def allInfos = getAllInfos(allFile)
    def transformedData = doTransform(allInfos)

    new File(destFile).withWriter { writer ->
        new File(templateFile).eachLine { line ->
            def outLine = line
            if (line.trim() == token) {
                outLine = transformedData
            }
            writer.write(outLine + "\n")
        }
    }
}

// --------- main

int numArgs = 3

if (args.size() < numArgs) {
    println "check usage"
    System.exit(-1)
}

def allFile = args[0]
def templateFile = args[1]
def destFile = args[2]

generate(allFile, templateFile, destFile)
