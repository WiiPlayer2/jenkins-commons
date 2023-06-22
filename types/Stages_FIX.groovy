//--------------
// Core methods
//--------------
def getMetadata() {
    return [
        WrapStage: false,
        FixedStages: false,
    ];
}

def createConfig()
{
    return [:];
}

def getStages(config)
{
    return config.Stages;
}

def preStage(stageName) { }

def postStage(stageName) { }

//--------
// Stages
//--------
def runStage(stageName, config) { }

//---------
// Helpers
//---------

return this;
