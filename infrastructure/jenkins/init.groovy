import jenkins.model.Jenkins
import jenkins.model.JenkinsLocationConfiguration
import hudson.security.HudsonPrivateSecurityRealm
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import hudson.slaves.DumbSlave
import hudson.slaves.JNLPLauncher
import hudson.slaves.RetentionStrategy
import hudson.model.Node
import hudson.plugins.git.GitSCM
import hudson.plugins.git.UserRemoteConfig
import hudson.plugins.git.BranchSpec
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey

def instance = Jenkins.get()
def readSecret = { name -> new File('/run/secrets/' + name).text.trim() }
def adminPassword = readSecret('jenkins_admin_password')
if (adminPassword.length() < 20) throw new IllegalStateException('Admin password is missing or too short')
if (!(instance.securityRealm instanceof HudsonPrivateSecurityRealm)) {
    instance.setSecurityRealm(new HudsonPrivateSecurityRealm(false))
}
// Files are authoritative for this single-user learning environment, including password rotation.
instance.securityRealm.createAccount('admin', adminPassword)
def authorization = new FullControlOnceLoggedInAuthorizationStrategy()
authorization.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(authorization)
instance.setNumExecutors(0)
instance.setSlaveAgentPort(-1)
JenkinsLocationConfiguration.get().setUrl(System.getenv('JENKINS_URL') ?: 'http://localhost:8080/')
JenkinsLocationConfiguration.get().save()

def store = SystemCredentialsProvider.getInstance().getStore()
def putCredential = { id, description, username, password ->
    def credential = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, id, description, username, password)
    def existing = store.getCredentials(Domain.global()).find { it.id == id }
    if (existing) store.updateCredentials(Domain.global(), existing, credential)
    else store.addCredentials(Domain.global(), credential)
}
putCredential('tomcat-deployer', 'Tomcat deployment account', 'deployer', readSecret('tomcat_password'))
def token = readSecret('github_token')
def githubCredentialId = ''
if (token) {
    putCredential('github-read', 'Read-only GitHub repository token', readSecret('github_username'), token)
    githubCredentialId = 'github-read'
}
def privateKey = readSecret('github_ssh_key')
if (privateKey) {
    def sshCredential = new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL, 'github-ssh', 'git',
        new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(privateKey), '', 'Read-only repository deploy key')
    def existing = store.getCredentials(Domain.global()).find { it.id == 'github-ssh' }
    if (existing) store.updateCredentials(Domain.global(), existing, sshCredential)
    else store.addCredentials(Domain.global(), sshCredential)
    githubCredentialId = 'github-ssh'
}

def agentName = 'maven-agent'
if (instance.getNode(agentName) == null) {
    def node = new DumbSlave(agentName, '/home/jenkins/agent', new JNLPLauncher())
    node.setNumExecutors(1)
    node.setLabelString('maven')
    node.setMode(Node.Mode.EXCLUSIVE)
    node.setRetentionStrategy(new RetentionStrategy.Always())
    instance.addNode(node)
}
def bootstrap = new File('/var/jenkins_home/agent-bootstrap')
bootstrap.mkdirs()
def secretFile = new File(bootstrap, 'secret')
secretFile.text = instance.getComputer(agentName).getJnlpMac()
secretFile.setReadable(false, false)
secretFile.setReadable(true, true)
secretFile.setWritable(false, false)
secretFile.setWritable(true, true)

def repository = System.getenv('GIT_REPOSITORY_URL')
if (!(repository?.startsWith('https://github.com/') || repository?.startsWith('git@github.com:'))) {
    throw new IllegalStateException('Configure a GitHub repository URL')
}
def branch = System.getenv('GIT_BRANCH') ?: 'main'
def job = instance.getItem('java-cicd') ?: instance.createProject(WorkflowJob, 'java-cicd')
def scm = new GitSCM([new UserRemoteConfig(repository, null, null, githubCredentialId)],
    [new BranchSpec('*/' + branch)], false, [], null, null, [])
def definition = new CpsScmFlowDefinition(scm, 'Jenkinsfile')
definition.setLightweight(true)
job.setDefinition(definition)
job.setDescription('Build, test, archive, deploy and verify the Release Observatory WAR. First run is manual; subsequent changes use SCM polling.')
job.save()
instance.save()
println('Project bootstrap completed: administrator, credentials, agent and java-cicd job configured.')
