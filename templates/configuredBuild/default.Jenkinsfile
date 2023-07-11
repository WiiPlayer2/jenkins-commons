def runAll()
{
    def configuredBuild = load "ci/jenkins/configuredBuild.groovy";
    configuredBuild.runAll();
}

pipeline
{
    agent any

    stages { stage('All') { steps { script { runAll(); } } } }
}
