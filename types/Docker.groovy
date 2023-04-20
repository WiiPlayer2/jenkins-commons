//--------------
// Core methods
//--------------
def getStages()
{
    return [
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
def Publish(config) {
    def fullTag = "${config.Registry}/${config.Repository}:${config.Tag}"

    withDockerRegistry([credentialsId: config.RegistryCredentials, url: "https://${config.Registry}/"])
    {
        sh "docker buildx create --name $BUILD_TAG --use"

        try
        {
            sh """docker buildx build \\
                    -t ${fullTag} \\
                    --pull \\
                    -f ${config.Dockerfile} \\
                    --platform ${config.Platforms.join(',')} \\
                    --push \\
                    ."""
        }
        finally
        {
            sh "docker buildx rm --force $BUILD_TAG"
        }
    }
}

return this;
