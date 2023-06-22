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
    return [
        "Build",
        "Publish",
    ];
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
    sh "echo TODO"
}

//---------
// Helpers
//---------

return this;
