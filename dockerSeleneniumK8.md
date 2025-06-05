**Complete Guide to Run UI Automation Repo on Jenkins with Docker & Kubernetes (4 Parallel Pods)**

Docker Prerequisite: https://chatgpt.com/canvas/shared/683eb01e41808191bfdfffb552d0bd1c

Repo: [https://github.com/sachinKhandelwall/cloudbeesUiAutomationAssignment](https://github.com/sachinKhandelwall/cloudbeesUiAutomationAssignment)

---

### 🔧 Prerequisites

Ensure the following are installed and configured:

* Jenkins with required plugins (Pipeline, Kubernetes CLI, Docker Pipeline)
* Docker installed on Jenkins agent
* Kubernetes cluster accessible from Jenkins
* GitHub repo access (public or via Jenkins credentials if private)
* DockerHub account for image push

---

## ✅ Step 1: Create Dockerfile

This file defines how your Selenium Test container will be built.

📁 Location: In the root folder of your repo (same level as pom.xml)

```Dockerfile
# Use Maven and JDK as base
FROM maven:3.8.5-openjdk-17-slim

# Set working directory
WORKDIR /automation

# Copy pom.xml and resolve dependencies first (to cache dependencies)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the project files
COPY . .

# Run tests using Maven and TestNG
# NOTE: We'll override this in Kubernetes job to allow parallel execution
CMD ["mvn", "clean", "test"]
```

This file will:

* Use Maven + JDK 17
* Copy your repo (including testng.xml, drivers, etc.)
* Run tests when container starts

---

## ✅ Step 2: Jenkins Pipeline Setup

You will create a Jenkins pipeline that:

1. Clones your GitHub repo
2. Builds Docker image from Dockerfile
3. Pushes image to DockerHub
4. Triggers Kubernetes Job to run 4 pods in parallel

### Jenkinsfile (Declarative Pipeline)

📁 Place this file in the root of the GitHub repo.

```groovy
pipeline {
  agent any

  environment {
    IMAGE_NAME = "yourdockerhubusername/selenium-tests"
    K8S_YAML_PATH = "k8/k8-job.yaml"
  }

  stages {
    stage('Clone Repository') {
      steps {
        git 'https://github.com/sachinKhandelwall/cloudbeesUiAutomationAssignment.git'
      }
    }

    stage('Build Docker Image') {
      steps {
        script {
          docker.build("${IMAGE_NAME}")
        }
      }
    }

    stage('Push to DockerHub') {
      steps {
        withDockerRegistry([ credentialsId: 'dockerhub-creds', url: '' ]) {
          script {
            docker.image("${IMAGE_NAME}").push()
          }
        }
      }
    }

    stage('Deploy to Kubernetes') {
      steps {
        sh "kubectl apply -f ${K8S_YAML_PATH}"
      }
    }
  }
}
```

📝 Notes:

* `dockerhub-creds` is a Jenkins credential with your DockerHub username/password
* `k8/k8-job.yaml` will be created in next step

---

## ✅ Step 3: Kubernetes Job YAML to Run 4 Pods

This YAML will create 4 pods in parallel to run the same Docker image (you can parameterize by group later if needed).

📁 Save as: `k8/k8-job.yaml`

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: selenium-parallel-job
spec:
  completions: 4         # Total pods to run
  parallelism: 4         # Run 4 pods in parallel
  template:
    spec:
      containers:
      - name: selenium-test
        image: yourdockerhubusername/selenium-tests
        imagePullPolicy: Always
        env:
        - name: BROWSER
          value: chrome
      restartPolicy: Never
```

If you want to run different tests in each pod:

* Use environment variables like `TEST_GROUP=group1`, `group2`, etc.
* Update your TestNG suite to read and run specific groups per pod

---

## ✅ Step 4: Configure Jenkins Credentials

Go to Jenkins > Manage Jenkins > Credentials:

* Add DockerHub credentials (ID: `dockerhub-creds`)
* (Optional) Kubeconfig if kubectl is not set globally on Jenkins agent

---

## ✅ Step 5: Setup TestNG Parallel Groups (Optional)

If needed, configure `testng.xml` with test groups:

```xml
<suite name="ParallelTests" parallel="tests" thread-count="4">
  <test name="Group1">
    <groups>
      <run>
        <include name="group1"/>
      </run>
    </groups>
    <classes>
      <class name="your.package.YourTestClass"/>
    </classes>
  </test>
  <!-- repeat for Group2, Group3, Group4 -->
</suite>
```

Then pass env TEST\_GROUP in YAML and read it via `@Parameters` or runtime system property.

---

## ✅ Step 6: Run Jenkins Pipeline

* Commit and push your code with Jenkinsfile and k8-job.yaml
* Trigger pipeline in Jenkins

Jenkins will:

1. Clone your repo
2. Build Docker image with your tests
3. Push to DockerHub
4. Deploy K8s Job with 4 pods

Each pod will:

* Pull the image
* Run mvn clean test
* Execute your UI tests (in parallel)


──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────

📝 Clarifying my Doubts

──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────


✅ 1. What is COPY . . in Dockerfile?

🔹 COPY . . means:
Copy everything (all files and folders) from the current directory on your local machine (or GitHub repo) → into the working directory inside the Docker image (e.g., /automation).

🧠 Example:
If your repo folder has:

* pom.xml
* testng.xml
* src/
* drivers/
* utils/

Then COPY . . puts all of this into /automation inside the container.
So now inside your Docker container, you’ll have:

* /automation/pom.xml
* /automation/testng.xml
* /automation/src/
* /automation/drivers/

✅ 2. What is Jenkinsfile? What type is it?

🔹 Jenkinsfile is a special file that tells Jenkins step-by-step what to do.

* File type: Plain text file (no extension needed)
* Format: Written in Groovy scripting language
* Name: Jenkinsfile (case-sensitive)
* Location: Keep it in the root of your repo (same level as pom.xml)

🧠 Example Repo Structure:

cloudbeesUiAutomationAssignment/
├── pom.xml
├── testng.xml
├── Jenkinsfile
├── Dockerfile
├── k8/
│   └── k8-job.yaml
└── src/

✅ 3. Where to create k8/k8-job.yaml?

🔹 This is a Kubernetes YAML file used to tell K8s to run pods with your tests.

📍 Location:

* Create it inside your automation repo — not in the K8s dashboard.
* Recommended path: k8/k8-job.yaml

🧠 Why?
Because your Jenkinsfile will apply it using:
sh "kubectl apply -f k8/k8-job.yaml"

✅ 4. How to Create Jenkins Pipeline That Uses Dockerfile + Jenkinsfile + testng.xml?

🪜 Step 1: Push Your Framework Repo

Ensure your GitHub repo contains:

* Dockerfile
* Jenkinsfile
* pom.xml + testng.xml
* k8/k8-job.yaml

🪜 Step 2: Create a New Pipeline Job in Jenkins

* Jenkins dashboard → New Item → Pipeline
* Under "Pipeline" section:

  * Select: “Pipeline script from SCM”
  * SCM: Git
  * Repo URL: your GitHub repo URL
  * Script Path: Jenkinsfile

🪜 Step 3: Setup DockerHub Credentials

* Go to: Jenkins > Manage Jenkins > Credentials
* Add “Username with password” type
* ID: dockerhub-creds

🪜 Step 4: Configure Jenkins Agent

* Ensure Docker is installed
* Ensure kubectl is available and connected to K8s cluster

🪜 Step 5: Run Jenkins Job

* Jenkins will:

  * Clone the repo
  * Build Docker image using Dockerfile
  * Push image to DockerHub
  * Apply the k8-job.yaml to create 4 pods
  * Each pod pulls the image and runs mvn clean test with your testng.xml

✅ Summary Table

| What             | Where to Put              | Why                                       |
| ---------------- | ------------------------- | ----------------------------------------- |
| Dockerfile       | Root of the repo          | Builds Docker image with your tests       |
| Jenkinsfile      | Root of the repo          | Tells Jenkins what steps to follow        |
| k8-job.yaml      | Inside k8/ folder in repo | Used by Jenkins to deploy tests in 4 pods |
| Jenkins Pipeline | Jenkins UI → New Item     | Triggers Jenkinsfile                      |

─────────────────────────────────────────────────────────────
❓ Why this step in the Jenkinsfile?

stage('Clone Repository') {
steps {
git '[https://github.com/sachinKhandelwall/cloudbeesUiAutomationAssignment.git](https://github.com/sachinKhandelwall/cloudbeesUiAutomationAssignment.git)'
}
}

🔹 This line is used only if you're pasting pipeline code directly into Jenkins, not when using “Pipeline script from SCM”.

🧠 Jenkins already clones the repo if you use “Pipeline script from SCM” with a Jenkinsfile.

✅ So you DO NOT need a separate git step inside your Jenkinsfile.

❌ Remove this:
stage('Clone Repository') {
steps {
git '[https://github.com/sachinKhandelwall/cloudbeesUiAutomationAssignment.git](https://github.com/sachinKhandelwall/cloudbeesUiAutomationAssignment.git)'
}
}

✅ Keep only:

* Build Docker Image
* Push to DockerHub
* Deploy to K8s

✅ Final Answer:
Only use git clone inside Jenkinsfile when:

* You’re using “Pipeline script” directly
* OR the pipeline needs to clone a secondary repo

─────────────────────────────────────────────────────────────

