import groovy.transform.Field
@Library('jenkins-pipeline-utils') _

@Field
def GITHUB_CREDENTIALS_ID = '433ac100-b3c2-4519-b4d6-207c029a103b'
@Field
def newTag
@Field
def serverArti
@Field
def rtGradle
@Field
def SLACK_WEBHOOK_URL = 'https://hooks.slack.com/services/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'

switch(env.BUILD_JOB_TYPE) {
  case "master": buildMaster(); break;
  case "release":releasePipeline(); break;
  default: buildPullRequest();
}

def buildPullRequest() {
  node('tpt4-slave') {
    def triggerProperties = githubPullRequestBuilderTriggerProperties()
    properties([disableConcurrentBuilds(), [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: false],
    githubConfig(),
    pipelineTriggers([triggerProperties]),
    parameters([
      string(defaultValue: 'master', description: '', name: 'branch'),
      booleanParam(defaultValue: true, description: 'Default release version template is: <majorVersion>_<buildNumber>-RC', name: 'RELEASE_PROJECT'),
      string(defaultValue: 'inventories/tpt2dev/hosts.yml', description: '', name: 'inventory')])])

    try {
      checkOut()
      verifySemVerLabel()
      build()
      javadoc()
      testAndCoverage()
      sonarQubeAnalysis()
    } catch(Exception exception) {
        emailext attachLog: true, body: "Failed: ${e}", recipientProviders: [[$class: 'DevelopersRecipientProvider']],
        subject: "Neutron Jobs failed with ${e.message}", to: "david.smith@osi.ca.gov, igor.chornobay@osi.ca.gov"
        currentBuild.result = "FAILURE"
        throw exception
    } finally {
        publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'build/reports/tests', reportFiles: 'index.html', reportName: 'JUnit Report', reportTitles: 'JUnit tests summary'])
        cleanWs()
    }
  }
}

def buildMaster() {
  node('tpt4-slave') {
    triggerProperties = pullRequestMergedTriggerProperties('jobs-master')
    properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '25')),
    pipelineTriggers([triggerProperties]), disableConcurrentBuilds(), [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: false],
    parameters([
      booleanParam(defaultValue: true, description: '', name: 'USE_NEWRELIC'),
      string(defaultValue: 'master', description: '', name: 'branch'),
      booleanParam(defaultValue: true, description: 'Default release version template is: <majorVersion>_<buildNumber>-RC', name: 'RELEASE_PROJECT'),
      string(defaultValue: '', description: 'Fill this field if need to specify custom version ', name: 'OVERRIDE_VERSION'),
      string(defaultValue: 'inventories/tpt2dev/hosts.yml', description: '', name: 'inventory')])
    ])

    try {
      checkOut()
      incrementTag()
      build()
      javadoc()
      testAndCoverage()
      sonarQubeAnalysis()
      tagRepo()
      pushToArtifatory()
      deployToRundeck()
      cleanWorkspace()
    } catch (Exception exception) {
        notifySlack(SLACK_WEBHOOK_URL, "Neutron-Jobs", exception)
        emailext attachLog: true, body: "Failed: ${e}", recipientProviders: [[$class: 'DevelopersRecipientProvider']],
        subject: "Neutron Jobs failed with ${e.message}", to: "david.smith@osi.ca.gov, igor.chornobay@osi.ca.gov"
        currentBuild.result = "FAILURE"
    } finally {
        publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'build/reports/tests', reportFiles: 'index.html', reportName: 'JUnit Report', reportTitles: 'JUnit tests summary'])
        cleanWs()
    }
  }
}

def releasePipeline() {
  parameters([
    string(name: 'APP_VERSION', defaultValue: '', description: 'App version to deploy')
  ])

  try {
    deploy('preint')
    deploy('integration')
  } catch (Exception exception) {
    notifySlack(SLACK_WEBHOOK_URL, "Neutron-Jobs", exception)
    currentBuild.result = "FAILURE"
    throw exception
  }
}


def checkOut()  {
  stage('Check Out') {
    cleanWs()
    git branch: '$branch', credentialsId: GITHUB_CREDENTIALS_ID, url: 'git@github.com:ca-cwds/jobs.git'
  }
}

def verifySemVerLabel() {
  stage('Verify SemVer Label') {
    checkForLabel('jobs')
  }
}

