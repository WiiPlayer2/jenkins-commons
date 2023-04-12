def run(stageName)
{
    def configuration = readYaml file: "CI.yaml";
    def kv = mapToList(configuration)[0]; // only support single configuration for now
    def configurationType = kv[0];
    def configurationData = kv[1];

    echo "Using \"$configurationType\"..."
    def builder = load "ci/jenkins/${configurationType}.groovy";
    builder."$stageName"(configurationData);
}

return this;
