public def build(config)
{
    sh "docker build -t ${config.registry}/${config.imageName}:${config.tag} -f ${config.dockerfile} ."
}

public def publish(config)
{
    withDockerRegistry([credentialsId: config.registryCredentials, url: "https://${config.registry}/"])
    {
        sh "docker image push ${config.registry}/${config.imageName}:${config.tag}"
    }
}

return this;
