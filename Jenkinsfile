@Library('jenkins-pipeline-utils') _

GITHUB_CREDENTIALS_ID = '433ac100-b3c2-4519-b4d6-207c029a103b'

node('dora-slave') {
  def serverArti = Artifactory.server 'CWDS_DEV'
  def rtGradle = Artifactory.newGradleBuild()
  def newTag
  if (env.BUILD_JOB_TYPE == 'master') {
    triggerProperties = pullRequestMergedTriggerProperties('neutron-jobs-master')
    properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '15')),
    pipelineTriggers([triggerProperties]), disableConcurrentBuilds(), [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: false],
    parameters([
      booleanParam(defaultValue: true, description: '', name: 'USE_NEWRELIC'),
      string(defaultValue: 'latest', description: '', name: 'APP_VERSION'),
      string(defaultValue: 'master', description: '', name: 'branch'),
      booleanParam(defaultValue: true, description: 'Default release version template is: <majorVersion>_<buildNumber>-RC', name: 'RELEASE_PROJECT'),
      string(defaultValue: '', description: 'Fill this field if need to specify custom version ', name: 'OVERRIDE_VERSION'),
      string(defaultValue: 'inventories/tpt2dev/hosts.yml', description: '', name: 'inventory')])
    ])
  } else {
    properties([disableConcurrentBuilds(), [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: false],
    parameters([
      string(defaultValue: 'master', description: '', name: 'branch'),
      booleanParam(defaultValue: true, description: 'Default release version template is: <majorVersion>_<buildNumber>-RC', name: 'RELEASE_PROJECT'),
      string(defaultValue: 'inventories/tpt2dev/hosts.yml', description: '', name: 'inventory')])])
  }

  try {
    stage('Preparation') {
      cleanWs()
      git branch: '$branch', credentialsId: GITHUB_CREDENTIALS_ID, url: 'git@github.com:ca-cwds/jobs.git'
      rtGradle.tool = 'Gradle_35'
      rtGradle.resolver repo:'repo', server: serverArti
      rtGradle.deployer.mavenCompatible = true
      rtGradle.deployer.deployMavenDescriptors = true
      rtGradle.useWrapper = true
    }
    if (env.BUILD_JOB_TYPE == 'master') {
      stage('Increment Tag') {
        newTag = newSemVer()
      }
    } else {
      stage('Check for Label') {
        checkForLabel('jobs')
      }
    }
    stage('Build'){
      def buildInfo = rtGradle.run buildFile: 'build.gradle', tasks: "jar shadowJar -DRelease=true -D build=${BUILD_NUMBER} -DnewVersion=${newTag}".toString()
    }
    stage('Tests and Coverage') {
      buildInfo = rtGradle.run buildFile: 'build.gradle', switches: '--info', tasks: 'test jacocoMergeTest'
    }
    stage('SonarQube analysis'){
      lint(rtGradle)
    }
    if (env.BUILD_JOB_TYPE == 'master') {
      stage('Tag Repo') {
        tagGithubRepo(newTag, GITHUB_CREDENTIALS_ID)
      }
      stage ('Push to artifactory'){
        rtGradle.deployer.deployArtifacts = true
        buildInfo = rtGradle.run buildFile: 'build.gradle', tasks: "publish -DRelease=\$RELEASE_PROJECT -DBuildNumber=\$BUILD_NUMBER -DCustomVersion=\$OVERRIDE_VERSION -DnewVersion=${newTag}".toString()
        rtGradle.deployer.deployArtifacts = false
      }
      stage('Clean WorkSpace') {
        archiveArtifacts artifacts: '**/LaunchCommand-*.jar', fingerprint: true
        publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: '**/build/reports/tests/', reportFiles: 'index.html', reportName: 'JUnitReports', reportTitles: ''])
      }
    }
  } catch(Exception e) {
    publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: '**/build/reports/tests/', reportFiles: 'index.html', reportName: 'JUnitReports', reportTitles: ''])
    emailext attachLog: true, body: "Failed: ${e}", recipientProviders: [[$class: 'DevelopersRecipientProvider']],
    subject: "Jobs failed with ${e.message}", to: "Prasad.Mysore@osi.ca.gov, tom.parker@osi.ca.gov, david.smith@osi.ca.gov, james.lebeau@osi.ca.gov, mariam.ghori@osi.ca.gov, adarsh.vandana@osi.ca.gov"
    currentBuild.result = "FAILURE"
    throw e
  } finally {
    cleanWs()
  }
}
