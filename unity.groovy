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

def getProjectEditorChangeset(projectPath)
{
    def projectVersionFilePath = "${projectPath}/ProjectSettings/ProjectVersion.txt";
    def content = readYaml file: projectVersionFilePath;
    def matches = content['m_EditorVersionWithRevision'] =~ /\(([0-9a-f]{12})\)/;
    return matches[0][1]; // first capture group of first match
}

def invokeEditor(version, args)
{
    sh "/opt/unity/editors/$version/Editor/Unity -quit -batchmode -nographics $args"
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
