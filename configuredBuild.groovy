def run(stageName)
{
    def configuration = readYaml file: "CI.yaml";
    def kv = configuration.entrySet().iterator().next(); // only support single configuration for now
    def configurationType = kv.key;
    def configurationData = kv.value;

    def builder = load "ci/jenkins/types/${configurationType}.groovy";
    if(!builder.stages.contains(stageName))
        return;

    echo "[$stageName] $configurationType: $configurationData";

    def preparedConfig = builder.defaultConfig.clone();
    preparedConfig.putAll(configurationData);

    builder."$stageName"(preparedConfig);
}

return this;
