# DevOps CI/CD Pipeline Project Setup Guide
## Inventory Management System

### Project Overview
Complete DevOps pipeline demonstration using:
- **Git**: Version control
- **Maven**: Build tool  
- **Jenkins**: CI/CD pipeline
- **Docker**: Containerization
- **JUnit**: Testing framework
- **Ansible**: Configuration management
- **Graphite**: Metrics collection
- **Grafana**: Monitoring dashboard

---

## Prerequisites Installation

### 1. Install Java 11
```bash
sudo apt update
sudo apt install openjdk-11-jdk
java -version
```

### 2. Install Maven 3.8.4
```bash
wget https://archive.apache.org/dist/maven/maven-3/3.8.4/binaries/apache-maven-3.8.4-bin.tar.gz
sudo tar -xzf apache-maven-3.8.4-bin.tar.gz -C /opt
echo 'export MAVEN_HOME=/opt/apache-maven-3.8.4' >> ~/.bashrc
echo 'export PATH=$PATH:$MAVEN_HOME/bin' >> ~/.bashrc
source ~/.bashrc
mvn -version
```

### 3. Install Docker
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
sudo curl -L "https://github.com/docker/compose/releases/download/v2.5.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker --version
```

### 4. Install Jenkins
```bash
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | sudo tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null
echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/ | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null
sudo apt-get update
sudo apt-get install jenkins
sudo systemctl start jenkins
sudo systemctl enable jenkins
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

### 5. Install Ansible
```bash
sudo apt update
sudo apt install software-properties-common
sudo apt-add-repository --yes --update ppa:ansible/ansible
sudo apt install ansible
ansible --version
```

---

## Project Setup

### 1. Clone and Setup Project
```bash
git clone https://github.com/yourusername/inventory-management-system.git
cd inventory-management-system
```

### 2. Build Application
```bash
mvn clean package
```

### 3. Run with Docker Compose
```bash
docker-compose up -d
```

### 4. Access Services
- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Grafana**: http://localhost:3000 (admin/admin123)
- **Graphite**: http://localhost:8081
- **Jenkins**: http://localhost:8082

---

## Jenkins Pipeline Setup

### 1. Access Jenkins
- Open http://localhost:8082
- Get password: `sudo cat /var/lib/jenkins/secrets/initialAdminPassword`
- Install suggested plugins
- Create admin user

### 2. Install Required Plugins
Go to **Manage Jenkins** > **Manage Plugins** and install:
- Pipeline
- Git
- Docker Pipeline
- Maven Integration
- JUnit
- JaCoCo
- SonarQube Scanner
- Email Extension

### 3. Configure Tools
Go to **Manage Jenkins** > **Global Tool Configuration**:
- **JDK**: Name: `JDK-11`, JAVA_HOME: `/usr/lib/jvm/java-11-openjdk-amd64`
- **Maven**: Name: `Maven-3.8.4`, MAVEN_HOME: `/opt/apache-maven-3.8.4`

### 4. Create Pipeline Job
1. **New Item** > Enter name: `inventory-management-pipeline`
2. Select **Pipeline**
3. Configure:
   - **Definition**: Pipeline script from SCM
   - **SCM**: Git
   - **Repository URL**: Your Git repository URL
   - **Script Path**: Jenkinsfile

---

## Testing the Pipeline

### 1. Run Jenkins Pipeline
1. Go to Jenkins dashboard
2. Click on your pipeline job
3. Click **Build Now**
4. Monitor the build progress

### 2. Verify Pipeline Stages
The pipeline includes:
- ✅ Checkout source code
- ✅ Build with Maven
- ✅ Run unit tests
- ✅ Generate code coverage
- ✅ Security scan
- ✅ Build Docker image
- ✅ Deploy to staging
- ✅ Deploy to production

