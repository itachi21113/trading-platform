# Start with an official Python base image
FROM python:3.10-slim

# Set the working directory in the container
WORKDIR /app

# Copy the file that lists the dependencies
COPY requirements.txt .

# Install the dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of your application's source code into the container
COPY . .

# Expose the port that FastAPI will run on (Uvicorn's default is 8000)
EXPOSE 8000

# The command to run the application when the container starts
# --host 0.0.0.0 makes the app accessible from outside the container
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]