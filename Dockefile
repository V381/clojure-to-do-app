# Use the official Clojure image as the base image
FROM clojure:openjdk-11-tools-deps

# Set the working directory in the container
WORKDIR /app

# Copy the project files to the container
COPY . .

# Install the dependencies and build the uberjar
RUN clojure -A:uberjar

# Expose the port that the app runs on
EXPOSE 3000

# Define the command to run the application
CMD ["java", "-Dclojure.main.report=stderr", "-jar", "target/uberjar/clojure-to-do-app.jar"]
