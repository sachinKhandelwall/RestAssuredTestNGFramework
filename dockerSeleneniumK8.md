# Docker and Kubernetes Basic Concepts

### Links: 
- https://chatgpt.com/canvas/shared/683eb01e41808191bfdfffb552d0bd1c
- https://chatgpt.com/canvas/shared/683ef87ae4d88191b889e4ee5c3e9b57


## Dockerfile and Its Use in Selenium Java UI Automation Framework

### What is a Dockerfile?

A Dockerfile is a text file that contains a list of instructions used to create a Docker image. Think of it like a recipe or blueprint for building a container. Each instruction in the Dockerfile tells Docker how to set up the image.

### Why use Dockerfile for Selenium Java UI Automation?

When running Selenium UI tests (especially with Java + Maven + browsers), you often face challenges like:

- Environment setup issues (Java version, browser version, drivers, etc.)
- Test failures due to system differences
- Hard to scale across machines or CI pipelines

Dockerfile solves this by letting you:

- Create a consistent environment (same for dev, QA, CI)
- Package Java, Maven, Chrome/Firefox, and tests into one image
- Run tests easily in containers anywhere (locally or in CI/CD)

So, your automation tests become portable, reproducible, and easy to run.

---

### Explanation of Dockerfile lines:

```Dockerfile
# Set the working directory
WORKDIR /automation

# Copy your project files into the container
COPY . .
```

#### 1. WORKDIR /automation

- This sets the working directory inside the container to `/automation`.
- Any command after this will be run as if you are inside that folder.
- It's similar to `cd /automation` before running any script.

#### 2. COPY . .

- Copies all files from your local project directory (where the Dockerfile is) into the `/automation` folder inside the container.
- The first `.` is the source (host machine)
- The second `.` is the destination (inside the image at /automation)

So this ensures your pom.xml, test classes, drivers, and configs all get copied into the image.

---

### Explanation of Docker Build and Run

```bash
docker build -t selenium-tests .
docker run --rm selenium-tests
```

#### docker build -t selenium-tests .

- This builds a Docker image using the Dockerfile in the current directory (`.`)
- `-t selenium-tests` gives your image a name (tag)
- After this command, you have an image called `selenium-tests`

#### docker run --rm selenium-tests

- This runs a container from the `selenium-tests` image
- `--rm` tells Docker to remove the container after it finishes running (clean up)

So this runs your tests (assuming your Dockerfile CMD or ENTRYPOINT starts them)

---

### What is selenium-tests?

It is the name/tag given to the Docker image built from your Dockerfile.
You can think of it like a named version of your test environment.

---

### Can 1 Docker Image Have Only 1 Container?

🟢 No — one Docker image can be used to create many containers.

#### Image vs Container

- Docker Image = A blueprint or recipe (like a class in Java).
- Docker Container = A running instance made from the image (like an object in Java).

So:
🧱 One image → 🔁 Can create multiple containers.

#### Example:

You build an image:

```bash
docker build -t selenium-tests .
```

Now you can run multiple containers from it:

```bash
docker run --rm selenium-tests         # Container 1
docker run --rm selenium-tests         # Container 2
docker run --rm selenium-tests         # Container 3
```

Each time you run this, Docker starts a new container using the same image.
✅ They can run in parallel, and independently.

#### Use Case:

```bash
docker run -e BROWSER=chrome selenium-tests
docker run -e BROWSER=firefox selenium-tests
```

Each container runs the same tests on different browsers in parallel.

---

### If I write `docker run --rm selenium-tests`, it will create only one container?

✅ Yes, you're absolutely right.

When you run:

```bash
docker run --rm selenium-tests
```

It will:

- Create one container from the `selenium-tests` image
- Run the tests (or whatever command you set)
- Automatically remove the container after it exits (because of `--rm`)

If you run the same command again, Docker creates a new container each time.

---

### What would be the use of Kubernetes here and how to integrate Docker with K8s

#### Why use Kubernetes with Selenium Java UI tests?

