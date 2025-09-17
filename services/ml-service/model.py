import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score

def create_features(df):
    """
    Creates time-series features from the price data.
    """
    # Ensure timestamp is a datetime object and set as index
    df['timestamp'] = pd.to_datetime(df['timestamp'])
    df = df.set_index('timestamp')

    # Create lag features (price from previous time steps)
    for i in range(1, 6): # Lags from 1 to 5 ticks ago
        df[f'lag_{i}'] = df['price'].shift(i)

    # Create moving average features
    df['sma_5'] = df['price'].rolling(window=5).mean()
    df['sma_10'] = df['price'].rolling(window=10).mean()

    # Drop rows with NaN values created by lags and rolling windows
    df.dropna(inplace=True)
    return df

def create_target(df, look_ahead_period=10):
    """
    Creates the target variable for prediction.
    Target = 1 if the price goes up in the next `look_ahead_period` ticks, else 0.
    """
    df['future_price'] = df['price'].shift(-look_ahead_period)
    df.dropna(inplace=True) # Drop last rows where future price is unknown

    df['target'] = (df['future_price'] > df['price']).astype(int)
    return df

def train_model(df):
    """
    Trains a RandomForestClassifier model on the prepared data.
    """
    if 'target' not in df.columns or df.empty:
        return None, 0.0

    # Define features (X) and target (y)
    features = [col for col in df.columns if col not in ['id', 'symbol', 'price', 'future_price', 'target']]
    X = df[features]
    y = df['target']

    # Split data into training and testing sets
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42, shuffle=False)

    if len(X_train) == 0:
        return None, 0.0

    # Initialize and train the model
    model = RandomForestClassifier(n_estimators=100, random_state=42, n_jobs=-1)
    model.fit(X_train, y_train)

    # Evaluate the model
    predictions = model.predict(X_test)
    accuracy = accuracy_score(y_test, predictions)

    print(f"Model trained with accuracy: {accuracy:.4f}")

    return model, accuracy