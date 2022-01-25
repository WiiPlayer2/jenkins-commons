isTriggeredByIndexing = currentBuild.getBuildCauses('jenkins.branch.BranchIndexingCause').size();
isTriggeredByCommit = currentBuild.getBuildCauses('com.cloudbees.jenkins.GitHubPushCause').size();
isTriggeredByUser = currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause').size();

return this;
