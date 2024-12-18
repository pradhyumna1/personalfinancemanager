# Use Node.js as a base image for building the frontend
FROM node:16-alpine

# Set working directory in the container
WORKDIR /app

# Add `/app/node_modules/.bin` to $PATH
ENV PATH /app/node_modules/.bin:$PATH

# Copy the package.json and package-lock.json to install dependencies
COPY package.json package-lock.json ./

# Install dependencies
RUN npm install

# Copy the rest of the app files to the container
COPY . .

# Start the development server
CMD ["npm", "start"]
