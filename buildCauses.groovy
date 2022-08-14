isTriggeredByIndexing = currentBuild.getBuildCauses('jenkins.branch.BranchIndexingCause').size();
isTriggeredByPush = currentBuild.getBuildCauses('com.cloudbees.jenkins.GitHubPushCause').size(); // better name
isTriggeredByCommit = isTriggeredByPush; // Obsolete
isTriggeredByUser = currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause').size();
isTriggeredByCron = currentBuild.getBuildCauses('hudson.triggers.TimerTrigger$TimerTriggerCause').size();

def selectManually()
{
    def inputSelection = input message: 'Select emulated trigger cause', parameters: [choice(choices: ['User', 'Push', 'Cron'], name: 'Cause')]
    switch(inputSelection)
    {
        case "Push":
            isTriggeredByPush = true;
            break;

        case "Cron":
            isTriggeredByCron = true;
            break;
    }
}

return this;
