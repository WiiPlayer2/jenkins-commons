//--------------
// Core methods
//--------------
import groovy.transform.Field;

@Field def metadata = [
    WrapStage: true,
    FixedStages: false,
];

def createConfig()
{
    return [
        RunInImage: null,
        Stages: [],
    ];
}

def getStages(config)
{
    return config.Stages.collect { it.getKey() };
}

def wrapStage(stageName, config, body)
{
    if (config.RunInImage == null) {
        body();
    } else {
        docker.image(config.RunInImage).inside {
            body();
        }
    }
}

//--------
// Stages
//--------
def runStage(stageName, config)
{
    for (cmd in config.Stages.get(stageName).Commands)
    {
        sh cmd;
    }
}

//---------
// Helpers
//---------

return this;
