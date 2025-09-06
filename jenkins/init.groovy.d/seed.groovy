import jenkins.model.*
import hudson.model.*
import javaposse.jobdsl.plugin.*
import com.cloudbees.hudson.plugins.folder.*

def instance = Jenkins.getInstance()

println "--> Creating pipeline_slack.groovy job"

def jobName = "pipeline_slack"
def jobScript = new File("/usr/share/jenkins/ref/jobs/pipeline_slack.groovy").text

def job = instance.getItem(jobName)
if (job == null) {
    def pipelineJob = instance.createProject(org.jenkinsci.plugins.workflow.job.WorkflowJob, jobName)
    pipelineJob.definition = new org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition(jobScript, true)
    pipelineJob.save()
}
