// redundant code segments are to be moved into jte functions at a later point in time

pipeline {
  agent {
    label 'worker'
  }
  tools {
    gradle 'Gradle 7'
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

          sh 'gradle detekt'
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
          sh 'gradle bootJar'
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
          sh 'gradle clean test'
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
        withCredentials([usernamePassword(credentialsId: 'HeiGIT-Nexus', passwordVariable: 'ORG_GRADLE_PROJECT_heigitNexusPassword', usernameVariable: 'ORG_GRADLE_PROJECT_heigitNexusUsername')]) {
          script {
            env.ORG_GRADLE_PROJECT_repositoryUrl = "https://repo.heigit.org/repository/maven-snapshots/"
            sh 'gradle publish'
          }
        }
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
        withCredentials([usernamePassword(credentialsId: 'HeiGIT-Nexus', passwordVariable: 'ORG_GRADLE_PROJECT_heigitNexusPassword', usernameVariable: 'ORG_GRADLE_PROJECT_heigitNexusUsername')]) {
          script {
            env.ORG_GRADLE_PROJECT_repositoryUrl = "https://repo.heigit.org/repository/maven-releases/"
            sh 'gradle publish'
          }
        }
      }
      post {
        failure {
          rocket_snapshotdeployfail()
        }
      }
    }
  }
}
