// Private
def __withBuildX(config, body)
{
    if(config.resetBinaries)
    {
        sh "docker run --rm --privileged multiarch/qemu-user-static --reset -p yes"
    }

    sh "docker buildx create --name $BUILD_TAG --use"

    if(config.debug)
    {
        sh "docker buildx inspect --bootstrap"
    }

    try
    {
        body()
    }
    finally
    {
        sh "docker buildx rm --force $BUILD_TAG"
    }
}

// Public
def buildLocalPlatform(config)
{
    sh "docker build -t ${config.fullTag} --pull -f ${config.dockerfile} ."
}

def buildMultiPlatform(config)
{
    __withBuildX(config)
    {
        sh """docker buildx build
                -t ${config.fullTag} \\
                --cache-from=type=local,src=${config.buildCache} \\
                --cache-to=type=local,dest=${config.buildCache},mode=max \\
                --pull \\
                -f ${config.dockerfile} \\
                --platform ${config.platforms.join(',')} \\
                ."""
    }
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
        __withBuildX(config)
        {
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
    def project = [
        platforms: [],
        buildCache: './dockerBuildCache',
        resetBinaries: false,
        debug: false,
    ];
    project.putAll(config);

    project.fullTag = "${project.registry}/${project.imageName}:${project.tag}";

    return project;
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
