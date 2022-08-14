def doesMatch(config, causes)
{
    def result = true;

    if(config.containsKey('branch'))
    {
        result = result && (env.BRANCH_NAME ==~ config.branch);
    }

    if (config.containsKey('causes'))
    {
        echo "Checking causes; result: ${result}; causes: ${config.causes}; isPush: ${causes.isTriggeredByPush}; isCron: ${causes.isTriggeredByCron}"
        result = result && !(config.causes.contains('push') && !causes.isTriggeredByPush);
        echo "result: ${result}"
        result = result && !(config.causes.contains('cron') && !causes.isTriggeredByCron);
        echo "result: ${result}"
    }

    return result;
}

def getMatchedConfig(configs, causes)
{
    for (config in configs)
    {
        if (doesMatch(config.match, causes))
        {
            return config.config;
        }
    }

    throw "No config match found."
}

return this;
