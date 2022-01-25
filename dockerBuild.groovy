
class Project
{
    String ImageName;
    String Tag;
    String Dockerfile;
    String Registry;
    String RegistryCredentials;

    Project(values)
    {
        this.ImageName = values.imageName;
        this.Tag = values.tag;
        this.Dockerfile = values.dockerfile;
        this.Registry = values.registry;
        this.RegistryCredentials = values.registryCredentials;
    }

    def build()
    {
        sh "docker build -t $Registry/$ImageName:$Tag -f $Dockerfile ."
    }

    def publish()
    {
        withDockerRegistry([credentialsId: RegistryCredentials, url: "https://$Registry/"])
        {
            sh "docker image push $Registry/$ImageName:$Tag"
        }
    }
}



return this;