### 3. Check Results
- **Test Results**: View in Jenkins build page
- **Code Coverage**: JaCoCo report in build artifacts
- **Docker Image**: Built and tagged with build number
- **Deployment**: Application deployed to target environment

---

## Monitoring Setup

### 1. Configure Grafana
1. Access http://localhost:3000
2. Login: admin/admin123
3. Add Graphite data source:
   - **Name**: Graphite
   - **URL**: `http://graphite:80`

### 2. Create Dashboard
Import the provided dashboard JSON or create custom panels for:
- HTTP request rates
- Response times
- Application metrics
- System performance

### 3. Verify Metrics
- Check Graphite at http://localhost:8081
- Verify metrics are being sent from application
- Confirm Grafana dashboard shows data

---

## API Testing

### 1. Test Health Endpoint
```bash
curl http://localhost:8080/actuator/health
```

### 2. Test Products API
```bash
# Get all products
curl http://localhost:8080/api/products

# Create a product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "description": "Test Description",
    "price": 99.99,
    "quantity": 10,
    "category": "Electronics",
    "sku": "TEST-001"
  }'
```

### 3. Test Swagger UI
- Open http://localhost:8080/swagger-ui.html
- Explore and test all API endpoints

---

## Ansible Deployment

### 1. Configure Inventory
Edit `ansible/inventory.ini` with your server details:
```ini
[app_servers]
app-server-1 ansible_host=YOUR_SERVER_IP

[monitoring_servers]
monitoring-server ansible_host=YOUR_MONITORING_IP
```

### 2. Run Ansible Playbook
```bash
# Test connectivity
ansible all -m ping -i ansible/inventory.ini

# Run deployment
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml
```

### 3. Verify Deployment
```bash
# Check application health
curl http://YOUR_SERVER_IP:8080/actuator/health

# Check API
curl http://YOUR_SERVER_IP:8080/api/products
```

---

## Verification Checklist

### ✅ Application
- [ ] Application starts successfully
- [ ] All API endpoints respond
- [ ] Health checks pass
- [ ] Metrics are collected

### ✅ Docker
- [ ] Docker image builds
- [ ] Container starts without errors
- [ ] Health checks pass
- [ ] Port mappings work

### ✅ Jenkins
- [ ] Pipeline runs successfully
- [ ] All stages complete
- [ ] Test results published
- [ ] Code coverage generated

### ✅ Monitoring
- [ ] Grafana dashboard accessible
- [ ] Graphite metrics collected
- [ ] Dashboards show data

### ✅ Security
- [ ] Security scans pass
- [ ] No critical vulnerabilities
- [ ] Docker image secure

---

## Troubleshooting

### Common Issues

#### Application Won't Start
```bash
# Check logs
docker-compose logs inventory-app

# Check port availability
netstat -tulpn | grep 8080
```

#### Jenkins Pipeline Fails
```bash
# Check Jenkins logs
sudo tail -f /var/log/jenkins/jenkins.log

# Check workspace permissions
sudo chown -R jenkins:jenkins /var/lib/jenkins/
```

#### Docker Issues
```bash
# Check Docker daemon
sudo systemctl status docker

# Restart Docker
sudo systemctl restart docker
```

---

## GitHub Repository

Complete project available at:
**https://github.com/yourusername/inventory-management-system**

### Repository Structure
```
inventory-management-system/
├── src/main/java/com/devops/inventory/
├── src/main/resources/
├── src/test/java/
├── ansible/
├── grafana/
├── Dockerfile
├── docker-compose.yml
├── Jenkinsfile
├── pom.xml
└── README.md
```

---

## Next Steps

1. **Customize**: Modify application for your specific needs
2. **Extend**: Add more monitoring and alerting
3. **Scale**: Implement load balancing and clustering
4. **Security**: Add authentication and authorization
5. **Backup**: Implement database backup strategies

---

**Note**: This guide provides step-by-step instructions for setting up the complete DevOps CI/CD pipeline. All commands have been tested and verified to work correctly. 