pipeline {
  agent {
    label 'agent_2'
  }
  environment {
    maven_is_installed = sh (script: "mvn -v", returnStatus: true)
    testResultSummary = 100
    imageName = 'cirta-social-release-dockerfile'
    dockerImage = ''
  }

  stages {
    stage('checkout') {
      steps {
        echo "checkout $currentBuild.projectName from github repository"
        checkout scm: [$class: 'GitSCM', branches: [[name: 'refs/heads/master']], extensions: [], userRemoteConfigs: [[credentialsId: 'test', name: 'origin', refspec: 'refs/heads/master:refs/remotes/origin/master', url: 'https://github.com/abdessamed-diab/cirta-social']]]
      }
    }

    stage('push') {
      steps {
        echo "push image: $env.imageName to docker hub registry."
        script {
          dockerImage = docker.build " --file $env.imageName --tag abdessamed/cirta-social:2.0-RC1"
          withDockerRegistry(credentialsId: 'docker-hub') {
            dockerImage.push()
          }
        }
      }
    }

    stage('requirements') {
      when {
        not {
          environment name: 'maven_is_installed', value: '0'
        }
        beforeAgent true
      }
      steps {
        script {
          currentBuild.result = 'aborted'
        }
      }
      post {
        aborted {
          error "build was aborted due system requirements."
        }
      }
    }

    stage('compile') {
      steps {
        sh "mvn compile"
      }
    }

    stage('test') {
      steps {
        sh "mvn --fail-never test"
        script {
          def summary = junit allowEmptyResults: true, healthScaleFactor: 2, testResults: 'target/surefire-reports/*.xml'
          def successPercentage = 100 -  (  ( 100 * summary.failCount / summary.totalCount) * 2  )
          if ( successPercentage < 90  ) {
            currentBuild.result = 'aborted'
            testResultSummary =  successPercentage
          }
        }
      }

      post {
        aborted {
          error "build is aborted due project health, check test success percentage: $testResultSummary"
        }
      }
    }

  }

}
