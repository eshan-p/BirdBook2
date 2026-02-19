pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = "epateltx"
        DOCKER_CREDENTIALS = "dockerhub-credentials"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Docker Login') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKER_CREDENTIALS}",
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                    echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                    '''
                }
            }
        }

        // =============================
        // BUILD & PUSH IMAGES
        // =============================

        stage('Build & Push Services') {
            steps {
                script {
                    def services = [
                        [name: "user-service", folder: "user"],
                        [name: "group-service", folder: "group"],
                        [name: "post-service", folder: "post"],
                        [name: "bird-service", folder: "bird"],
                        [name: "eureka-server", folder: "eureka-server"]
                    ]

                    for (svc in services) {
                        sh """
                        docker build -t ${DOCKERHUB_USERNAME}/${svc.name}:latest ./${svc.folder}
                        docker push ${DOCKERHUB_USERNAME}/${svc.name}:latest
                        """
                    }
                }
            }
        }

        // =============================
        // DEPLOYMENTS
        // =============================

        stage('Deploy User Service') {
            steps {
                sshagent(['user-service-ssh']) {
                    sh """
                    ssh -o StrictHostKeyChecking=no ec2-user@13.59.22.2 '
                        docker pull ${DOCKERHUB_USERNAME}/user-service:latest &&
                        docker stop user-service || true &&
                        docker rm user-service || true &&
                        docker run -d -p 8081:8081 --name user-service ${DOCKERHUB_USERNAME}/user-service:latest
                    '
                    """
                }
            }
        }

        stage('Deploy Group Service') {
            steps {
                sshagent(['group-service-ssh']) {
                    sh """
                    ssh -o StrictHostKeyChecking=no ec2-user@3.135.238.1 '
                        docker pull ${DOCKERHUB_USERNAME}/group-service:latest &&
                        docker stop group-service || true &&
                        docker rm group-service || true &&
                        docker run -d -p 8084:8084 --name group-service ${DOCKERHUB_USERNAME}/group-service:latest
                    '
                    """
                }
            }
        }

        stage('Deploy Post Service') {
            steps {
                sshagent(['post-service-ssh']) {
                    sh """
                    ssh -o StrictHostKeyChecking=no ec2-user@18.216.201.210 '
                        docker pull ${DOCKERHUB_USERNAME}/post-service:latest &&
                        docker stop post-service || true &&
                        docker rm post-service || true &&
                        docker run -d -p 8083:8083 --name post-service ${DOCKERHUB_USERNAME}/post-service:latest
                    '
                    """
                }
            }
        }

        stage('Deploy Bird Service') {
            steps {
                sshagent(['bird-service-ssh']) {
                    sh """
                    ssh -o StrictHostKeyChecking=no ec2-user@3.139.74.245 '
                        docker pull ${DOCKERHUB_USERNAME}/bird-service:latest &&
                        docker stop bird-service || true &&
                        docker rm bird-service || true &&
                        docker run -d -p 8082:8082 --name bird-service ${DOCKERHUB_USERNAME}/bird-service:latest
                    '
                    """
                }
            }
        }

        stage('Deploy Eureka Server') {
            steps {
                sshagent(['eureka-service-ssh']) {
                    sh """
                    ssh -o StrictHostKeyChecking=no ec2-user@3.21.230.30 '
                        docker pull ${DOCKERHUB_USERNAME}/eureka-server:latest &&
                        docker stop eureka-server || true &&
                        docker rm eureka-server || true &&
                        docker run -d -p 8761:8761 --name eureka-server ${DOCKERHUB_USERNAME}/eureka-server:latest
                    '
                    """
                }
            }
        }
    }

    post {
        always {
            sh 'docker logout'
        }
    }
}
