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

def __withVar(name, data)
{
    if (data instanceof String) {
        env[name] = data;
        return { b ->
            env[name] = data;
            try
            {
                b();
            }
            finally
            {
                env[name] = "";
            }
        };
    }

    if (data instanceof Map) {
        if (data.containsKey("file")) {
            return { b ->
                withCredentials([file(credentialsId: data["file"], variable: name)]) {
                    b();
                }
            }
        }
    }

    throw new Exception("Variable of type ${data.getClass()} not handled.");
}

def withVars(file = "pipeline.vars.yaml", body)
{
    data = readYaml file: file;

    currentWrapper = body;
    for (var in data.vars)
    {
        newBody = __withVar(var.key, var.value);
        currentWrapper = { -> newBody(currentWrapper); };
    }

    currentWrapper();
}

return this;
