def causes = load "buildCauses.groovy"

def doesMatch(matchConfig)
{
    def result = true;

    if(config.containsKey('branch'))
    {
        result = result && env.BRANCH_NAME ==~ config.branch
    }

    if (config.containsKey('causes'))
    {
        result = result && (config.causes.contains('push') || causes.isTriggeredByPush);
        result = result && (config.causes.contains('cron') || causes.isTriggeredByCron);
    }

    return result;
}

def getMatchedConfig(configs)
{
    for (config in configs)
    {
        if (doesMatch(config))
        {
            return config.config;
        }
    }

    throw "No config match found."
}

return this;
