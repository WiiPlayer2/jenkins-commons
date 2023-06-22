import org.jenkinsci.plugins.pipeline.modeldefinition.Utils;

_defaultMetadata = [
    WrapStage: false,
    FixedStages: true,
];

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

def _loadBuilder(type)
{
    def builder = load "ci/jenkins/types/${type}.groovy";

    def metadata = _defaultMetadata.clone();
    metadata.putAll(builder.metadata ?: [:]);
    builder.metadata = metadata;

    return builder;
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
            builders[type] = _loadBuilder(type);

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

def _callStage(builder, stageName, config) {
    if (builder.metadata.FixedStages) {
        builder."$stageName"(config);
    } else {
        builder.runStage(stageName, config);
    }
}

def _runSingle(stageName, buildConfig)
{
    if(!buildConfig.builder.getStages().contains(stageName))
        return;

    echo "[$stageName] ${buildConfig.type}: ${buildConfig.data}";

    def preparedConfig = buildConfig.builder.createConfig();
    preparedConfig.putAll(buildConfig.data);

    if (buildConfig.builder.metadata.CanWrapStage) {
        buildConfig.builder.wrapStage(stageName, preparedConfig, {
            _callStage(buildConfig.builder, stageName, preparedConfig);
        })
    } else {
        buildConfig.builder.preStage(stageName);
        _callStage(buildConfig.builder, stageName, preparedConfig);
        buildConfig.builder.postStage(stageName);
    }
}

return this;
