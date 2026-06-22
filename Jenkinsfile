pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'elearning-app'
        DOCKER_PORT = '8085'
    }
    
    stages {
        stage('🧹 Clean') {
            steps {
                sh 'mvn clean'
                echo '✅ Nettoyage terminé'
            }
        }
        
        stage('🔨 Build') {
            steps {
                sh 'mvn compile -DskipTests'
                echo '✅ Build terminé'
            }
        }
        
        stage('🧪 Tests') {
            steps {
                sh 'mvn test'
                echo '✅ Tests terminés'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('📦 Package') {
            steps {
                sh 'mvn package -DskipTests'
                echo '✅ Package créé'
            }
        }
        
        stage('🐳 Docker Build') {
            steps {
                script {
                    sh '''
                        docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} .
                        docker tag ${DOCKER_IMAGE}:${BUILD_NUMBER} ${DOCKER_IMAGE}:latest
                        echo "✅ Image Docker construite"
                    '''
                }
            }
        }
        
        stage('🚀 Deploy') {
            steps {
                script {
                    sh '''
                        echo "=== Déploiement ==="
                        docker stop elearning-app || true
                        docker rm elearning-app || true
                        docker run -d -p ${DOCKER_PORT}:${DOCKER_PORT} \
                            --name elearning-app \
                            --restart unless-stopped \
                            ${DOCKER_IMAGE}:${BUILD_NUMBER}
                        echo "✅ Application déployée sur le port ${DOCKER_PORT}"
                    '''
                }
            }
        }
        
        stage('🔎 Verify') {
            steps {
                script {
                    sh '''
                        echo "=== Vérification ==="
                        sleep 30
                        if curl -s -f http://localhost:${DOCKER_PORT}/actuator/health; then
                            echo "✅ Application fonctionnelle"
                        else
                            echo "❌ Application non disponible"
                            exit 1
                        fi
                    '''
                }
            }
        }
    }
    
    post {
        success {
            echo '''
            ========================================
            ✅ PIPELINE RÉUSSI !
            ========================================
            🌐 Application: http://localhost:''' + "${DOCKER_PORT}" + '''
            ========================================
            '''
        }
        failure {
            echo '''
            ========================================
            ❌ PIPELINE ÉCHOUÉ !
            ========================================
            Vérifiez les logs ci-dessus.
            ========================================
            '''
        }
    }
}
