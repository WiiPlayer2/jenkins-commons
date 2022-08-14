isTriggeredByIndexing = currentBuild.getBuildCauses('jenkins.branch.BranchIndexingCause').size() > 0;
isTriggeredByPush = currentBuild.getBuildCauses('jenkins.branch.BranchEventCause').size() > 0;
isTriggeredByCommit = isTriggeredByPush; // Obsolete
isTriggeredByUser = currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause').size() > 0;
isTriggeredByCron = currentBuild.getBuildCauses('hudson.triggers.TimerTrigger$TimerTriggerCause').size() > 0;

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
