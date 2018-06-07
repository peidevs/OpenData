
def verify = { def allFile, def crossRefFile ->
    def allLines = []

    new File(allFile).eachLine { allLines << it }

    new File(crossRefFile).eachLine { def crossRefLine ->
        def result = allLines.find { it == crossRefLine }
        assert result
    }
}

// --------- main

int numArgs = 2

if (args.size() < numArgs) {
    println "Usage: check usage"
    System.exit(-1)
}

def allFile = args[0]
def crossRefFile = args[1]

verify(allFile, crossRefFile)

println "TRACER crossref verified!"
