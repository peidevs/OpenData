
// --------- main

int numArgs = 1

if (args.size() < numArgs) {
    println "Usage: groovy RandomCheck.groovy file"
    System.exit(-1)
}

def file = args[0]

def lines = new File(file).getText() 

def numLines = lines.size()
def minRange = 10
def maxRange = numLines - 10
def randomIndex = 

println "TRACER: lines.get(randomIndex) 
