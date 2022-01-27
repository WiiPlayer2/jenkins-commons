def checkPullRequest()
{
    if(changeRequest())
    {
        stage('Check PR')
        {
            if(env.CHANGE_TARGET == 'main' && !(env.CHANGE_BRANCH ==~ /(release|hotfix)\/.+/))
            {
                error('Only release and hotifx branches are allowed.')
            }

            if(env.CHANGE_TARGET == 'dev' && !(env.CHANGE_BRANCH ==~ /(feature|bug|hotfix)\/.+/))
            {
                error('Only feature, bug and hotfix branches are allowed.')
            }
        }
    }
}

return this;
