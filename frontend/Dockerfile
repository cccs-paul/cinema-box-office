# Node.js base image for Angular frontend build
FROM node:20-alpine AS builder

WORKDIR /build

# Copy package files
COPY frontend/package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY frontend/ ./

# Build Angular application
RUN npm run build

# Stage 2: Nginx runtime
FROM nginx:1.27-alpine

WORKDIR /usr/share/nginx/html

# Copy built Angular app to Nginx
COPY --from=builder /build/dist/cinema-box-office ./

# Copy Nginx configuration
COPY frontend/nginx.conf /etc/nginx/nginx.conf

# Create non-root user
RUN addgroup -g 1000 nginx-user && \
    adduser -D -u 1000 -G nginx-user nginx-user && \
    chown -R nginx-user:nginx-user /usr/share/nginx/html

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:80/ || exit 1

# Expose port
EXPOSE 80

# Switch to non-root user
USER nginx-user

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
