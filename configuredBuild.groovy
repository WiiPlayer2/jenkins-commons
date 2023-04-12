def run(stageName)
{
    def configuration = readYaml file: "CI.yaml";
    def kv = configuration.entrySet().iterator().next(); // only support single configuration for now
    def configurationType = kv.key;
    def configurationData = kv.value;

    echo "Using \"$configurationType\"..."
    def builder = load "ci/jenkins/${configurationType}.groovy";
    builder."$stageName"(configurationData);
}

return this;
