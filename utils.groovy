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

@NonCPS
List<List<?>> mapToList(Map map) {
  return map.collect { it ->
    [it.key, it.value]
  }
}

def __withVar(name, data)
{
    if (data instanceof String) {
        return { b ->
            withEnv(["${name}=${data}"]) {
                b();
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
    wrappers = [];

    echo "data: $data"

    for (var in mapToList(data.vars))
    {
        newBody = __withVar(var[0], var[1]);
        wrappers.add(newBody);
    }

    wrapped = { b -> b() };
    for (def i = 0; i < wrappers.size(); i++)
    {
        wrapped = { b -> wrappers[0](b) };
    }

    wrapped(body);
}

return this;
