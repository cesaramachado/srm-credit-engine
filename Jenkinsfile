pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timeout(time: 20, unit: 'MINUTES')
        timestamps()
    }

    stages {
        stage('Build and tests') {
            steps {
                sh 'mvn -B -ntp clean test'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: false
                }
            }
        }

        stage('Static analysis') {
            steps {
                sh 'mvn -B -ntp checkstyle:check'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B -ntp verify -DskipTests'
            }
        }
    }
}