def build() {
  stage('Build') {
    serverArti = Artifactory.server 'CWDS_DEV'
    rtGradle = Artifactory.newGradleBuild()
    rtGradle.tool = 'Gradle_35'
    rtGradle.resolver repo:'repo', server: serverArti
    rtGradle.deployer.mavenCompatible = true
    rtGradle.deployer.deployMavenDescriptors = true
    rtGradle.useWrapper = true
    def buildInfo = rtGradle.run buildFile: 'build.gradle', tasks: "jar shadowJar -DRelease=true -D build=${BUILD_NUMBER} -DnewVersion=${newTag}".toString()
  }
}

def testAndCoverage() {
  stage('Tests and Coverage') {
    buildInfo = rtGradle.run buildFile: 'build.gradle', switches: '--info', tasks: 'test jacocoTestReport'
  }
}

def sonarQubeAnalysis() {
  stage('SonarQube analysis'){
    lint(rtGradle)
  }
}

def javadoc() {
  stage('Javadoc') {
    rtGradle.run buildFile: 'build.gradle', tasks: 'javadoc'
  }
}

def incrementTag() {
  stage('Increment Tag') {
    newTag = newSemVer()
  }
}

def tagRepo() {
  stage('Tag Repo') {
    tagGithubRepo(newTag, GITHUB_CREDENTIALS_ID)
  }
}

def pushToArtifatory() {
  stage ('Push to artifactory'){
    rtGradle.deployer.deployArtifacts = true
    buildInfo = rtGradle.run buildFile: 'build.gradle', tasks: "publish -DRelease=\$RELEASE_PROJECT -DBuildNumber=\$BUILD_NUMBER -DCustomVersion=\$OVERRIDE_VERSION -DnewVersion=${newTag}".toString()
    rtGradle.deployer.deployArtifacts = false
  }
}

def deployToRundeck() {
  stage ('Deploy to Rundeck@Dev') {
    build job: 'tpt4-api-deploy-jobs', parameters: [[$class: 'StringParameterValue', name: 'playbook', value: 'deploy-jobs-to-rundeck.yml'], [$class: 'StringParameterValue', name: 'version', value: 'LATEST']]
  }
}

def triggerReleasePipeline() {
  stage('Trigger Release Pipeline') {
    withCredentials([usernameColonPassword(credentialsId: 'fa186416-faac-44c0-a2fa-089aed50ca17', variable: 'jenkinsauth')]) {
      sh "curl -v -u $jenkinsauth 'http://jenkins.mgmt.cwds.io:8080/job/PreInt-Integration/job/deploy-neutron-jobs/buildWithParameters" +
      "?token=trigger-neutron-jobs-deploy" +
      "&cause=Caused%20by%20Build%20${env.BUILD_ID}" +
      "&APP_VERSION=${newTag}'"
    }
  }
}

def cleanWorkspace() {
  stage('Clean WorkSpace') {
    publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'build/docs/javadoc', reportFiles: 'index-all.html', reportName: 'javadoc', reportTitles: 'javadoc'])
    archiveArtifacts artifacts: '**/LaunchCommand-*.jar', fingerprint: true
  }
}

def githubConfig() {
  githubConfigProperties('https://github.com/ca-cwds/jobs')
}

def deploy(environment) {
  node(environment) {
    checkOutStage()
    deployToStage(environment, env.APP_VERSION)
    updateManifestStage(environment, env.APP_VERSION)
  }
}

def checkOutStage() {
  stage('Check Out Stage') {
    dir('de-ansible') {
      cleanWs()
      git branch: "master", credentialsId: GITHUB_CREDENTIALS_ID, url: 'git@github.com:ca-cwds/de-ansible.git'
    }
  }
}


def deployToStage(environment, version) {
  stage("Deploy to $environment") {
    dir('de-ansible') {
      sh "ansible-playbook -e Job_StartScript=$Job_StartScript -e Java_heap_size=$Java_heap_size -e JobLastRun_time=$Reset_JobLastRun_time -e VERSION_NUMBER=$version -i inventories/$environment/hosts.yml deploy-jobs-to-rundeck.yml --vault-password-file ~/.ssh/vault.txt -vv"
    }
  }
}

def updateManifestStage(environment, version) {
  stage("Update Manifest Version for $environment") {
    updateManifest("jobs", environment, GITHUB_CREDENTIALS_ID, version)
  }
}
