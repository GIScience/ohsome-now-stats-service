// redundant code segments are to be moved into jte functions at a later point in time

pipeline {
  agent {
    label 'worker'
  }


  environment {
    // this variable defines which branches will be deployed as snapshots
    SNAPSHOT_BRANCH_REGEX = /(^main$)/
  }

  stages {
    stage('Code Quality: detekt') {
      steps {

        // setting up a few basic env variables like REPO_NAME and LATEST_AUTHOR
        setup_basic_env()
        script {
          echo REPO_NAME
          echo LATEST_AUTHOR
          echo LATEST_COMMIT_ID

          echo env.BRANCH_NAME
          echo env.BUILD_NUMBER
          echo env.TAG_NAME

          server = Artifactory.server 'HeiGIT Repo'
          rtGradle = Artifactory.newGradleBuild()

          rtGradle.tool = 'Gradle 7'
          rtGradle.resolver server: server, repo: 'main'
          rtGradle.deployer server: server, releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local'
          rtGradle.deployer.deployArtifacts = false

          rtGradle.run tasks: 'detekt'
        }
      }
      post {
        failure {
          rocket_basicsend("*${REPO_NAME}*-build nr. ${env.BUILD_NUMBER} *failed* code quality detekt on Branch - ${env.BRANCH_NAME}  (<${env.BUILD_URL}|Open Build in Jenkins>). Latest commit from  ${LATEST_AUTHOR}. Review the code!")
        }
      }
    }

    stage ('Build fat jar') {
      steps {
        script {
          server = Artifactory.server 'HeiGIT Repo'
          rtGradle = Artifactory.newGradleBuild()

          rtGradle.tool = 'Gradle 7'
          rtGradle.resolver server: server, repo: 'main'
          rtGradle.deployer server: server, releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local'
          rtGradle.deployer.deployArtifacts = false

          rtGradle.run tasks: 'bootJar'
        }
      }
      post {
        failure {
          rocket_buildfail()
        }
      }
    }

    stage ('Test') {
      steps {
        script {
          server = Artifactory.server 'HeiGIT Repo'
          rtGradle = Artifactory.newGradleBuild()

          rtGradle.tool = 'Gradle 7'
          rtGradle.resolver server: server, repo: 'main'
          rtGradle.deployer server: server, releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local'
          rtGradle.deployer.deployArtifacts = false

          rtGradle.run tasks: 'clean test'
        }
      }
      post {
        failure {
          rocket_testfail()
        }
      }
    }

    stage ('Deploy to Artifactory') {
      when {
        expression {
          return env.BRANCH_NAME ==~ SNAPSHOT_BRANCH_REGEX && VERSION ==~ /.*-SNAPSHOT$/
        }
      }
      steps {
        script {
          server = Artifactory.server 'HeiGIT Repo'
          rtGradle = Artifactory.newMavenBuild()

          rtGradle.tool = 'Gradle 7'
          rtGradle.resolver server: server, repo: 'main'
          rtGradle.deployer server: server, releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local'
          rtGradle.deployer.deployArtifacts = false

          buildInfo = rtGradle.run tasks: 'clean bootJar'

          rtGradle.deployer.deployArtifacts buildInfo
          server.publishBuildInfo buildInfo
        }
      }
      post {
        failure {
          rocket_basicsend("*${REPO_NAME}*-build nr. ${env.BUILD_NUMBER} *failed* deployment to artifactory on Branch - ${env.BRANCH_NAME}  (<${env.BUILD_URL}|Open Build in Jenkins>). Latest commit from  ${LATEST_AUTHOR}. Review the code!")
        }
      }
    }
  }

}