isTriggeredByIndexing = currentBuild.getBuildCauses('jenkins.branch.BranchIndexingCause').size();
isTriggeredByPush = currentBuild.getBuildCauses('com.cloudbees.jenkins.GitHubPushCause').size(); // better name
isTriggeredByCommit = isTriggeredByPush; // Obsolete
isTriggeredByUser = currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause').size();
isTriggeredByCron = currentBuild.getBuildCauses('hudson.triggers.TimerTrigger$TimerTriggerCause').size();

return this;
