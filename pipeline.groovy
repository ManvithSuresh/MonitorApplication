pipeline {
    agent any

    tools {
        maven 'maven3'
        jdk 'jdk8'
        nodejs 'node20'
    }

    environment {
        SONAR_SCANNER_HOME = tool 'sonar-scanner'
    }

    stages {

        stage('Frontend code Checkout') {
            steps {
                dir('frontend') {
                    git branch: 'main', url: 'https://github.com/ManvithSuresh/MonitorAppFrontEnd.git'
                }
            }
        }

        stage('Install Frontend Dependencies') {
            steps {
                dir('frontend') {
                    sh 'npm install'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm run build'
                }
            }
        }

        stage('SonarQube FrontEnd Analysis') {
            steps {
                dir('frontend') {
                    withSonarQubeEnv('sonar-scanner') {
                        sh "${SONAR_SCANNER_HOME}/bin/sonar-scanner -Dsonar.projectName=MonitorAppFrontEnd -Dsonar.projectKey=MonitorAppFEKey"
                    }
                }
            }
        }

        stage('Docker FrontEnd Build') {
            steps {
                dir('frontend') {
                    sh 'docker build -t monitorappfe:latest .'
                }
            }
        }

        stage('Docker Build Push Frontend Image'){
            steps{
                script{
                    dir('backend') {
                        withDockerRegistry(credentialsId: 'DockerHub'){
                        sh "docker tag monitorappfe:latest manvitth/monitorappfe:latest"
                        sh "docker push manvitth/monitorappfe:latest"
                        }
                    }
                }
            }
        }


        stage('Run Docker Container FrontEnd') {
            steps {
                script {
                    dir('frontend') {
                        sh 'docker stop monitorappfe || true'
                        sh 'docker rm monitorappfe || true'
                        sh 'docker run -d --restart unless-stopped -p 3000:3000 --name monitorappfe --network springbootnetwork -d manvitth/monitorappfe:latest'
                    }
                }
            }
        }

        stage('Backend Code Checkout') {
            steps {
                dir('backend') {
                    git branch: 'main', url: 'https://github.com/ManvithSuresh/MonitorAppBackEnd.git'
                }
            }
        }

        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh 'mvn clean install'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('backend') {
                    withSonarQubeEnv('sonar-scanner') {
                        sh """
                        ${SONAR_SCANNER_HOME}/bin/sonar-scanner \
                        -Dsonar.projectName=MonitorAppBackEnd \
                        -Dsonar.projectKey=MotiorAppBEKey \
                        -Dsonar.java.binaries=target/classes
                        """
                    }
                }
            }
        }

        stage('Docker Build for Backend') {
            steps {
                dir('backend') {
                    sh 'docker build -t monitorappbe:latest .'
                }
            }
        }

        stage('Docker Build Push Backend Image'){
            steps{
                script{
                    dir('backend') {
                        withDockerRegistry(credentialsId: 'DockerHub'){
                        sh "docker tag monitorappbe:latest manvitth/monitorappbe:latest"
                        sh "docker push manvitth/monitorappbe:latest"
                        }
                    }
                }
            }
        }

        stage('Run Docker Container BackEnd') {
            steps {
                script {
                    dir('backend') {
                        sh 'docker stop monitorappbe || true'
                        sh 'docker rm monitorappbe || true'
                        sh 'docker run --restart unless-stopped -d -p 9090:9090 --name monitorappbe --network springbootnetwork -d manvitth/monitorappbe:latest'
                    }
                }
            }
        }
    }
}
