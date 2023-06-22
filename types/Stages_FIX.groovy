//--------------
// Core methods
//--------------
import groovy.transform.Field;

@Field def metadata = [
    WrapStage: false,
    FixedStages: true,
];

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

def Build(config) { }

//---------
// Helpers
//---------

return this;
