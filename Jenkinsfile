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
        sh "mvn -q compile --fail-never -e test"
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
        sh "mvn -q site:site -q -Dmaven.test.skip=true package"
      }
    }

    stage('deploy') {
      steps {
        script {
          def imageName = "cirta-social-${params.environment}-dockerfile"
          projectVersion = sh (script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout", returnStdout: true).trim()
          sh "echo COPY target/cirta-social-${projectVersion}.jar  /appli/cirta-social-${projectVersion}.jar >> $imageName  "
          sh "echo CMD java -jar /appli/cirta-social-${projectVersion}.jar ${params.environment} >> $imageName"
          dockerImage = docker.build("abdessamed/cirta-social:$projectVersion", "-f $imageName .")

          if (params.environment == 'release') {
            withDockerRegistry(credentialsId: 'docker-hub') {
              dockerImage.push()
            }
            echo "docker container run --rm -d -i -p 443:443 --hostname cirta-social-release --name cirta-social-release --cpus 0.5 abdessamed/cirta-social:$projectVersion"
          }

          if (params.environment == 'production') {
            sh "aws ecr get-login-password --region eu-west-2 | docker login --username AWS --password-stdin 384310696216.dkr.ecr.eu-west-2.amazonaws.com "
            sh "docker tag abdessamed/cirta-social:$projectVersion 384310696216.dkr.ecr.eu-west-2.amazonaws.com/cirta-social:$projectVersion"
            sh "docker push 384310696216.dkr.ecr.eu-west-2.amazonaws.com/cirta-social:$projectVersion"
          }

        }
      }
    }

  }

}
