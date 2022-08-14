def doesMatch(config, causes)
{
    def result = true;

    if(config.containsKey('branch'))
    {
        result = result && (env.BRANCH_NAME ==~ config.branch);
    }

    if (config.containsKey('causes'))
    {
        result = result && !(config.causes.contains('push') && !causes.isTriggeredByPush);
        result = result && !(config.causes.contains('cron') && !causes.isTriggeredByCron);
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
