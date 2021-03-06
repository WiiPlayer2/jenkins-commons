def buildLocalPlatform(config)
{
    sh "docker build -t ${config.fullTag} --pull -f ${config.dockerfile} ."
}

def buildMultiPlatform(config)
{
    sh "docker buildx create --use"
    sh """docker buildx build
            -t ${config.fullTag} \\
            --cache-from=type=local,src=${config.buildCache} \\
            --cache-to=type=local,dest=${config.buildCache},mode=max \\
            --pull \\
            -f ${config.dockerfile} \\
            --platform ${config.platforms.join(',')} \\
            ."""
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
        sh "docker buildx create --use"
        if(fileExists("${config.buildCache}/index.json")) {
            sh """docker buildx build \\
                    -t ${config.fullTag} \\
                    --cache-from=type=local,src=${config.buildCache} \\
                    --cache-to=type=local,dest=${config.buildCache} \\
                    --pull \\
                    -f ${config.dockerfile} \\
                    --platform ${config.platforms.join(',')} \\
                    --push \\
                    ."""
        } else {
            sh """docker buildx build \\
                    -t ${config.fullTag} \\
                    --cache-to=type=local,dest=${config.buildCache} \\
                    --pull \\
                    -f ${config.dockerfile} \\
                    --platform ${config.platforms.join(',')} \\
                    --push \\
                    ."""
        }
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

    return config;
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
