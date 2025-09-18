from fastapi import FastAPI
from pydantic import BaseModel
import pandas as pd
from typing import List
from contextlib import asynccontextmanager
import threading
import time

from database import fetch_price_data
from model import create_features, create_target, train_model

# --- Lifespan event for startup training ---
@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Handles application startup and shutdown events.
    On startup, it waits a few seconds for the database to be ready,
    then triggers the initial model training in a background thread.
    """
    print("Application startup: Kicking off initial model training...")

    # Give the DB a moment to start up, especially in Docker Compose
    time.sleep(10)

    # Run training in a background thread so it doesn't block the API from starting
    training_thread = threading.Thread(target=train_new_model)
    training_thread.start()

    yield # The application runs here

    print("Application shutdown.")

# --- App Initialization ---
app = FastAPI(title="Trading Platform ML Service", lifespan=lifespan)


# This will hold our trained model in memory
model_cache = {
    "model": None,
    "accuracy": 0.0
}

# --- Pydantic Models for Data Validation ---
class PriceTick(BaseModel):
    timestamp: str  # We'll receive timestamps as strings in ISO format
    price: float

class PredictionRequest(BaseModel):
    ticks: List[PriceTick]

# --- API Endpoints ---
@app.get("/")
def read_root():
    return {"message": "ML Service is running"}

@app.get("/train")
def train_new_model():
    """
    Fetches data, trains the model, and stores it in the cache.
    """
    print("Attempting to train a new model...")
    df_raw = fetch_price_data()
    if df_raw.empty:
        print("Training failed: Could not fetch data.")
        return {"error": "Could not fetch data to train model."}

    df_featured = create_features(df_raw)
    df_final = create_target(df_featured)

    model, accuracy = train_model(df_final)

    if model is None:
        print("Training failed: Not enough data.")
        return {"error": "Not enough data to train the model."}

    model_cache["model"] = model
    model_cache["accuracy"] = accuracy

    print(f"Model trained successfully with accuracy: {accuracy}")
    return {"message": "Model trained successfully", "accuracy": accuracy}

@app.post("/predict")
def predict_price_movement(request: PredictionRequest):
    """
    Receives recent price ticks and predicts the next movement.
    """
    model = model_cache.get("model")
    if model is None:
        return {"prediction": "TRAINING"} # Return a clear status if not ready

    # 1. Convert incoming data to a DataFrame
    data = [tick.dict() for tick in request.ticks]
    df = pd.DataFrame(data)

    # 2. Create the same features the model was trained on
    df_featured = create_features(df)

    if df_featured.empty:
        return {"prediction": "INSUFFICIENT_DATA"}

    # 3. Select the features for the last available row
    features = [col for col in df_featured.columns if col not in ['id', 'symbol', 'price', 'future_price', 'target']]
    last_row_features = df_featured[features].tail(1)

    # 4. Make a prediction
    prediction_result = model.predict(last_row_features)
    prediction_text = "UP" if prediction_result[0] == 1 else "DOWN"

    return {"prediction": prediction_text}