pipeline {
    agent any

    environment {
        APP_NAME = 'hello-world-app'
        IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT?.take(7) ?: 'unknown'}"
        K8S_MANIFEST = 'k8s/'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean verify -B'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
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
                sh "docker tag ${APP_NAME}:${IMAGE_TAG} ${APP_NAME}:latest"
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh "sed -i 's|hello-world-app:latest|${APP_NAME}:${IMAGE_TAG}|g' ${K8S_MANIFEST}deployment.yaml"
                sh "kubectl apply -f ${K8S_MANIFEST}deployment.yaml"
                sh "kubectl apply -f ${K8S_MANIFEST}service.yaml"
                sh "kubectl rollout status deployment/hello-world-deployment --timeout=120s"
            }
        }

        stage('Verify Deployment') {
            steps {
                sh """
                    kubectl get pods -l app=hello-world
                    kubectl get services hello-world-service
                """
            }
        }
    }

    post {
        success {
            echo "Pipeline SUCCESS - deployed ${APP_NAME}:${IMAGE_TAG}"
        }
        failure {
            echo 'Pipeline FAILED - initiating rollback...'
            sh 'kubectl rollout undo deployment/hello-world-deployment || true'
        }
        always {
            sh 'kubectl get all -l app=hello-world || true'
        }
    }
}