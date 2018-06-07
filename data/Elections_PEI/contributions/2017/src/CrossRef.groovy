
@Grab('com.xlson.groovycsv:groovycsv:1.0')

import static com.xlson.groovycsv.CsvParser.parseCsv

final int INDEX_PARTY = 0 
final int INDEX_FIRST_NAME = 1
final int INDEX_LAST_NAME = 2
final int INDEX_CITY = 3
final int INDEX_TOTAL = 4

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

def parseFile = { def text, def infos  ->
    def results = []
    results.addAll(infos)

    try {
        def data = parseCsv text

        data.each { def line ->
            def party = line.getAt(INDEX_PARTY)
            def firstName = line.getAt(INDEX_FIRST_NAME)
            def lastName = line.getAt(INDEX_LAST_NAME)
            def city = line.getAt(INDEX_CITY)
            def total = line.getAt(INDEX_TOTAL)

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

def clean = { def s ->
    return s.trim().replaceAll(" ", "").toLowerCase()
}

assert "abc" == clean("A B C")

def asList = { def s ->
    def result = []
    s.each { result << it }
    return result.sort()
}

assert ['a', 'a', 'b', 'c']  == asList("abca")

def compare = { def s, t ->
    def cleanS = clean(s)
    def cleanT = clean(t)
    def listS = asList(cleanS)
    def listT = asList(cleanT)

    def result = (listS == listT)

    return result 
}

assert true == compare("a b c", "C B A")
assert true == compare("cox & palmer", "palmer cox &")
assert true == compare('ElectricMaritime', 'Maritime Electric') 

def processPair = { def map, def party, def otherParty ->
    def partyInfos = map[party]
    def otherPartyInfos = map[otherParty]
    partyInfos.each { partyInfo ->
        otherPartyInfos.each { otherPartyInfo ->
            def partyName = partyInfo.firstName + partyInfo.lastName
            def otherPartyName = otherPartyInfo.firstName + otherPartyInfo.lastName
            def isMatch = compare(partyName, otherPartyName) 

            if (isMatch) {
                println "contrib 1: " + partyInfo.toString()
                println "contrib 2: " + otherPartyInfo.toString()
                println "----------"
            }
        }
    }
}

def processMap = { def map ->
    def parties = map.keySet()
    def alreadyProcessed = [:].withDefault { false }

    parties.each { def party ->
        def otherParties = parties.minus(party)
        otherParties.each { def otherParty ->
            def pairKey = ""
            [party, otherParty].sort().each { pairKey += it }
            if (! alreadyProcessed[pairKey]) {
                processPair(map, party, otherParty)
                alreadyProcessed[pairKey] = true
            }
        }
    }
}

// --------- main 

if (args.size() < 1) {
    println "Usage: groovy Viewer.groovy infile"
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

def map = infos.groupBy { it.party }
processMap(map)


