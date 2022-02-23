def useHubContainer(body)
{
    docker.image('unityci/hub:ubuntu-latest').inside
    {
        body();
    }
}

def getProjectEditorVersion(projectPath)
{
    def projectVersionFilePath = "${projectPath}/ProjectSettings/ProjectVersion.txt";
    def content = readYaml file: projectVersionFilePath;
    return content['m_EditorVersion'];
}

def installEditor(version, changeset = null, modules = [])
{
    if(modules == null)
    {
        modules = []
    }

    def cmd = "unity-hub install --version $version"

    if(changeset != null)
    {
        cmd += " --changeset $changeset";
    }

    if(modules.size() > 0)
    {
        cmd += " --modules " + modules.join(',');
    }

    sh cmd;
}

return this;
