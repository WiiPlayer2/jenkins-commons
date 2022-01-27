def build(config)
{
    this.match(
        onLocalPlatform:
        {
            sh "docker build -t ${config.registry}/${config.imageName}:${config.tag} --pull -f ${config.dockerfile} ."
        },
        onMultiPlatform:
        {
            def buildInstance = sh (
                script: "docker buildx create",
                returnStdout: true
            ).trim();
            sh "docker buildx use $buildInstance"
            sh "docker buildx build -t ${config.registry}/${config.imageName}:${config.tag} --pull -f ${config.dockerfile} ."
        }
    )
}

def match(onLocalPlatform, onMultiPlatform)
{
    if(config.containsKey('platforms') && config.platforms.size() > 0)
    {
        onMultiPlatform();
    }
    else
    {
        onLocalPlatform();
    }
}

def publish(config)
{
    withDockerRegistry([credentialsId: config.registryCredentials, url: "https://${config.registry}/"])
    {
        this.match(
            onLocalPlatform:
            {
                def buildInstance = sh (
                    script: "docker buildx create",
                    returnStdout: true
                ).trim();
                sh "docker buildx use $buildInstance"
                sh "docker buildx build -t ${config.registry}/${config.imageName}:${config.tag} --pull -f ${config.dockerfile} --push ."
            },
            onMultiPlatform:
            {
                sh "docker image push ${config.registry}/${config.imageName}:${config.tag}"
            }
        );
    }
}

return this;
