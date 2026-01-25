# Node.js build stage for Angular frontend using Ubuntu
FROM ubuntu:24.04 AS builder

WORKDIR /build

# Install Node.js and build tools
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    gnupg \
    ca-certificates && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y --no-install-recommends nodejs && \
    rm -rf /var/lib/apt/lists/*

# Copy package files
COPY frontend/package*.json ./

# Install dependencies (including dev dependencies needed for build)
RUN npm install

# Copy source code
COPY frontend/ ./

# Build Angular application
RUN npm run build

# Stage 2: Nginx runtime using Ubuntu
FROM ubuntu:24.04

WORKDIR /usr/share/nginx/html

# Install Nginx and curl
RUN apt-get update && apt-get install -y --no-install-recommends \
    nginx \
    curl && \
    rm -rf /var/lib/apt/lists/* && \
    rm -rf /etc/nginx/sites-enabled/* && \
    rm -rf /etc/nginx/sites-available/*

# Copy built Angular app to Nginx (Angular 17+ outputs to browser subfolder)
COPY --from=builder /build/dist/myrc/browser ./

# Copy Nginx configuration
COPY frontend/nginx.conf /etc/nginx/nginx.conf

# Create all required nginx directories as root, then set proper permissions
RUN mkdir -p /var/lib/nginx/body /var/lib/nginx/fastcgi /var/lib/nginx/proxy /var/lib/nginx/scgi /var/lib/nginx/uwsgi \
    /var/log/nginx /var/cache/nginx /var/run/nginx /tmp/nginx && \
    chmod -R 755 /var/lib/nginx /tmp/nginx && \
    useradd -m -u 1001 nginx-user && \
    chown -R nginx-user:nginx-user /usr/share/nginx/html && \
    chown -R nginx-user:nginx-user /var/lib/nginx && \
    chown -R nginx-user:nginx-user /var/log/nginx && \
    chown -R nginx-user:nginx-user /var/cache/nginx && \
    chown -R nginx-user:nginx-user /var/run/nginx && \
    chown -R nginx-user:nginx-user /tmp/nginx && \
    chown -R nginx-user:nginx-user /etc/nginx

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:80/ || exit 1

# Expose port
EXPOSE 80

# Switch to non-root user
USER nginx-user

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
