def build(config)
{
    if(!config.containsKey('platforms') || config.platforms.size() == 0)
    {
        sh "docker build -t ${config.registry}/${config.imageName}:${config.tag} --pull -f ${config.dockerfile} ."
    }
    else
    {
        def buildInstance = sh (
            script: "docker buildx create",
            returnStdout: true
        ).trim();
        sh "docker buildx use $buildInstance"
        sh "docker buildx build -t ${config.registry}/${config.imageName}:${config.tag} --pull -f ${config.dockerfile} --platform ${config.platforms.join(',')} ."
    }
}

def publish(config)
{
    withDockerRegistry([credentialsId: config.registryCredentials, url: "https://${config.registry}/"])
    {
        if(!config.containsKey('platforms') || config.platforms.size() == 0)
        {
            def buildInstance = sh (
                script: "docker buildx create",
                returnStdout: true
            ).trim();
            sh "docker buildx use $buildInstance"
            sh "docker buildx build -t ${config.registry}/${config.imageName}:${config.tag} --pull -f ${config.dockerfile} --platform ${config.platforms.join(',')} --push ."
        }
        else
        {
            sh "docker image push ${config.registry}/${config.imageName}:${config.tag}"
        }
    }
}

return this;
