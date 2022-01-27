def build(config)
{
    if(config.platforms.size() == 0)
    {
        sh "docker build -t ${config.fullTag} --pull -f ${config.dockerfile} ."
    }
    else
    {
        def buildInstance = sh (
            script: "docker buildx create",
            returnStdout: true
        ).trim();
        sh "docker buildx use $buildInstance"
        sh "docker buildx build -t ${config.fullTag} --cache-to=type=local,dest=${config.buildCache} --pull -f ${config.dockerfile} --platform ${config.platforms.join(',')} ."
    }
}

def publish(config)
{
    withDockerRegistry([credentialsId: config.registryCredentials, url: "https://${config.registry}/"])
    {
        if(config.platforms.size() == 0)
        {
            sh "docker image push ${config.fullTag}"
        }
        else
        {
            def buildInstance = sh (
                script: "docker buildx create",
                returnStdout: true
            ).trim();
            sh "docker buildx use $buildInstance"
            sh "docker buildx build -t ${config.fullTag} --cache-from=type=local,src=${config.buildCache} --pull -f ${config.dockerfile} --platform ${config.platforms.join(',')} --push ."
        }
    }
}

def prepare(config)
{
    if(!config.containsKey('platforms'))
    {
        config.platforms = [];
    }

    if(!config.containsKey('buildCache'))
    {
        config.buildCache = './buildCache';
    }

    config.fullTag = "${config.registry}/${config.imageName}:${config.tag}";
}

return this;
