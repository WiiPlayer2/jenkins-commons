def doesMatch(config, causes)
{
    def result = true;

    if(config.containsKey('branch'))
    {
        result = result && (env.BRANCH_NAME =~ config.branch);
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

        if (data.containsKey("registry")) {
            return { b ->
                withDockerRegistry(credentialsId: data["registry"], url: name) {
                    b();
                }
            }
        }

        if (data.containsKey("sshKey")) {
            return { b ->
                withCredentials([sshUserPrivateKey(credentialsId: data["sshKey"], keyFileVariable: "${name}__key", passphraseVariable: "${name}__pass", usernameVariable: "${name}__user")]) {
                    b();
                }
            }
        }

        if (data.containsKey("text")) {
            return { b ->
                withCredentials([string(credentialsId: data["text"], variable: name)]) {
                    b();
                }
            }
        }

        if (data.containsKey("userPass")) {
            return { b ->
                withCredentials([usernamePassword(credentialsId: data["userPass"], passwordVariable: "${name}__pass", usernameVariable: "${name}__user")]) {
                    b();
                }
            }
        }
    }

    throw new Exception("Variable of type ${data.getClass()} not handled.");
}

def __checkVarsFilter(filter)
{
    if (filter.containsKey("or"))
    {
        def result = false;
        for (subFilter in filter.or)
        {
            result = result || __checkVarsFilter(subFilter);
        }
        return result;
    }

    if (filter.containsKey("and"))
    {
        def result = true;
        for (subFilter in filter.and)
        {
            result = result && __checkVarsFilter(subFilter);
        }
        return result;
    }

    if (filter.containsKey("not"))
    {
        return !__checkVarsFilter(filter.not);
    }

    if (filter.containsKey("branch"))
    {
        return env.BRANCH_NAME =~ filter.branch;
    }

    return true;
}

def __buildVars(data)
{
    def vars = data.vars;

    for (filterVars in data.filtered)
    {
        if(!__checkVarsFilter(filterVars.filter))
        {
            continue;
        }

        vars << filterVars.vars;
    }

    return vars;
}

def withVars(file = "pipeline.vars.yaml", body)
{
    def data = readYaml file: file;
    // echo "data: $data"
    def vars = __buildVars(data);

    def wrappers = [];
    for (var in mapToList(vars))
    {
        newBody = __withVar(var[0], var[1]);
        wrappers.add(newBody);
    }

    def wrapped = { -> body() };
    for (def i = 0; i < wrappers.size(); i++)
    {
        def currentIndex = i;
        def currentWrapped = wrapped;
        wrapped = { -> wrappers[currentIndex](currentWrapped) };
    }

    wrapped();
}

return this;
