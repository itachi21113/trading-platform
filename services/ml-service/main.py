from fastapi import FastAPI
from pydantic import BaseModel
import pandas as pd
from typing import List

from database import fetch_price_data
from model import create_features, create_target, train_model

app = FastAPI(title="Trading Platform ML Service")

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
    # ... (this function remains exactly the same)
    df_raw = fetch_price_data()
    if df_raw.empty:
        return {"error": "Could not fetch data to train model."}

    df_featured = create_features(df_raw)
    df_final = create_target(df_featured)

    model, accuracy = train_model(df_final)

    if model is None:
        return {"error": "Not enough data to train the model."}

    model_cache["model"] = model
    model_cache["accuracy"] = accuracy

    return {"message": "Model trained successfully", "accuracy": accuracy}

# NEW: Endpoint to make predictions
@app.post("/predict")
def predict_price_movement(request: PredictionRequest):
    """
    Receives recent price ticks and predicts the next movement.
    """
    model = model_cache.get("model")
    if model is None:
        return {"error": "Model not trained yet. Please call the /train endpoint first."}

    # 1. Convert incoming data to a DataFrame
    data = [tick.dict() for tick in request.ticks]
    df = pd.DataFrame(data)

    # 2. Create the same features the model was trained on
    df_featured = create_features(df)

    if df_featured.empty:
        return {"error": "Not enough recent data to create features for a prediction."}

    # 3. Select the features for the last available row
    features = [col for col in df_featured.columns if col not in ['id', 'symbol', 'price', 'future_price', 'target']]
    last_row_features = df_featured[features].tail(1)

    # 4. Make a prediction
    prediction_result = model.predict(last_row_features)
    prediction_text = "UP" if prediction_result[0] == 1 else "DOWN"

    return {"prediction": prediction_text}