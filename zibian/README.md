# Vue 3 + TypeScript + Vite

This template should help get you started developing with Vue 3 and TypeScript in Vite. The template uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.

Learn more about the recommended Project Setup and IDE Support in the [Vue Docs TypeScript Guide](https://vuejs.org/guide/typescript/overview.html#project-setup).


# Development Setup

## Standard Development

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build
```

## Docker Deployment

This project includes Docker configuration for easy deployment.

### Prerequisites

- Docker
- Docker Compose

### Building and Running with Docker Compose

1. **Build and Run in one command:**

   ```bash
   docker-compose up -d --build
   ```

   The application will be accessible at http://localhost:8080.

   - `-d`: Run in detached mode (in the background).
   - `--build`: Force build of images before starting containers.

2. **Stop the container:**

   ```bash
   docker-compose down
   ```

3. **View logs:**

   ```bash
   docker-compose logs -f
   ```

### Building and Running with Docker (Manual)

1. **Build the image:**

   ```bash
   docker build -t zibian-app .
   ```

2. **Run the container:**

   ```bash
   docker run -d -p 8080:80 --name zibian-container zibian-app
   ```

   The application will be accessible at http://localhost:8080.
