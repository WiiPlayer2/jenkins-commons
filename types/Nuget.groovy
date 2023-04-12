//--------------
// Core methods
//--------------
def getStages()
{
    return [
        "Cleanup",
        "Build",
        "Test",
        "Pack",
        "Publish",
    ];
}

def createConfig()
{
    return [
        PackageOutput: "./packages",
        VersionSuffix: "",
    ];
}

def preStage(stageName)
{
    env.DOTNET_CLI_HOME = '/tmp/DOTNET_CLI_HOME'
}

def postStage(stageName) { }

//--------
// Stages
//--------
def Cleanup(config)
{
    sh "rm -r ${config.PackageOutput} || true"
    sh "dotnet clean ${config.Project} || true"
}

def Build(config)
{
    sh "dotnet build --configuration Release ${config.Project}"
}

def Test(config)
{
    sh "dotnet test --no-build --configuration Release ${config.Project}"
}

def Pack(config)
{
    def versionSuffix = config.VersionSuffix;
    if(versionSuffix == '$TIMESTAMP')
        versionSuffix = '$(date +%s)'

    sh "dotnet pack --no-build --configuration Release --output ${config.PackageOutput} --version-suffix \"${versionSuffix}\" ${config.Project}"
}

def Publish(config)
{
    withCredentials([usernamePassword(credentialsId: config.SourceCredentials, passwordVariable: 'apiKey', usernameVariable: 'source')])
    {
        sh "dotnet nuget push ${config.PackageOutput}/* --skip-duplicate --source $source --api-key $apiKey"
    }
}

return this;
