.PHONY: k8s-start k8s-deploy k8s-stop k8s-logs

k8s-start:
	@echo "Starting Minikube cluster..."
	minikube start --container-runtime=docker --memory=4096 --cpus=2

k8s-deploy: k8s-start
	@echo "Packaging latest Java application JAR..."
	mvn package -DskipTests
	@echo "Building Docker image directly inside Minikube..."
	minikube image build -t online-encryption-hub:latest .
	@echo "Applying Kubernetes manifests..."
	kubectl apply -f k8s/
	kubectl get pods -w

k8s-stop:
	minikube stop

k8s-logs:
	kubectl logs -l app=encryption-api -f