Docker runs containers — great for local or CI/CD.
But when you want to:

- Run tests in parallel
- Scale automatically
- Retry failed containers
- Use Selenium Grid
- Manage hundreds of tests across machines

👉 Use Kubernetes

#### Kubernetes Use Cases in UI Automation:

- Run 5, 50, or 500 test containers in parallel
- Manage test failures and retries
- Connect test pods to Selenium Grid (Hub + Nodes)
- Run tests per commit in CI pipeline

#### How to integrate:

1. Build your Docker image

```bash
docker build -t selenium-tests .
```

2. Push it to a registry

```bash
docker tag selenium-tests yourusername/selenium-tests
docker push yourusername/selenium-tests
```

3. Create a Kubernetes Job YAML

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: selenium-ui-tests
spec:
  completions: 1
  parallelism: 1
  template:
    spec:
      containers:
      - name: test-runner
        image: yourusername/selenium-tests
        env:
        - name: BROWSER
          value: "chrome"
      restartPolicy: Never
```

4. Apply it

```bash
kubectl apply -f selenium-tests-job.yaml
```

---

### Explanation of:

```bash
docker tag selenium-tests yourusername/selenium-tests
docker push yourusername/selenium-tests
```

#### docker tag

- Renames the image for DockerHub format
- yourusername = your DockerHub ID

#### docker push

- Uploads the image to DockerHub
- Now it’s available online for K8s or CI to use

---

### How Kubernetes fetches the image

When you define:

```yaml
containers:
- name: test
  image: yourusername/selenium-tests
```

Kubernetes will:

1. See the image name
2. Pull it from DockerHub (or another registry)
3. Start a container from it in a pod

If it's a private image, you need to:

- Create a secret
- Add imagePullSecrets in YAML

---

### So 1 container means 1 pod?

✅ Most of the time: Yes

- A Pod is the smallest deployable unit in K8s
- It usually runs 1 container
- But it can run multiple (advanced use cases)

#### Examples:

- 1 selenium-tests container = 1 pod (normal)
- 1 pod with test + helper container (sidecar) = uncommon for testing

So: 1 container ≈ 1 pod (99% of the time in test automation)

---

### How does Kubernetes run the container after pulling the image?

1. You apply a YAML file (`kubectl apply -f selenium-tests.yaml`)
2. K8s scheduler chooses a worker node
3. Kubelet on that node pulls the image
4. It creates a Pod with your container
5. Runs the CMD/ENTRYPOINT from the Dockerfile (e.g. `mvn test`)
6. Kubernetes monitors the container (restart or clean up based on type)

K8s doesn’t use docker run under the hood. It uses containerd or another runtime.

---

### What is Node, Cluster, ReplicaSet, and How to Scale Pods

#### Node

- A machine (VM or physical) that runs your workloads (containers)
- Has Docker/container runtime + kubelet

#### Cluster

- A group of nodes managed by Kubernetes
- Control plane + worker nodes

#### ReplicaSet

- Ensures X number of pods are running at all times
- You usually don’t create it directly — it’s managed by a Deployment

#### Deployment

- Higher-level object
- Manages ReplicaSets
- Lets you update app versions and scale easily

#### Manual Scaling:

```bash
kubectl scale deployment selenium-runner --replicas=5
```

- K8s creates or removes pods to match 5 total

#### Autoscaling (based on CPU):

```bash
kubectl autoscale deployment selenium-runner --cpu-percent=50 --min=1 --max=10
```

- K8s watches CPU
- If usage > 50%, adds more pods (up to 10)
- If low, scales down (but not below 1)

You must define resources.requests and limits in the pod/container for this to work.


---------------------------------------------------------------------------------------------------------------------------------

# Complete Guide to Run UI Automation Repo on Jenkins with Docker & Kubernetes (4 Parallel Pods)
### Repo: https://github.com/sachinKhandelwall/cloudbeesUiAutomationAssignment


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


---------------------------------------------------------------------------------------------------------------------------------

# Clarifying my Doubts


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

