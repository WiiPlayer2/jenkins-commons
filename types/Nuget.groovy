//--------------
// Core methods
//--------------
def getMetadata() {
    return [
        WrapStage: true,
    ];
}

def getStages(config)
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
        BuildImage: null,
        PackageOutput: "./packages",
        VersionSuffix: "",
    ];
}

def preStage(stageName)
{
}

def postStage(stageName) { }

def wrapStage(stageName, config, body)
{
    env.HOME = '/tmp'
    env.DOTNET_CLI_HOME = '/tmp/DOTNET_CLI_HOME'

    if(config.BuildImage == null) {
        body();
    } else {
        docker.image(config.BuildImage) body;
    }
}

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
