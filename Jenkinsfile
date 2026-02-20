pipeline {
    agent any

    environment {
        APP_NAME = 'hello-world-app'
        IMAGE_TAG = 'latest'
        K8S_MANIFEST = 'k8s/'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile -B'
            }
        }

        stage('Unit Test') {
            steps {
                sh 'mvn test -B'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests -B'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${APP_NAME}:${IMAGE_TAG} ."
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh "kubectl apply -f ${K8S_MANIFEST}deployment.yaml"
                sh "kubectl apply -f ${K8S_MANIFEST}service.yaml"
                sh "kubectl rollout status deployment/hello-world-deployment --timeout=60s"
            }
        }

        stage('Verify Deployment') {
            steps {
                sh """
                    kubectl get pods 
                    kubectl get services
                """
            }
        }
    }

    post {
        success {
            echo 'Pipeline SUCCESS.'
        }
        failure {
            echo 'Pipeline FAILED. Check logs above.'
        }
        always {
            sh 'kubectl get all'
        }
    }
}