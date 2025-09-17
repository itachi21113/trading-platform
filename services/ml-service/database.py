import os
import pandas as pd
import psycopg2
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

def fetch_price_data():
    """
    Connects to the PostgreSQL database and fetches all price ticks
    into a pandas DataFrame.
    """
    conn = None
    try:
        # Connect to the database
        conn = psycopg2.connect(
            host=os.getenv("DB_HOST"),
            port=os.getenv("DB_PORT"),
            dbname=os.getenv("DB_NAME"),
            user=os.getenv("DB_USER"),
            password=os.getenv("DB_PASSWORD")
        )

        # SQL query to fetch the data
        sql_query = "SELECT * FROM price_ticks ORDER BY timestamp ASC;"

        # Use pandas to execute the query and load into a DataFrame
        df = pd.read_sql_query(sql_query, conn)

        print(f"Successfully fetched {len(df)} rows from the database.")
        return df

    except (Exception, psycopg2.DatabaseError) as error:
        print(f"Error connecting to PostgreSQL: {error}")
        return pd.DataFrame() # Return empty DataFrame on error
    finally:
        if conn is not None:
            conn.close()