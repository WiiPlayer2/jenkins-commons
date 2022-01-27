def buildLocalPlatform(config)
{
    sh "docker build -t ${config.fullTag} --pull -f ${config.dockerfile} ."
}

def buildMultiPlatform(config)
{
    def buildInstance = sh (
        script: "docker buildx create",
        returnStdout: true
    ).trim();
    sh "docker buildx use $buildInstance"
    sh "docker buildx build -t ${config.fullTag} --cache-to=type=local,dest=${config.buildCache} --pull -f ${config.dockerfile} --platform ${config.platforms.join(',')} ."
}

def build(config)
{
    if(config.platforms.size() == 0)
    {
        buildLocalPlatform();
    }
    else
    {
        buildMultiPlatform();
    }
}

def publishLocalPlatform(config)
{
    withDockerRegistry([credentialsId: config.registryCredentials, url: "https://${config.registry}/"])
    {
        sh "docker image push ${config.fullTag}"
    }
}

def publishMultiPlatform(config)
{
    withDockerRegistry([credentialsId: config.registryCredentials, url: "https://${config.registry}/"])
    {
        def buildInstance = sh (
            script: "docker buildx create",
            returnStdout: true
        ).trim();
        sh "docker buildx use $buildInstance"
        sh "docker buildx build -t ${config.fullTag} --cache-from=type=local,src=${config.buildCache} --pull -f ${config.dockerfile} --platform ${config.platforms.join(',')} --push ."
    }
}

def publish(config)
{
    if(config.platforms.size() == 0)
    {
        publishLocalPlatform(config);
    }
    else
    {
        publishMultiPlatform(config);
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
        config.buildCache = './dockerBuildCache';
    }

    config.fullTag = "${config.registry}/${config.imageName}:${config.tag}";
}

def buildAndPublish(config)
{
    if(config.platforms.size() == 0)
    {
        stage('Build') {
            buildLocalPlatform(config);
        }

        stage('Publish') {
            publishLocalPlatform(config);
        }
    }
    else
    {
        stage('Build & Publish') {
            publishMultiPlatform(config);
        }
    }
}

return this;
