//--------------
// Core methods
//--------------
def getStages()
{
    return [
        "Build",
    ];
}

def createConfig()
{
    return [:];
}

//--------
// Stages
//--------
def Build(config)
{
    echo "would have built nuget 🤷";
}

return this;
