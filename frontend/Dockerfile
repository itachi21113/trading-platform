# Use the official lightweight Nginx image from Docker Hub
FROM nginx:alpine

# Copy all the frontend files into the default Nginx web root directory
COPY . /usr/share/nginx/html

# Expose port 80, which is the default port for Nginx
EXPOSE 80

# The Nginx image already has a default command to start the server,
# so we don't need to specify a CMD or ENTRYPOINT.