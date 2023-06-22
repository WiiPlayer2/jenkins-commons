//--------------
// Core methods
//--------------
def metadata = [
    WrapStage = false,
    FixedStages = true,
];

def createConfig()
{
    return [:];
}

def getStages(config)
{
    return [
        "Build",
        "Publish",
    ];
}

def preStage(stageName) { }

def postStage(stageName) { }

//--------
// Stages
//--------
def Build(config) { }

def Publish(config) { }

//---------
// Helpers
//---------

return this;
