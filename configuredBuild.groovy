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

def _metadataDefaults() {
    return [
        WrapStage: false,
        FixedStages: true,
    ];
}

def _gatherStages(buildConfigs)
{
    return buildConfigs[0].builder.b.getStages(buildConfigs[0].data); // Just return the stages of the first config for now
}

def _loadBuilder(type)
{
    def builder = load "ci/jenkins/types/${type}.groovy";

    def metadata = _metadataDefaults();
    metadata.putAll(builder.getMetadata() ?: [:]);

    return [
        b: builder,
        m: metadata,
    ];
}

def _loadConfiguration()
{
    def configuration = readYaml file: "CI.yaml";

    def builders = [:];
    def buildConfigs = [];

    for(buildConfig in configuration)
    {
        def iterator = buildConfig.entrySet().iterator();

        def kv = iterator.next();
        while (['Matcher', 'Skips'].contains(kv.key))
            kv = iterator.next();

        def type = kv.key;
        def data = kv.value;

        def doesMatch = buildConfig.containsKey('Matcher')
            ? _doesMatch(buildConfig['Matcher'])
            : true;

        if(!doesMatch)
            continue;

        def skips = buildConfig.containsKey('Skips')
            ? buildConfig.Skips
            : [];

        if(!builders.containsKey(type))
            builders[type] = _loadBuilder(type);

        buildConfigs.add([
            type: type,
            data: data,
            builder: builders[type],
            skips: skips,
        ]);
    }

    return buildConfigs;
}

def _doesMatch(matcher)
{
    if(matcher.containsKey('Branch') && !(env.BRANCH_NAME ==~ matcher.Branch))
        return false;

    if(matcher.containsKey('PullRequest') && (env.CHANGE_ID != null) != matcher.PullRequest)
        return false;

    return true;
}

def _run(stageName, buildConfigs)
{
    for(buildConfig in buildConfigs)
        _runSingle(stageName, buildConfig);
}

def _callStage(builder, stageName, config) {
    if (builder.m.FixedStages) {
        builder.b."$stageName"(config);
    } else {
        builder.b.runStage(stageName, config);
    }
}

def _runSingle(stageName, buildConfig)
{
    if(!buildConfig.builder.b.getStages(buildConfig.data).contains(stageName))
        return;

    echo "[$stageName] ${buildConfig.type}: ${buildConfig.data}";
    if (buildConfig.skips.contains(stageName)) {
        echo "SKIPPED"
        return;
    }

    def preparedConfig = buildConfig.builder.b.createConfig();
    preparedConfig.putAll(buildConfig.data);

    if (buildConfig.builder.m.WrapStage) {
        buildConfig.builder.b.wrapStage(stageName, preparedConfig, {
            _callStage(buildConfig.builder, stageName, preparedConfig);
        })
    } else {
        buildConfig.builder.b.preStage(stageName);
        _callStage(buildConfig.builder, stageName, preparedConfig);
        buildConfig.builder.b.postStage(stageName);
    }
}

return this;
