import org.jenkinsci.plugins.pipeline.modeldefinition.Utils;

def run(stageName)
{
    def buildConfigs = _loadConfiguration();
    _run(stageName, buildConfigs);
}

def runAll()
{
    def buildConfigs = _loadConfiguration();
    def stages = _gatherStages(buildConfigs);

    echo "Stages: $stages";

    for(stageName in stages)
    {
        stage(stageName)
        {
            _run(stageName, buildConfigs);
        }
    }
}

def _gatherStages(buildConfigs)
{
    return buildConfigs[0].builder.getStages(); // Just return the stages of the first config for now
}

def _loadConfiguration()
{
    def configuration = readYaml file: "CI.yaml";

    def builders = [:];
    def buildConfigs = [];

    for(buildConfig in configuration)
    {
        def kv = buildConfig.entrySet().iterator().next();
        def type = kv.key;
        def data = kv.value;

        if(!builders.containsKey(type))
            builders[type] = load "ci/jenkins/types/${type}.groovy";

        buildConfigs.add([
            type: type,
            data: data,
            builder: builders[type],
        ]);
    }

    return buildConfigs;
}

def _run(stageName, buildConfigs)
{
    for(buildConfig in buildConfigs)
        _runSingle(stageName, buildConfig);
}

def _runSingle(stageName, buildConfig)
{
    if(!buildConfig.builder.getStages().contains(stageName))
        return;

    echo "[$stageName] ${buildConfig.type}: ${buildConfig.data}";

    def preparedConfig = buildConfig.builder.createConfig();
    preparedConfig.putAll(buildConfig.data);

    buildConfig.builder.preStage(stageName);
    buildConfig.builder."$stageName"(preparedConfig);
    buildConfig.builder.postStage(stageName);
}

return this;
