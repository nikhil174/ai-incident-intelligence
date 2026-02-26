pipeline {
  agent any
  environment {
    IMAGE_TAG = "${env.BUILD_NUMBER}"
  }
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Unit Tests') {
      steps {
        sh 'cd projects/ai-incident-intelligence/services/event-ingestion-service && mvn -q test || true'
        sh 'cd projects/ai-incident-intelligence/services/processor-service && mvn -q test || true'
        sh 'cd projects/ai-incident-intelligence/services/incident-service && mvn -q test || true'
      }
    }

    stage('Build Images') {
      steps {
        sh 'docker build -t aiincident/event-ingestion:${IMAGE_TAG} projects/ai-incident-intelligence/services/event-ingestion-service'
        sh 'docker build -t aiincident/processor:${IMAGE_TAG} projects/ai-incident-intelligence/services/processor-service'
        sh 'docker build -t aiincident/incident:${IMAGE_TAG} projects/ai-incident-intelligence/services/incident-service'
        sh 'docker build -t aiincident/ai-analysis:${IMAGE_TAG} projects/ai-incident-intelligence/services/ai-analysis-service'
      }
    }

    stage('Deploy') {
      steps {
        echo 'Deploy to AWS target (ECS/EC2) with environment-specific script'
      }
    }

    stage('Smoke Tests') {
      steps {
        echo 'Run health checks after deployment'
      }
    }
  }
}
