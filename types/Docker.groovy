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
    return [
        Platforms: [],
    ];
}

def preStage(stageName) { }

def postStage(stageName) { }

//--------
// Stages
//--------
def Build(config) {
    def fullTag = __getFullTag(config);

    if(config.Platforms.size() == 0)
    {
        __buildLocalPlatform(config, fullTag);
    }
    else
    {
        __buildMultiPlatform(config, fullTag);
    }
}

def Publish(config) {
    def fullTag = __getFullTag(config);

    withDockerRegistry([credentialsId: config.RegistryCredentials, url: "https://${config.Registry}/"])
    {
        if(config.Platforms.size() == 0)
        {
            __publishLocalPlatform(config, fullTag);
        }
        else
        {
            __publishMultiPlatform(config, fullTag);
        }
    }
}

//---------
// Helpers
//---------
def __getFullTag(config) {
    return "${config.Registry}/${config.Repository}:${config.Tag}";
}

def __buildLocalPlatform(config, fullTag) {
    sh "docker build -t ${fullTag} --pull -f ${config.Dockerfile} .";
}

def __publishLocalPlatform(config, fullTag) {
    sh "docker image push ${fullTag}";
}

def __buildMultiPlatform(config, fullTag) {
    echo "When building a multiplatform image the build stage does nothing.";
}

def __publishMultiPlatform(config, fullTag) {
    sh "docker buildx create --name $BUILD_TAG --use";

    try
    {
        sh """docker buildx build \\
                -t ${fullTag} \\
                --pull \\
                -f ${config.Dockerfile} \\
                --platform ${config.Platforms.join(',')} \\
                --push \\
                .""";
    }
    finally
    {
        sh "docker buildx rm --force $BUILD_TAG";
    }
}

return this;
