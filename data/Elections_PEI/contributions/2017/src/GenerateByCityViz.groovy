
// --------- main

int numArgs = 3

if (args.size() < numArgs) {
    println "check usage"
    System.exit(-1)
}

def byCityFile = args[0]
def templateFile = args[1]
def destFile = args[2]

def newText = new File(byCityFile).getText()
def token = "ELECTIONS_PEI_DATA_ROWS"

new File(destFile).withWriter { writer ->
    new File(templateFile).eachLine { line ->
        def outLine = line
        if (line.trim() == token) {
            outLine = newText
        }
        writer.write(outLine + "\n")
    }
}
 

