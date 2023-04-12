def run(stageName)
{
    def config = _loadConfiguration();
    _run(stageName, config);
}

def run()
{
    def config = _loadConfiguration();

    for(stageName in config.builder.getStages())
    {
        stage(stageName)
        {
            _run(stageName, config);
        }
    }
}

def _loadConfiguration()
{
    def configuration = readYaml file: "CI.yaml";
    def kv = configuration.entrySet().iterator().next(); // only support single configuration for now
    def configurationType = kv.key;
    def configurationData = kv.value;

    def builder = load "ci/jenkins/types/${configurationType}.groovy";

    return [
        type: configurationType,
        data: configurationData,
        builder: builder,
    ];
}

def _run(stageName, config)
{
    if(!config.builder.getStages().contains(stageName))
        return;

    echo "[$stageName] ${config.type}: ${config.data}";

    def preparedConfig = builder.createConfig();
    preparedConfig.putAll(config.data);

    config.builder.preStage(stageName);
    config.builder."$stageName"(preparedConfig);
    config.builder.postStage(stageName);
}

return this;
