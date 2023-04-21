pipeline {
  agent {
    label 'ohsome-now'
  }

  options {
    timeout(time: 30, unit: 'MINUTES')
  }

  environment {
    REPO_NAME = sh(returnStdout: true, script: 'basename `git remote get-url origin` .git').trim()
    LATEST_AUTHOR = sh(returnStdout: true, script: 'git show -s --pretty=%an').trim()
    LATEST_COMMIT_ID = sh(returnStdout: true, script: 'git describe --tags --long  --always').trim()
  }

  stages {

    stage ('Code Quality: detekt') {
      steps {
        sh './gradlew detekt'
      }
    }

    stage ('Test') {
      steps {
        script {
          echo REPO_NAME
          echo LATEST_AUTHOR
          echo LATEST_COMMIT_ID

          echo env.BRANCH_NAME
          echo env.BUILD_NUMBER
          echo env.TAG_NAME
        }

        sh './gradlew clean test'
      }
    }

    stage ('Build fat jar') {
      steps {
        sh './gradlew bootJar'
      }
    }


    stage ('Publish to Maven Repo') {
      when {
        expression {
            return (env.TAG_NAME != null  &&  !env.TAG_NAME.toString().endsWith("SNAPSHOT") )
        }
      }
      steps {
        withCredentials([ usernamePassword(credentialsId: 'HeiGIT-Repo', passwordVariable: 'ARTIFACTORY_PASSWORD', usernameVariable: 'ARTIFACTORY_USERNAME')]) {
          sh './gradlew publish'
        }
      }
    }

    stage ('Deploy') {
      when {
        expression {
            return (env.BRANCH_NAME == 'main')
        }
      }
      steps {
        /* remove the following line when secrets.properties is no longer necessary */
        sh 'echo spring.datasource.password= > src/main/resources/secrets.properties'
        /* integrate this property properly e.g. into the docker setup */
        sh 'echo server.servlet.context-path=/api >> src/main/resources/application.properties'
        sh 'docker compose -f docker-compose.yml -f docker-compose.integration.yml up -d --build --force-recreate'
      }
    }


    stage ('Encourage') {
      when {
        expression {
          if (currentBuild.number > 1) {
            date_pre = new Date(currentBuild.previousBuild.rawBuild.getStartTimeInMillis()).clearTime()
            echo date_pre.format( 'yyyyMMdd' )
            date_now = new Date(currentBuild.rawBuild.getStartTimeInMillis()).clearTime()
            echo date_now.format( 'yyyyMMdd' )
            return date_pre.numberAwareCompareTo(date_now) < 0
          }
          return false
        }
      }
      steps {
        rocketSend channel: 'jenkinsohsome', emoji: ':wink:', message: "Hey, this is just your daily notice that Jenkins is still working for you on *${REPO_NAME}* Branch ${env.BRANCH_NAME}! Happy and for free! Keep it up!" , rawMessage: true
      }
      post {
        failure {
          rocketSend channel: 'jenkinsohsome', emoji: ':disappointed:', message: "Reporting of *${REPO_NAME}*-build nr. ${env.BUILD_NUMBER} *failed* on Branch - ${env.BRANCH_NAME}  (<${env.BUILD_URL}|Open Build in Jenkins>). Latest commit from  ${LATEST_AUTHOR}." , rawMessage: true
        }
      }
    }

    stage ('Report Status Change') {
      when {
        expression {
          return ((currentBuild.number > 1) && (currentBuild.getPreviousBuild().result == 'FAILURE'))
        }
      }
      steps {
        rocketSend channel: 'jenkinsohsome', emoji: ':sunglasses:', message: "We had some problems, but we are BACK TO NORMAL! Nice debugging: *${REPO_NAME}*-build-nr. ${env.BUILD_NUMBER} *succeeded* on Branch - ${env.BRANCH_NAME}  (<${env.BUILD_URL}|Open Build in Jenkins>). Latest commit from  ${LATEST_AUTHOR}." , rawMessage: true
      }
      post {
        failure {
          rocketSend channel: 'jenkinsohsome', emoji: ':disappointed:', message: "Reporting of *${REPO_NAME}*-build nr. ${env.BUILD_NUMBER} *failed* on Branch - ${env.BRANCH_NAME}  (<${env.BUILD_URL}|Open Build in Jenkins>). Latest commit from  ${LATEST_AUTHOR}." , rawMessage: true
        }
      }
    }
  }

  post {
    always {
      junit 'build/test-results/**/*.xml'
    }
  }
}
