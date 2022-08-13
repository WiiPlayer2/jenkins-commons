def doesMatch(matchConfig)
{
    def result = true;

    if(config.containsKey('branch'))
    {
        result = result && env.BRANCH_NAME ==~ config.branch
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
