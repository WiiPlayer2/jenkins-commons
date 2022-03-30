isTriggeredByIndexing = currentBuild.getBuildCauses('jenkins.branch.BranchIndexingCause').size();
isTriggeredByCommit = currentBuild.getBuildCauses('com.cloudbees.jenkins.GitHubPushCause').size();
isTriggeredByUser = currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause').size();
isTriggeredByCron = currentBuild.getBuildCauses('hudson.triggers.TimerTrigger$TimerTriggerCause').size();

return this;
