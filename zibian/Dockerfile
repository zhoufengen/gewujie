# Build stage
FROM registry.cn-shenzhen.aliyuncs.com/sync_dockerhub/node:20-alpine as build-stage

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# Production stage
FROM registry.cn-shenzhen.aliyuncs.com/sync_dockerhub/nginx:1.29.4 as production-stage

COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
