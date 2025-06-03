
📝 Jenkins + Docker + Kubernetes Setup
For Selenium Java UI Automation Framework
─────────────────────────────────────────────────────────────

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

