// redundant code segments are to be moved into jte functions at a later point in time

pipeline {
  agent {
    label 'worker'
  }

  tools {
    maven 'Maven 3'
  }

  environment {
    // this variable defines which branches will be deployed
    SNAPSHOT_BRANCH_REGEX = /(^main$)/
    RELEASE_REGEX = /^([0-9]+(\.[0-9]+)*)(-(RC|beta-|alpha-)[0-9]+)?$/
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
          rocket_basicsend("*${env.REPO_NAME}*-build nr. ${env.BUILD_NUMBER} *failed* code quality detekt on Branch - ${env.BRANCH_NAME}  (<${env.BUILD_URL}|Open Build in Jenkins>). Latest commit from  ${env.LATEST_AUTHOR}. Review the code!")
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

    stage ('Deploy Snapshot') {
      when {
        expression {
          return env.BRANCH_NAME ==~ SNAPSHOT_BRANCH_REGEX && VERSION ==~ /.*-SNAPSHOT$/
        }
      }
      steps {
        deploy_snapshot('clean package spring-boot:repackage deploy -P git')
      }
      post {
        failure {
          rocket_snapshotdeployfail()
        }
      }
    }

    stage ('Deploy Release') {
      when {
        expression {
          return VERSION ==~ RELEASE_REGEX && env.TAG_NAME ==~ RELEASE_REGEX
        }
      }
      steps {
        deploy_release('clean package spring-boot:repackage deploy -P git')
      }
      post {
        failure {
           rocket_releasedeployfail()
        }
      }
    }

    stage('Wrapping Up') {
      steps {
        encourage()
        status_change()
      }
    }

  }
}
