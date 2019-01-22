import groovy.transform.Field
@Library('jenkins-pipeline-utils') _

@Field
def GITHUB_CREDENTIALS_ID = '433ac100-b3c2-4519-b4d6-207c029a103b'
def newTag

switch(env.BUILD_JOB_TYPE) {
  case "master": buildMaster(); break;
  case "release":releasePipeline(); break;
  default: buildPullRequest();
}

def buildPullRequest() {
  node('tpt4-slave') {
    def serverArti = Artifactory.server 'CWDS_DEV'
    def rtGradle = Artifactory.newGradleBuild()
    def triggerProperties = githubPullRequestBuilderTriggerProperties()
    properties([disableConcurrentBuilds(), [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: false],
    parameters([
      string(defaultValue: 'master', description: '', name: 'branch'),
      //booleanParam(defaultValue: true, description: 'Default release version template is: <majorVersion>_<buildNumber>-RC', name: 'RELEASE_PROJECT'),
      string(defaultValue: 'inventories/tpt2dev/hosts.yml', description: '', name: 'inventory')])])

    try {
      checkOut()
      verifySemVerLabel()
      build()
      testAndCoverage()
      sonarQubeAnalysis()
    } catch(Exception exception) {
        emailext attachLog: true, body: "Failed: ${e}", recipientProviders: [[$class: 'DevelopersRecipientProvider']],
        subject: "Neutron Jobs failed with ${e.message}", to: "david.smith@osi.ca.gov, igor.chornobay@osi.ca.gov"
        currentBuild.result = "FAILURE"
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
      //booleanParam(defaultValue: true, description: 'Default release version template is: <majorVersion>_<buildNumber>-RC', name: 'RELEASE_PROJECT'),
      //string(defaultValue: '', description: 'Fill this field if need to specify custom version ', name: 'OVERRIDE_VERSION'),
      string(defaultValue: 'inventories/tpt2dev/hosts.yml', description: '', name: 'inventory')])
    ])

    try {
      checkOut()
      javadoc()
      incrementTag()
      build()
      testAndCoverage()
      sonarQubeAnalysis()
      tagRepo()
      pushToArtifatory()
      deployToRundeck()
      cleanWorkspace()
    } catch (Exception exception) {
        emailext attachLog: true, body: "Failed: ${e}", recipientProviders: [[$class: 'DevelopersRecipientProvider']],
        subject: "Neutron Jobs failed with ${e.message}", to: "david.smith@osi.ca.gov, igor.chornobay@osi.ca.gov"
        currentBuild.result = "FAILURE"
    } finally {
        publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'build/reports/tests', reportFiles: 'index.html', reportName: 'JUnit Report', reportTitles: 'JUnit tests summary'])
        cleanWs()
    }
  }
}

def checkOut()  {
  stage('Check Out') {
    cleanWs()
    git branch: '$branch', credentialsId: GITHUB_CREDENTIALS_ID, url: 'git@github.com:ca-cwds/jobs.git'
    rtGradle.tool = 'Gradle_35'
    rtGradle.resolver repo:'repo', server: serverArti
    rtGradle.deployer.mavenCompatible = true
    rtGradle.deployer.deployMavenDescriptors = true
    rtGradle.useWrapper = true
  }
}

def verifySemVerLabel() {
  stage('Verify SemVer Label') {
    checkForLabel('jobs')
  }
}

def build() {
  stage('Build'){
    def buildInfo = rtGradle.run buildFile: 'build.gradle', tasks: "jar shadowJar -DRelease=true -DnewVersion=${newTag}".toString()
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
    buildInfo = rtGradle.run buildFile: 'build.gradle', tasks: "publish -DRelease=true -DnewVersion=${newTag}".toString()
    rtGradle.deployer.deployArtifacts = false
  }
}

def deployToRundeck() {
  stage ('Deploy to Rundeck@Dev') {
    build job: 'tpt4-api-deploy-jobs', parameters: [[$class: 'StringParameterValue', name: 'playbook', value: 'deploy-jobs-to-rundeck.yml'], [$class: 'StringParameterValue', name: 'version', value: 'LATEST']]
  }
}

def cleanWorkspace() {
  stage('Clean WorkSpace') {
    publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'build/docs/javadoc', reportFiles: 'index-all.html', reportName: 'javadoc', reportTitles: 'javadoc'])
    archiveArtifacts artifacts: '**/LaunchCommand-*.jar', fingerprint: true
  }
}
