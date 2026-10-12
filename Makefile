.PHONY: k8s-start k8s-deploy k8s-stop k8s-logs

k8s-start:
	@echo "Starting Minikube cluster..."
	minikube start --container-runtime=docker

k8s-deploy: k8s-start
	@echo "Building Docker image directly inside Minikube..."
	minikube image build -t online-encryption-hub:latest .
	@echo "Applying Kubernetes manifests..."
	kubectl apply -f k8s/config.yml
	kubectl apply -f k8s/redis.yml
	kubectl apply -f k8s/postgres.yml
	kubectl apply -f k8s/deployment.yml
	kubectl apply -f k8s/worker-statefulset.yml
	kubectl get pods -w

k8s-stop:
	minikube stop

k8s-logs:
	kubectl logs -l app=encryption-api -f