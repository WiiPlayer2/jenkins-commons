//--------------
// Core methods
//--------------
def getStages()
{
    return [
        "Build",
        "Publish",
    ];
}

def createConfig()
{
    return [:];
}

def preStage(stageName) { }

def postStage(stageName) { }

//--------
// Stages
//--------
def Build(config) {
}

def Publish(config) {
}

//---------
// Helpers
//---------
