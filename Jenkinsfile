pipeline {
    agent { label 'maven' }
    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        timeout(time: 20, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '15', artifactNumToKeepStr: '5'))
        timestamps()
    }
    triggers { pollSCM('H/2 * * * *') }
    environment {
        MAVEN_OPTS = '-Xmx512m'
        TOMCAT_URL = 'http://tomcat:8080'
    }
    stages {
        stage('Checkout') {
            steps {
                deleteDir()
                checkout scm
                script { env.RELEASE_COMMIT = sh(script: 'git rev-parse HEAD', returnStdout: true).trim() }
            }
        }
        stage('Build') {
            steps { sh 'mvn -B -ntp clean compile -Dgit.commit="$RELEASE_COMMIT" -Dbuild.number="$BUILD_NUMBER"' }
        }
        stage('Test') {
            steps { sh 'mvn -B -ntp test -Dgit.commit="$RELEASE_COMMIT" -Dbuild.number="$BUILD_NUMBER"' }
            post { always { junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: false } }
        }
        stage('Package') {
            // Maven repeats the test phase here deliberately: packaging always enforces tests.
            steps { sh 'mvn -B -ntp package -Dgit.commit="$RELEASE_COMMIT" -Dbuild.number="$BUILD_NUMBER"' }
        }
        stage('Archive') {
            steps {
                sh 'sha256sum target/cicd-demo.war > target/cicd-demo.war.sha256'
                archiveArtifacts artifacts: 'target/cicd-demo.war,target/cicd-demo.war.sha256', fingerprint: true
            }
        }
        stage('Deploy') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'tomcat-deployer', usernameVariable: 'TOMCAT_USER', passwordVariable: 'TOMCAT_PASSWORD')]) {
                    sh 'bash scripts/deploy.sh target/cicd-demo.war'
                }
            }
        }
        stage('Verify') {
            steps { sh 'bash scripts/smoke-test.sh "$TOMCAT_URL/cicd-demo" "$RELEASE_COMMIT"' }
        }
    }
}
