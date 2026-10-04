import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
import joblib

# 1. Load Data
df = pd.read_csv('honeypot_telemetry.csv')
X = df[['request_rate', 'time_variance', 'invalid_path_ratio', 'payload_entropy']]
y = df['is_bot']

# 2. Split & Scale
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
scaler = StandardScaler()
X_train_scaled = scaler.fit_transform(X_train)

# 3. Train Classifier
clf = LogisticRegression(class_weight='balanced')
clf.fit(X_train_scaled, y_train)

# 4. Save Model and Scaler for the gRPC Server
joblib.dump(clf, 'logistic_model.pkl')
joblib.dump(scaler, 'scaler.pkl')