pipeline {
  agent {
    label 'agent_2'
  }

  parameters {
    choice choices: ['release', 'production'], description: 'choose target environment', name: 'environment'
  }

  environment {
    maven_is_installed = sh (script: "mvn -v", returnStatus: true)
    testResultSummary = 100
    dockerImage = ''
    projectVersion = ''
  }

  stages {
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

    stage('package') {
      steps {
        sh "mvn package"
      }
    }

    stage('deploy') {
      steps {
        script {
          def imageName = "cirta-social-${params.environment}-dockerfile"
          echo "image name is : $imageName"
          projectVersion = sh (script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout", returnStdout: true).trim()
          echo "extracted version: $projectVersion"
          sh "echo COPY target/dz.cirta.cirta-social-${projectVersion}.jar  /appli/cirta-social-${projectVersion}.jar >> $imageName  "
          sh "echo CMD java -jar /appli/cirta-social-${projectVersion}.jar ${params.environment} >> $imageName"
          dockerImage = docker.build("abdessamed/cirta-social:$projectVersion", "-f $imageName .")
          withDockerRegistry(credentialsId: 'docker-hub') {
            dockerImage.push()
          }
        }
      }
    }

  }

}
